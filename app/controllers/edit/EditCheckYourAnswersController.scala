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

import connectors.CustomsFinancialsConnector
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
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CheckYourAnswersEditHelper
import views.html.ServiceUnavailableView
import views.html.edit.EditAuthorisedUserView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent._

class EditCheckYourAnswersController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                service: AuthoritiesCacheService,
                                                connector: CustomsFinancialsConnector,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: AuthorisedUserFormProvider,
                                                val dateTimeService: DateTimeService,
                                                editAuthorityValidationService: EditAuthorityValidationService,
                                                serviceUnavailable: ServiceUnavailableView,
                                                sessionRepository: SessionRepository,
                                                editView: EditAuthorisedUserView,
                                                editSessionService: EditSessionService,
                                                navigator: Navigator,
                                                implicit val controllerComponents: MessagesControllerComponents,

                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  val form: Form[AuthorisedUser] = formProvider()

  lazy val noAccount = AccountWithAuthoritiesWithId(UnknownAccount, "", None, Map.empty)
  lazy val noAuthorities = StandingAuthority("", LocalDate.now(), None, false)

  def onPageLoad( accountId: String, authorityId: String ): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      for {
        accountsWithAuthorities <- service.retrieveAuthorities(request.internalId)
        account = accountsWithAuthorities.authorities.getOrElse(accountId, noAccount)
        authority = account.authorities.getOrElse(authorityId, noAuthorities)
        userAnswers = request.userAnswers.getOrElse(UserAnswers(request.internalId.value))
        checkYourAnswersEditHelper <- editSessionService.populateUserAnswers(accountId, authorityId, userAnswers, authority, account)
        result <- validateData(checkYourAnswersEditHelper, accountId, authorityId, authority, account)
      } yield result
  }

  private def getEditPage( authority: StandingAuthority, userAnswers: UserAnswers, accountId: String, authorityId: String, account: AccountWithAuthoritiesWithId )( implicit hc: HeaderCarrier ) = {
    val edit = !authority.canEditStartDate(dateTimeService.localDate())
    val startChanged = authority.startChanged(userAnswers, accountId, authorityId, dateTimeService.localDate())
    (edit, startChanged) match {
      case (_, Left(err)) => Future.successful(Redirect(controllers.routes.TechnicalDifficulties.onPageLoad()))
      case (true, Right(true)) => Future.successful(Redirect(controllers.edit.routes.EditCheckYourAnswersController.onPageLoad(accountId, authorityId)))
      case (_, _) => doSubmission(userAnswers, accountId, authorityId, authority.authorisedEori, account)
    }
  }

  def onSubmit( accountId: String, authorityId: String ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        for {
          accountsWithAuthorities <- service.retrieveAuthorities(request.internalId)
          account = accountsWithAuthorities.authorities.getOrElse(accountId, noAccount)
          authority = account.authorities.getOrElse(authorityId, noAuthorities)
          result <- form.bindFromRequest().fold(
            formWithErrors => {
              val helper = new CheckYourAnswersEditHelper(request.userAnswers, accountId, authorityId, dateTimeService, authority, account)
              Future.successful(BadRequest(editView(formWithErrors, helper, accountId, authorityId)))
            },
            value =>
              (for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(EditAuthorisedUserPage(accountId, authorityId), value))
                _ <- sessionRepository.set(updatedAnswers)
                result <- getEditPage(authority, updatedAnswers, accountId, authorityId, account)
              } yield result).recover {
                case _ => Redirect(controllers.routes.TechnicalDifficulties.onPageLoad())
              }
          )
        } yield result
    }

  private def validateData( checkYourAnswersEditHelper: CheckYourAnswersEditHelper,
                            accountId: String,
                            authorityId: String,
                            authority: StandingAuthority,
                            account: AccountWithAuthoritiesWithId )( implicit request: OptionalDataRequest[AnyContent] ): Future[Result] = {
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
            Ok(editView(form, new CheckYourAnswersEditHelper(answers, accountId, authorityId, dateTimeService, authority, account), accountId, authorityId))
          )
        case (_, AuthorityEnd.Setdate) if editAuthorityEndDatePage.isEmpty =>
          editSessionService.resetEndAnswers(userAnswers, accountId, authorityId, authority).map(answers =>
            Ok(editView(form, new CheckYourAnswersEditHelper(answers, accountId, authorityId, dateTimeService, authority, account), accountId, authorityId))
          )
        case _ => getViewPage(authority, userAnswers, accountId, authorityId, account, checkYourAnswersEditHelper)
      })
    } yield result).getOrElse(Future.successful(InternalServerError(serviceUnavailable())))
  }

  private def getViewPage( authority: StandingAuthority, userAnswers: UserAnswers, accountId: String, authorityId: String, account: AccountWithAuthoritiesWithId, checkYourAnswersEditHelper: CheckYourAnswersEditHelper )( implicit request: Request[_]) = {
    val edit = !authority.canEditStartDate(dateTimeService.localDate())
    val startChanged = authority.startChanged(userAnswers, accountId, authorityId, dateTimeService.localDate())
    (edit, startChanged) match {
      case (_, Left(err)) => Future.successful(Redirect(controllers.routes.TechnicalDifficulties.onPageLoad()))
      case (true, Right(true)) => editSessionService.resetStartAnswers(userAnswers, accountId, authorityId, authority).map(answers =>
        Ok(editView(form, new CheckYourAnswersEditHelper(answers, accountId, authorityId, dateTimeService, authority, account), accountId, authorityId))
      )
      case (_, _) => Future.successful(Ok(editView(form, checkYourAnswersEditHelper, accountId, authorityId)))
    }
  }

  private def doSubmission( userAnswers: UserAnswers,
                            accountId: String,
                            authorityId: String,
                            authorisedEori: String,
                            account: AccountWithAuthoritiesWithId )( implicit hc: HeaderCarrier ): Future[Result] = {
    editAuthorityValidationService.validate(userAnswers, accountId, authorityId, authorisedEori, account)
    match {
      case Right(payload) => connector.grantAccountAuthorities(payload).map {
        case true => Redirect(navigator.nextPage(EditAuthorisedUserPage(accountId, authorityId), NormalMode, userAnswers))
        case false => {
          logger.error("Edit authority request submission to backend failed")
          Redirect(controllers.routes.TechnicalDifficulties.onPageLoad())
        }
      }
      case _ => {
        logger.error("UserAnswers did not contain sufficient data to construct add authority request")
        Future.successful(Redirect(controllers.routes.TechnicalDifficulties.onPageLoad()))
      }
    }
  }

}
