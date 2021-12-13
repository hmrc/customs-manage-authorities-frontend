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

package controllers.add

import config.FrontendAppConfig
import controllers.actions._
import forms.AuthorityDetailsFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.add.AuthorityDetailsPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CheckYourAnswersHelper
import views.html.add.AuthorityDetailsView

import scala.concurrent.{ExecutionContext, Future}

class AuthorityDetailsController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            sessionRepository: SessionRepository,
                                            navigator: Navigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: AuthorityDetailsFormProvider,
                                            dateTimeService: DateTimeService,
                                            verifyAccountNumbers: VerifyAccountNumbersAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: AuthorityDetailsView
                                        )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport with Logging {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen verifyAccountNumbers) {
    implicit request =>
      val helper = CheckYourAnswersHelper(request.userAnswers, dateTimeService)
      val preparedForm = request.userAnswers.get(AuthorityDetailsPage) match {
              case None => form
              case Some(value) => form.fill(value)
            }

     Ok(view(preparedForm, helper, mode, navigator.backLinkRoute(mode,controllers.add.routes.ShowBalanceController.onPageLoad(mode))))
  }


  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen verifyAccountNumbers).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val helper = CheckYourAnswersHelper(request.userAnswers, dateTimeService)
          Future.successful(BadRequest(view(formWithErrors, helper, mode, navigator.backLinkRoute(mode,controllers.add.routes.ShowBalanceController.onPageLoad(mode)))))
        },
        value =>
          (for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AuthorityDetailsPage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AuthorityDetailsPage, mode, updatedAnswers))).recover {
            case _ => Redirect(controllers.routes.TechnicalDifficulties.onPageLoad)
          }
      )
  }
}
