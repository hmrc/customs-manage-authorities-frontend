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

import controllers.actions._
import forms.EditAuthorityStartFormProvider
import models.{AuthorityStart, NormalMode}
import navigation.Navigator
import pages.edit._
import play.api.i18n._
import play.api.mvc._
import repositories.SessionRepository
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.edit.EditAuthorityStartView

import javax.inject.Inject
import scala.concurrent._

class EditAuthorityStartController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              sessionRepository: SessionRepository,
                                              navigator: Navigator,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              dateTimeService: DateTimeService,
                                              formProvider: EditAuthorityStartFormProvider,
                                              implicit val controllerComponents: MessagesControllerComponents,
                                              view: EditAuthorityStartView
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {



  def onPageLoad(accountId: String, authorityId: String): Action[AnyContent] = (
    identify andThen getData andThen requireData
    ) { implicit request =>
      val form = formProvider(request.userAnswers.get(EditAuthorityEndDatePage(accountId, authorityId)), dateTimeService.localDate())
      val preparedForm = request.userAnswers.get(EditAuthorityStartPage(accountId, authorityId)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, accountId, authorityId))
  }

  def onSubmit(accountId: String, authorityId: String): Action[AnyContent] = (
    identify andThen getData andThen requireData
    ).async { implicit request =>
      val form = formProvider(request.userAnswers.get(EditAuthorityEndDatePage(accountId, authorityId)), dateTimeService.localDate())
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, accountId, authorityId))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EditAuthorityStartPage(accountId, authorityId), value)(AuthorityStart.writes))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(EditAuthorityStartPage(accountId: String, authorityId: String), NormalMode, updatedAnswers))
      )
  }
}
