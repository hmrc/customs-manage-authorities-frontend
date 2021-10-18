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

import connectors.CustomsFinancialsConnector
import controllers.actions._
import models.domain.{AccountWithAuthoritiesWithId, AuthoritiesWithId}
import models.requests.DataRequest
import models.{ErrorResponse, MissingAccountError, MissingAuthorityError, NormalMode, UserAnswers}
import navigation.Navigator
import pages.edit._
import play.api.Logging
import play.api.i18n._
import play.api.mvc._
import services._
import services.edit.EditAuthorityValidationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CheckYourAnswersEditHelper
import views.html.edit.EditCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent._

class EditCheckYourAnswersController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                service: AuthoritiesCacheService,
                                                connector: CustomsFinancialsConnector,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                dateTimeService: DateTimeService,
                                                editAuthorityValidationService: EditAuthorityValidationService,
                                                view: EditCheckYourAnswersView,
                                                navigator: Navigator,
                                                implicit val controllerComponents: MessagesControllerComponents,
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  lazy val commonActions: ActionBuilder[DataRequest, AnyContent] = identify andThen getData andThen requireData


  def onPageLoad(accountId: String, authorityId: String): Action[AnyContent] = commonActions.async { implicit request =>
    service.getAccountAndAuthority(request.internalId, authorityId, accountId).map {
      case Left(NoAuthority) => errorPage(MissingAuthorityError)
      case Left(NoAccount) => errorPage(MissingAccountError)
      case Right(AccountAndAuthority(account, authority)) =>
        val helper = new CheckYourAnswersEditHelper(
          request.userAnswers, accountId, authorityId, dateTimeService, authority, account)
        Ok(view(helper, accountId, authorityId))
    }
  }

  def onSubmit(accountId: String, authorityId: String): Action[AnyContent] = commonActions.async { implicit request =>
    service.getAccountAndAuthority(request.internalId, authorityId, accountId).flatMap {
      case Left(NoAuthority) => Future.successful(errorPage(MissingAuthorityError))
      case Left(NoAccount) => Future.successful(errorPage(MissingAccountError))
      case Right(AccountAndAuthority(account, authority)) => doSubmission(
        request.userAnswers, accountId, authorityId, authority.authorisedEori, account
      )
    }
  }



  private def doSubmission(userAnswers: UserAnswers,
                           accountId: String,
                           authorityId: String,
                           authorisedEori: String,
                           account: AccountWithAuthoritiesWithId)(implicit hc: HeaderCarrier): Future[Result] = {

    editAuthorityValidationService.validate(userAnswers, accountId, authorityId, authorisedEori, account) match {
      case Right(payload) => connector.grantAccountAuthorities(payload).map {
        case true => Redirect(navigator.nextPage(EditCheckYourAnswersPage(accountId, authorityId), NormalMode, userAnswers))
        case false =>
          logger.error("Edit authority request submission to backend failed")
          Redirect(controllers.routes.TechnicalDifficulties.onPageLoad())
      }
      case _ =>
        logger.error("UserAnswers did not contain sufficient data to construct add authority request")
        Future.successful(Redirect(controllers.routes.TechnicalDifficulties.onPageLoad()))
    }
  }

  private def errorPage(error: ErrorResponse): Result = {
    logger.error(error.msg)
    Redirect(controllers.routes.TechnicalDifficulties.onPageLoad())
  }
}
