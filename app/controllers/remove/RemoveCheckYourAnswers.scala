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

package controllers.remove

import cats.data.EitherT
import cats.data.EitherT.{fromOption, fromOptionF, liftF}
import connectors.{CustomsDataStoreConnector, CustomsFinancialsConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.domain.{AccountWithAuthoritiesWithId, AuthoritiesWithId, StandingAuthority}
import models.requests.{DataRequest, RevokeAuthorityRequest}
import models.{ErrorResponse, MissingAccountError, MissingAuthorisedUser, MissingAuthorityError, SubmissionError}
import pages.remove.RemoveAuthorisedUserPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.AuthoritiesCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.CheckYourAnswersRemoveHelper
import views.html.remove.RemoveCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveCheckYourAnswers @Inject()(identify: IdentifierAction,
                                       view: RemoveCheckYourAnswersView,
                                       authoritiesCacheService: AuthoritiesCacheService,
                                       customsFinancialsConnector: CustomsFinancialsConnector,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       mcc: MessagesControllerComponents
                                      )(implicit executionContext: ExecutionContext) extends FrontendController(mcc) with Logging with I18nSupport {

  case class Details(account: AccountWithAuthoritiesWithId, authority: StandingAuthority)

  def onPageLoad(accountId: String, authorityId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    authoritiesCacheService.retrieveAuthorities(request.internalId).flatMap { accountsWithAuthorities: AuthoritiesWithId =>
      accountsWithAuthorities.authorities.get(accountId).map { account =>
        account.authorities.get(authorityId).map { authority =>
          request.userAnswers.get(RemoveAuthorisedUserPage(accountId, authorityId)) match {
            case Some(value) =>
              Future.successful(Ok(view(new CheckYourAnswersRemoveHelper(request.userAnswers, accountId, authorityId, value, authority, account), accountId, authorityId)))
            case None => Future.successful(Redirect(controllers.routes.ViewAuthorityController.onPageLoad(accountId, authorityId)))
          }
        }.getOrElse(Future.successful(errorPage(MissingAuthorityError)))
      }.getOrElse(Future.successful(errorPage(MissingAccountError)))
    }
  }

  def onSubmit(accountId: String, authorityId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      authoritiesCacheService.retrieveAuthorities(request.internalId).flatMap { accountsWithAuthorities: AuthoritiesWithId =>
        accountsWithAuthorities.authorities.get(accountId).map { account =>
          account.authorities.get(authorityId).map { authority =>
            request.userAnswers.get(RemoveAuthorisedUserPage(accountId, authorityId)).map { authorisedUser =>
              val revokeRequest = RevokeAuthorityRequest(account.accountNumber, account.accountType, authority.authorisedEori, authorisedUser)
              doSubmission(revokeRequest, accountId, authorityId)
            }.getOrElse(Future.successful(errorPage(MissingAuthorisedUser)))
          }.getOrElse(Future.successful(errorPage(MissingAuthorityError)))
        }.getOrElse(Future.successful(errorPage(MissingAccountError)))
      }
  }

  def doSubmission(revokeRequest: RevokeAuthorityRequest, accountId: String, authorityId: String)(implicit hc: HeaderCarrier): Future[Result] = {
    customsFinancialsConnector.revokeAccountAuthorities(revokeRequest).map {
      case true => Redirect(routes.RemoveConfirmationController.onPageLoad(accountId, authorityId))
      case false => errorPage(SubmissionError)
    }
  }

  private def errorPage(error: ErrorResponse): Result = {
    logger.error(error.msg)
    Redirect(controllers.routes.TechnicalDifficulties.onPageLoad())
  }
}
