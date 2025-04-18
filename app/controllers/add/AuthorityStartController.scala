/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.add

import config.FrontendAppConfig
import controllers.actions._
import forms.AuthorityStartFormProvider
import models.{AuthorityStart, Mode}
import navigation.Navigator
import pages.add.AuthorityStartPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.AuthorityStartView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorityStartController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AuthorityStartFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AuthorityStartView
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(AuthorityStartPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(
      view(
        preparedForm,
        mode,
        navigator.backLinkRoute(mode, controllers.add.routes.AccountsController.onPageLoad(mode))
      )
    )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  mode,
                  navigator.backLinkRoute(mode, controllers.add.routes.AccountsController.onPageLoad(mode))
                )
              )
            ),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(AuthorityStartPage, value)(AuthorityStart.writes))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AuthorityStartPage, mode, updatedAnswers))
        )
  }
}
