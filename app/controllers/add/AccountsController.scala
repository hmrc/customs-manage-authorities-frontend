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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.AccountsFormProvider
import models.domain.CDSAccount
import models.requests.DataRequest
import models.{AuthorisedAccounts, EoriDetailsCorrect, Mode, NormalMode}
import navigation.Navigator
import pages.add.{AccountsPage, EoriDetailsCorrectPage, EoriNumberPage}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AuthorisedAccountsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{AccountsView, NoAvailableAccountsView, ServiceUnavailableView}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class AccountsController @Inject()(
                                    override val messagesApi: MessagesApi,
                                    sessionRepository: SessionRepository,
                                    navigator: Navigator,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    authorisedAccountsService: AuthorisedAccountsService,
                                    formProvider: AccountsFormProvider,
                                    requireData: DataRequiredAction,
                                    serviceUnavailable: ServiceUnavailableView,
                                    noAvailableAccounts: NoAvailableAccountsView,
                                    val controllerComponents: MessagesControllerComponents,
                                    view: AccountsView
                                  )(implicit appConfig: FrontendAppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  val log = Logger(this.getClass)
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(EoriNumberPage) match {
        case None => Future.successful(Redirect(controllers.add.routes.EoriNumberController.onPageLoad(NormalMode)))
        case Some(enteredEori) =>
          authorisedAccountsService.getAuthorisedAccounts(enteredEori.eori).map { authorisedAccounts =>
            if (authorisedAccounts.availableAccounts.nonEmpty) {

              if(enteredEori.eori.contains("GB")) {
                val accounts: AuthorisedAccounts = getAuthorisedAccountsList(authorisedAccounts,
                  authorisedAccounts.availableAccounts.filter(x => !x.isNiAccount))

                Ok(view(populateForm(authorisedAccounts.availableAccounts.filter(x=> !x.isNiAccount)), accounts, mode,
                  navigator.backLinkRoute(mode, controllers.add.routes.EoriNumberController.onPageLoad(mode)))
                )
              } else {
                val accounts: AuthorisedAccounts = getAuthorisedAccountsList(authorisedAccounts,
                  authorisedAccounts.availableAccounts.filter(x => x.isNiAccount || x.accountType.equals("cash") || x.accountType.equals("generalGuarantee")))
                Ok(view(populateForm(authorisedAccounts.availableAccounts
                  .filter(x => x.isNiAccount || x.accountType.equals("cash") || x.accountType.equals("generalGuarantee"))), accounts, mode,
                  navigator.backLinkRoute(mode, controllers.add.routes.EoriNumberController.onPageLoad(mode)))
                )
              }

            } else {
              Ok(noAvailableAccounts(authorisedAccounts.enteredEori))
            }
          }.recover {
            case _ => InternalServerError(serviceUnavailable())
          }
      }
  }

  def getAuthorisedAccountsList(accounts: AuthorisedAccounts,
    cdsAcc: Seq[CDSAccount]): AuthorisedAccounts = AuthorisedAccounts(
    accounts.alreadyAuthorisedAccounts, cdsAcc,
    accounts.closedAccounts, accounts.pendingAccounts, accounts.enteredEori)

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(EoriNumberPage) match {
        case None => Future.successful(Redirect(controllers.add.routes.EoriNumberController.onPageLoad(NormalMode)))
        case Some(enteredEori) =>
          authorisedAccountsService.getAuthorisedAccounts(enteredEori.eori).flatMap { authorisedAccounts =>
            form.bindFromRequest().fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, authorisedAccounts, mode, navigator.backLinkRoute(mode, controllers.add.routes.EoriNumberController.onPageLoad(mode))))),
              value => {
                val selected = {
                  if(enteredEori.eori.startsWith("XI")){
                    value.map(account => authorisedAccounts.availableAccounts.filter(x => x.isNiAccount || x.accountType.equals("cash") || x.accountType.equals("generalGuarantee"))
                    (account.replace("account_", "").toInt))
                  } else {
                    value.map(account => authorisedAccounts.availableAccounts.filter(x => !x.isNiAccount)
                    (account.replace("account_", "").toInt))
                  }
                }
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(AccountsPage, selected))
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AccountsPage, mode, updatedAnswers))
              }
            )
          }
      }
  }

  private def populateForm(availableAccounts: Seq[CDSAccount])(implicit request: DataRequest[_]): Form[List[String]] = {
    (request.userAnswers.get(AccountsPage), request.userAnswers.get(EoriDetailsCorrectPage)) match {
      case (None, _) => form
      case (Some(value), Some(EoriDetailsCorrect.No)) =>
        form
      case (Some(value), _) =>
        val formValues = availableAccounts
          .toList
          .zipWithIndex
          .filter { case (account, _) => value.contains(account) }
          .map { case (_, index) => s"account_$index" }
        form.fill(formValues)
    }
  }
}
