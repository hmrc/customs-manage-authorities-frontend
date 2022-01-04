/*
 * Copyright 2022 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.{AuthorisedUserFormProvider, AuthorityDetailsFormProvider}
import models.NormalMode
import models.requests.DataRequest
import navigation.Navigator
import pages.edit.EditAuthorisedUserPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.edit.EditAuthorisedUserView
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class EditAuthorisedUserController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: AuthorityDetailsFormProvider,
                                              val dateTimeService: DateTimeService,
                                              sessionRepository: SessionRepository,
                                              view: EditAuthorisedUserView,
                                              navigator: Navigator,
                                              implicit val controllerComponents: MessagesControllerComponents,
                                            )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport with Logging {

  private val form = formProvider()

  lazy val commonActions: ActionBuilder[DataRequest, AnyContent] = identify andThen getData andThen requireData

  def onPageLoad(accountId: String, authorityId: String): Action[AnyContent] = commonActions { implicit request =>
    val populatedForm = request.userAnswers.get(EditAuthorisedUserPage(accountId, authorityId)) match {
      case Some(value) => form.fill(value)
      case None => form
    }
    Ok(view(populatedForm, accountId, authorityId))
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
}
