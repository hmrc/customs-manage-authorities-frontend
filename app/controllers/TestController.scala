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

package controllers

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import connectors.{CustomsDataStoreConnector, CustomsFinancialsConnector}
import controllers.actions._
import forms.AuthorisedUserFormProvider
import models.domain.{AccountWithAuthoritiesWithId, AuthorisedUser, StandingAuthority, UnknownAccount}
import models.requests.OptionalDataRequest
import models.{AuthorityEnd, AuthorityStart, NormalMode, UserAnswers}
import navigation.Navigator
import pages.edit._
import play.api.Logging
import play.api.data.Form
import play.api.i18n._
import play.api.mvc._
import repositories.SessionRepository
import services._
import services.edit.EditAuthorityValidationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.{FrontendBaseController, FrontendController}
import viewmodels.CheckYourAnswersEditHelper
import views.html.{EditOrRemoveView, ServiceUnavailableView}
import views.html.edit.EditAuthorisedUserView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent._

class TestController @Inject()(view: EditOrRemoveView,
                               serviceUnavailableView: ServiceUnavailableView,
                               mcc: MessagesControllerComponents,
                               authoritiesCacheService: AuthoritiesCacheService,
                               editSessionService: EditSessionService,
                               dateTimeService: DateTimeService,
                               customsDataStoreConnector: CustomsDataStoreConnector,
                               identify: IdentifierAction,
                               getData: DataRetrievalAction,
                               requireData: DataRequiredAction
                              )(implicit executionContext: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  //TODO: Check to see if works with require data

  def test(accountId: String, authorityId: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    lazy val noAccount = AccountWithAuthoritiesWithId(UnknownAccount, "", None, Map.empty)
    lazy val noAuthorities = StandingAuthority("", LocalDate.now(), None, viewBalance = false)

    for {
      accountsWithAuthorities <- authoritiesCacheService.retrieveAuthorities(request.internalId)
      account = accountsWithAuthorities.authorities.getOrElse(accountId, noAccount)
      authority = account.authorities.getOrElse(authorityId, noAuthorities)
      userAnswers = request.userAnswers.getOrElse(UserAnswers(request.internalId.value))
      checkYourAnswersEditHelper <- editSessionService.populateUserAnswers(accountId, authorityId, userAnswers, authority, account)
      result <- validateData(checkYourAnswersEditHelper, accountId, authorityId, authority, account)
    } yield result
  }

  private def validateData(checkYourAnswersEditHelper: CheckYourAnswersEditHelper,
                           accountId: String,
                           authorityId: String,
                           authority: StandingAuthority,
                           account: AccountWithAuthoritiesWithId)(implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    val userAnswers = checkYourAnswersEditHelper.userAnswers

    (for {
      editAuthorityStartPage <- userAnswers.get(EditAuthorityStartPage(accountId, authorityId))
      editAuthorityEndPage <- userAnswers.get(EditAuthorityEndPage(accountId, authorityId))
      _ <- userAnswers.get(EditShowBalancePage(accountId, authorityId))
      editAuthorityStartDatePage = userAnswers.get(EditAuthorityStartDatePage(accountId, authorityId))
      editAuthorityEndDatePage = userAnswers.get(EditAuthorityEndDatePage(accountId, authorityId))
      result <- Some((editAuthorityStartPage, editAuthorityEndPage) match {
        case (AuthorityStart.Setdate, _) if editAuthorityStartDatePage.isEmpty =>
          editSessionService.resetStartAnswers(userAnswers, accountId, authorityId, authority).map(answers =>
            Ok(view(new CheckYourAnswersEditHelper(answers, accountId, authorityId, dateTimeService, authority, account), accountId, authorityId))
          )
        case (_, AuthorityEnd.Setdate) if editAuthorityEndDatePage.isEmpty =>
          editSessionService.resetEndAnswers(userAnswers, accountId, authorityId, authority).map(answers =>
            Ok(view(new CheckYourAnswersEditHelper(answers, accountId, authorityId, dateTimeService, authority, account), accountId, authorityId))
          )
        case _ => getViewPage(authority, userAnswers, accountId, authorityId, account, checkYourAnswersEditHelper)
      })
    } yield result).getOrElse(Future.successful(InternalServerError(serviceUnavailableView())))
  }

  private def getViewPage(authority: StandingAuthority, userAnswers: UserAnswers, accountId: String, authorityId: String, account: AccountWithAuthoritiesWithId, checkYourAnswersEditHelper: CheckYourAnswersEditHelper)(implicit request: Request[_]) = {
    val edit = !authority.canEditStartDate(dateTimeService.localDate())
    val startChanged = authority.startChanged(userAnswers, accountId, authorityId, dateTimeService.localDate())
    (edit, startChanged) match {
      case (_, Left(err)) => Future.successful(Redirect(controllers.routes.TechnicalDifficulties.onPageLoad()))
      case (true, Right(true)) => editSessionService.resetStartAnswers(userAnswers, accountId, authorityId, authority).map(answers =>
        Ok(view(new CheckYourAnswersEditHelper(answers, accountId, authorityId, dateTimeService, authority, account), accountId, authorityId))
      )
      case (_, _) => Future.successful(Ok(view(checkYourAnswersEditHelper, accountId, authorityId)))
    }
  }

}
