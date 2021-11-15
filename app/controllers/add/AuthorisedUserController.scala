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
import connectors.CustomsFinancialsConnector
import controllers.actions._
import forms.AuthorisedUserFormProviderWithConsent
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import pages.add.AuthorisedUserPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.DateTimeService
import services.add.{AddAuthorityValidationService, CheckYourAnswersValidationService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CheckYourAnswersHelper
import views.html.add.AuthorisedUserView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedUserController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          sessionRepository: SessionRepository,
                                          navigator: Navigator,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          connector: CustomsFinancialsConnector,
                                          formProvider: AuthorisedUserFormProviderWithConsent,
                                          cyaValidationService: CheckYourAnswersValidationService,
                                          addAuthorityValidationService: AddAuthorityValidationService,
                                          dateTimeService: DateTimeService,
                                          verifyAccountNumbers: VerifyAccountNumbersAction,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: AuthorisedUserView
                                        )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport with Logging {

  private val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen verifyAccountNumbers) {
    implicit request =>
      cyaValidationService.validate(request.userAnswers)
        .fold(
          errorPage("UserAnswers did not contain sufficient data for Check your answers")
        ) {
          _ =>
            val helper = CheckYourAnswersHelper(request.userAnswers, dateTimeService)
            val preparedForm = request.userAnswers.get(AuthorisedUserPage) match {
              case None => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, helper))
        }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData andThen verifyAccountNumbers).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val helper = CheckYourAnswersHelper(request.userAnswers, dateTimeService)
          Future.successful(BadRequest(view(formWithErrors, helper)))
        },
        value =>
          (for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AuthorisedUserPage, value))
            _ <- sessionRepository.set(updatedAnswers)
            result <- doSubmission(updatedAnswers)
          } yield result).recover {
            case _ => Redirect(controllers.routes.TechnicalDifficulties.onPageLoad())
          }
      )
  }

  def doSubmission(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Result] = {
    addAuthorityValidationService.validate(userAnswers)
      .fold(
        Future.successful(errorPage("UserAnswers did not contain sufficient data to construct add authority request"))
      ) { payload =>
        connector.grantAccountAuthorities(payload).map {
          case true => Redirect(navigator.nextPage(AuthorisedUserPage, NormalMode, userAnswers))
          case false => errorPage("Add authority request submission to backend failed")
        }
      }
  }

  private def errorPage(msg:String) = {
    logger.error(msg)
    Redirect(controllers.routes.TechnicalDifficulties.onPageLoad())
  }

}
