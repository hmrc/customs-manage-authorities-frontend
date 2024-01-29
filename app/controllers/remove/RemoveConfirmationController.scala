/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.remove

import cats.data.OptionT
import cats.implicits._
import config.FrontendAppConfig
import controllers.actions._
import connectors.CustomsDataStoreConnector
import org.slf4j.LoggerFactory
import pages.ConfirmationPage
import play.api.i18n._
import play.api.mvc._
import repositories.AuthoritiesRepository
import services._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.remove.RemoveConfirmationView

import javax.inject.Inject
import scala.concurrent._

class RemoveConfirmationController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           service: AuthoritiesCacheService,
                                           repository: AuthoritiesRepository,
                                           identify: IdentifierAction,
                                           getData : DataRetrievalAction,
                                           confirmationService: ConfirmationService,
                                           implicit val controllerComponents: MessagesControllerComponents,
                                           view: RemoveConfirmationView,
                                           dataStore: CustomsDataStoreConnector
                                         )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
  extends FrontendBaseController
    with I18nSupport {

  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  def onPageLoad(accountId: String, authorityId: String): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      val maybeResult = (for {
         ids          <- OptionT.liftF(service.retrieveAuthorities(request.internalId))
         account      <- OptionT.fromOption[Future](ids.authorities.get(accountId))
         authority    <- OptionT.fromOption[Future](account.authorities.get(authorityId))
         companyName  <- OptionT.liftF(dataStore.getCompanyName(authority.authorisedEori))
         _            <- OptionT.liftF(repository.clear(request.internalId.value))
         _            <- OptionT.liftF(confirmationService.populateConfirmation(
                                                                              request.internalId.value,
                                                                              authority.authorisedEori))
      } yield Ok(view(authority.authorisedEori, companyName))).value

      maybeResult flatMap {
        case Some(result) => Future.successful(result)
        case None => Future.successful(reportSessionExpired)
      } recover {
        case _ =>
          request.userAnswers match {
            case Some(userAnswers) => userAnswers.get(ConfirmationPage) match {
              case Some(value) => Ok(view(value.eori, value.companyName))
              case None => reportSessionExpired
            }
            case None => reportSessionExpired
          }
      }
  }

  private def reportSessionExpired: Result = {
    logger.warn("Something went wrong when displaying confirmation page on the remove journey")
    Redirect(controllers.routes.SessionExpiredController.onPageLoad)
  }

}
