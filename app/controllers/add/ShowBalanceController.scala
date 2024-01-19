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
import forms.ShowBalanceFormProvider
import models.domain.CDSAccount
import models.{EmptyAccountsError, ErrorResponse, Mode, ShowBalance}
import navigation.Navigator
import pages.add._
import play.api.Logging
import play.api.i18n._
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ShowBalanceView

import javax.inject.Inject
import scala.concurrent._

class ShowBalanceController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: ShowBalanceFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ShowBalanceView
                                     )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
  extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val accountsLength = getAccountsLength(request.userAnswers.get(AccountsPage))

      val preparedForm = request.userAnswers.get(ShowBalancePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      accountsLength match {
        case Right(noOfAccounts) =>
          Ok(view(
            preparedForm,
            noOfAccounts,
            mode,
            navigator.backLinkRouteForShowBalancePage(mode, request.userAnswers))
          )

        case Left(emptyError) => errorPage(emptyError.msg)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          getAccountsLength(request.userAnswers.get(AccountsPage)) match {
            case Right(noOfAccounts) => Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  noOfAccounts,
                  mode, navigator.backLinkRouteForShowBalancePage(mode, request.userAnswers))
              )
            )

            case Left(emptyError) => Future.successful(errorPage(emptyError.msg))
          }
        },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ShowBalancePage, value)(ShowBalance.writes))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ShowBalancePage, mode, updatedAnswers))
      )
  }

  private def getAccountsLength(maybeAccounts: Option[List[CDSAccount]]): Either[ErrorResponse, Int] = {
    maybeAccounts match {
      case Some(accounts) => Right(accounts.size)
      case None => Left(EmptyAccountsError)
    }
  }

  private def errorPage(msg: String): Result = {
    logger.error(msg)
    Redirect(controllers.routes.TechnicalDifficulties.onPageLoad)
  }

}
