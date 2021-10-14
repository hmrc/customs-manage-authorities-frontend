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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.remove.routes
import forms.AuthorisedUserFormProvider
import models.domain.AuthoritiesWithId
import models.requests.DataRequest
import models.{ErrorResponse, MissingAccountError, MissingAuthorityError, NormalMode}
import navigation.Navigator
import pages.edit.{EditAuthorisedUserPage, EditAuthorityStartPage}
import pages.remove.RemoveAuthorisedUserPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.edit.EditAuthorityValidationService
import services.{AuthoritiesCacheService, DateTimeService, EditSessionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ServiceUnavailableView
import views.html.edit.EditAuthorisedUserView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EditAuthorisedUserController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              service: AuthoritiesCacheService,
                                              connector: CustomsFinancialsConnector,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: AuthorisedUserFormProvider,
                                              val dateTimeService: DateTimeService,
                                              editAuthorityValidationService: EditAuthorityValidationService,
                                              serviceUnavailable: ServiceUnavailableView,
                                              sessionRepository: SessionRepository,
                                              editSessionService: EditSessionService,
                                              view: EditAuthorisedUserView,
                                              navigator: Navigator,
                                              implicit val controllerComponents: MessagesControllerComponents,
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val form = formProvider()

  lazy val commonActions: ActionBuilder[DataRequest, AnyContent] = identify andThen getData andThen requireData

  def onPageLoad(accountId: String, authorityId: String): Action[AnyContent] =
    commonActions { implicit request =>
      Ok(view(form, accountId, authorityId))
    }

  def onSubmit(accountId: String, authorityId: String): Action[AnyContent] =
    commonActions.async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, accountId, authorityId))),
        authorisedUser => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EditAuthorisedUserPage(accountId, authorityId), authorisedUser))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(EditAuthorisedUserPage(accountId: String, authorityId: String), NormalMode, updatedAnswers))
        })
    }

  private def errorPage(error: ErrorResponse) = {
    logger.error(error.msg)
    Redirect(controllers.routes.TechnicalDifficulties.onPageLoad())
  }


}
