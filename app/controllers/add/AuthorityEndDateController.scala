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

package controllers.add

import config.FrontendAppConfig
import controllers.actions._
import forms.AuthorityEndDateFormProvider
import models.Mode
import navigation.Navigator
import pages.add.{AuthorityEndDatePage, AuthorityStartDatePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.AuthorityEndDateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorityEndDateController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: AuthorityEndDateFormProvider,
                                        dateTimeService: DateTimeService,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: AuthorityEndDateView
                                      )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val startDate = request.userAnswers.get(AuthorityStartDatePage).getOrElse(dateTimeService.localTime().toLocalDate)
      val form = formProvider(startDate)

      val preparedForm = request.userAnswers.get(AuthorityEndDatePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode,navigator.backLinkRoute(mode,controllers.add.routes.AuthorityEndController.onPageLoad(mode))))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val startDate = request.userAnswers.get(AuthorityStartDatePage).getOrElse(dateTimeService.localTime().toLocalDate)
      val form = formProvider(startDate)

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode,navigator.backLinkRoute(mode,controllers.add.routes.AuthorityEndController.onPageLoad(mode))))),
        date =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AuthorityEndDatePage, date))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AuthorityEndDatePage, mode, updatedAnswers))
      )
  }
}
