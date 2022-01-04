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
import controllers.actions._
import forms.EditAuthorityStartDateFormProvider
import models.NormalMode
import navigation.Navigator
import pages.edit._
import play.api.data.Form
import play.api.i18n._
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.edit.EditAuthorityStartDateView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent._

class EditAuthorityStartDateController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  sessionRepository: SessionRepository,
                                                  navigator: Navigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: EditAuthorityStartDateFormProvider,
                                                  implicit val controllerComponents: MessagesControllerComponents,
                                                  view: EditAuthorityStartDateView
                                                )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  def onPageLoad(accountId: String, authorityId: String): Action[AnyContent] = (
    identify andThen getData andThen requireData
    ) { implicit request =>
      val form: Form[LocalDate] = formProvider()
      val preparedForm = request.userAnswers.get(EditAuthorityStartDatePage(accountId, authorityId)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, accountId, authorityId))
  }

  def onSubmit(accountId: String, authorityId: String): Action[AnyContent] = (
    identify andThen getData andThen requireData
    ).async { implicit request =>
      val form: Form[LocalDate] = formProvider()
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, accountId, authorityId))),
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EditAuthorityStartDatePage(accountId, authorityId), value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(EditAuthorityStartDatePage(accountId, authorityId), NormalMode, request.userAnswers)
          )
        })
  }
}


