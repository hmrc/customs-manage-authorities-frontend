/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.edit

import cats.data.OptionT._
import cats.implicits._
import controllers.actions._
import org.slf4j.LoggerFactory
import pages.ConfirmationPage
import pages.edit.EditAuthorityStartDatePage
import play.api.i18n._
import play.api.mvc._
import repositories._
import services._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateUtils
import views.html.edit.EditConfirmationView

import javax.inject.Inject
import scala.concurrent._

class EditConfirmationController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            sessionRepository: SessionRepository,
                                            accountsRepository: AccountsRepository,
                                            authoritiesRepository: AuthoritiesRepository,
                                            service: AuthoritiesCacheService,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            confirmationService: ConfirmationService,
                                            implicit val controllerComponents: MessagesControllerComponents,
                                            view: EditConfirmationView
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with DateUtils {

  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  def onPageLoad(accountId: String, authorityId: String): Action[AnyContent] = (
    identify andThen getData andThen requireData).async {
    implicit request =>
      val startDate: Option[String] =
          request.userAnswers.get(EditAuthorityStartDatePage(accountId, authorityId)).map(dateAsDayMonthAndYear)

      val maybeResult = for {
        accountsWithAuthorities <- liftF(service.retrieveAuthorities(request.internalId))
        account                 <- fromOption[Future](accountsWithAuthorities.authorities.get(accountId))
        authority               <- fromOption[Future](account.authorities.get(authorityId))
        eori                    <- fromOption[Future](Some(authority.authorisedEori))
        _                       <- liftF(sessionRepository.clear(request.userAnswers.id))
        _                       <- liftF(accountsRepository.clear(request.internalId.value))
        _                       <- liftF(authoritiesRepository.clear(request.internalId.value))
        _                       <- liftF(confirmationService.populateConfirmation(request.internalId.value, eori, startDate))
      } yield Ok(view(eori, startDate))

      maybeResult.value.flatMap {
        case Some(result) => Future.successful(result)
        case None => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      } recover {
        case _ => {
          request.userAnswers.get(ConfirmationPage) match {
           case Some(value) => Ok(view(value.eori, value.startDate))
            case None => {
              logger.warn("Something went wrong when displaying confirmation page on the edit journey")
              Redirect(controllers.routes.SessionExpiredController.onPageLoad())
            }
          }
        }
      }
    }
}
