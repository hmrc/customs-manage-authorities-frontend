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
import forms.AuthorityStartDateFormProvider
import models.Mode
import navigation.Navigator
import pages.add.AuthorityStartDatePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.AuthorityStartDateView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorityStartDateController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              sessionRepository: SessionRepository,
                                              navigator: Navigator,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: AuthorityStartDateFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: AuthorityStartDateView
                                            )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
  extends FrontendBaseController
    with I18nSupport {

  private def form: Form[LocalDate] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(AuthorityStartDatePage) match {
        case None => form
        case Some(date) => form.fill(date)
      }

      Ok(view(
        preparedForm,
        mode,
        navigator.backLinkRoute(mode, controllers.add.routes.AuthorityStartController.onPageLoad(mode)))
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              view(
                formWithErrors,
                mode,
                navigator.backLinkRoute(mode, controllers.add.routes.AuthorityStartController.onPageLoad(mode)))
            )
          ),
        date =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AuthorityStartDatePage, date))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AuthorityStartDatePage, mode, updatedAnswers))
      )
  }
}
