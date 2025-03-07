/*
 * Copyright 2024 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.{CustomsDataStoreConnector, CustomsFinancialsConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.domain.{AccountType, CdsCashAccount, CdsGeneralGuaranteeAccount}
import models.requests.RevokeAuthorityRequest
import models.{ErrorResponse, MissingAccountError, MissingAuthorisedUser, MissingAuthorityError, SubmissionError}
import pages.remove.RemoveAuthorisedUserPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{AccountAndAuthority, AuthoritiesCacheService, NoAccount, NoAuthority}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.StringUtils.{emptyString, nIEORIPrefix}
import viewmodels.CheckYourAnswersRemoveHelper
import views.html.remove.RemoveCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveCheckYourAnswers @Inject() (
  identify: IdentifierAction,
  view: RemoveCheckYourAnswersView,
  authoritiesCacheService: AuthoritiesCacheService,
  customsFinancialsConnector: CustomsFinancialsConnector,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  mcc: MessagesControllerComponents,
  dataStore: CustomsDataStoreConnector
)(implicit executionContext: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendController(mcc)
    with Logging
    with I18nSupport {

  def onPageLoad(accountId: String, authorityId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      authoritiesCacheService.getAccountAndAuthority(request.internalId, authorityId, accountId).map {
        case Left(NoAccount)                                => errorPage(MissingAccountError)
        case Left(NoAuthority)                              => errorPage(MissingAuthorityError)
        case Right(AccountAndAuthority(account, authority)) =>
          request.userAnswers.get(RemoveAuthorisedUserPage(accountId, authorityId)) match {
            case Some(authorisedUser) =>
              Ok(
                view(
                  new CheckYourAnswersRemoveHelper(
                    request.userAnswers,
                    accountId,
                    authorityId,
                    authorisedUser,
                    authority,
                    account,
                    dataStore
                  )
                )
              )
            case None                 => Redirect(controllers.routes.ViewAuthorityController.onPageLoad(accountId, authorityId))
          }
      }
    }

  def onSubmit(accountId: String, authorityId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      authoritiesCacheService.getAccountAndAuthority(request.internalId, authorityId, accountId).flatMap {
        case Left(NoAccount)   => Future.successful(errorPage(MissingAccountError))
        case Left(NoAuthority) => Future.successful(errorPage(MissingAuthorityError))

        case Right(AccountAndAuthority(account, authority)) =>
          for {
            xiEori <- dataStore.getXiEori
            result <- request.userAnswers
                        .get(RemoveAuthorisedUserPage(accountId, authorityId))
                        .map { authorisedUser =>
                          val revokeRequest = RevokeAuthorityRequest(
                            account.accountNumber,
                            account.accountType,
                            authority.authorisedEori,
                            authorisedUser
                          )
                          doSubmission(
                            revokeRequest,
                            accountId,
                            authorityId,
                            xiEori.getOrElse(emptyString),
                            request.eoriNumber
                          )
                        }
                        .getOrElse(Future.successful(errorPage(MissingAuthorisedUser)))
          } yield result
      }
    }

  def doSubmission(
    revokeRequest: RevokeAuthorityRequest,
    accountId: String,
    authorityId: String,
    xiEori: String,
    eori: String
  )(implicit hc: HeaderCarrier): Future[Result] = {

    val ownerEori = ownerEoriForAccountType(revokeRequest.accountType, revokeRequest.authorisedEori, xiEori, eori)

    customsFinancialsConnector.revokeAccountAuthorities(revokeRequest, ownerEori).map {
      case true  => Redirect(routes.RemoveConfirmationController.onPageLoad(accountId, authorityId))
      case false => errorPage(SubmissionError)
    }
  }

  private def ownerEoriForAccountType(
    account: AccountType,
    authorisedEori: String,
    xiEori: String,
    eori: String
  ): String =
    if (authorisedEori.startsWith(nIEORIPrefix)) {
      account match {
        case accn if accn == CdsCashAccount || accn == CdsGeneralGuaranteeAccount => eori
        case _                                                                    => xiEori
      }
    } else {
      eori
    }

  private def errorPage(error: ErrorResponse): Result = {
    logger.error(error.msg)
    Redirect(controllers.routes.TechnicalDifficulties.onPageLoad)
  }
}
