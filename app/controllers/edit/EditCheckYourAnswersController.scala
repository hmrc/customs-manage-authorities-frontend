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

package controllers.edit

import config.FrontendAppConfig
import connectors.{CustomsDataStoreConnector, CustomsFinancialsConnector}
import controllers.actions.*
import controllers.grantAccountAuthRequestList
import models.domain.AccountWithAuthoritiesWithId
import models.requests.{AddAuthorityRequest, DataRequest, GrantAccountAuthorityRequest}
import models.{ErrorResponse, MissingAccountError, MissingAuthorityError, NormalMode, UserAnswers}
import navigation.Navigator
import pages.edit.*
import play.api.Logging
import play.api.i18n.*
import play.api.mvc.*
import services.*
import services.edit.EditAuthorityValidationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.StringUtils.{emptyString, nIEORIPrefix}
import viewmodels.CheckYourAnswersEditHelper
import views.html.edit.EditCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.*
import scala.concurrent.duration.Duration

class EditCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  service: AuthoritiesCacheService,
  connector: CustomsFinancialsConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  dateTimeService: DateTimeService,
  editAuthorityValidationService: EditAuthorityValidationService,
  view: EditCheckYourAnswersView,
  navigator: Navigator,
  implicit val controllerComponents: MessagesControllerComponents,
  dataStore: CustomsDataStoreConnector
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  lazy val commonActions: ActionBuilder[DataRequest, AnyContent] = identify andThen getData andThen requireData

  def onPageLoad(accountId: String, authorityId: String): Action[AnyContent] = commonActions.async { implicit request =>
    service.getAccountAndAuthority(request.internalId, authorityId, accountId).map {
      case Left(NoAuthority)                              => errorPage(MissingAuthorityError)
      case Left(NoAccount)                                => errorPage(MissingAccountError)
      case Right(AccountAndAuthority(account, authority)) =>
        val companyName =
          Await.result(dataStore.retrieveCompanyInformationThirdParty(authority.authorisedEori), Duration.Inf)
        val helper      = new CheckYourAnswersEditHelper(
          request.userAnswers,
          accountId,
          authorityId,
          dateTimeService,
          authority,
          account,
          companyName
        )

        Ok(view(helper, accountId, authorityId))
    }
  }

  def onSubmit(accountId: String, authorityId: String): Action[AnyContent] =
    commonActions.async { implicit request =>
      service.getAccountAndAuthority(request.internalId, authorityId, accountId).flatMap {
        case Left(NoAuthority) => Future.successful(errorPage(MissingAuthorityError))
        case Left(NoAccount)   => Future.successful(errorPage(MissingAccountError))

        case Right(AccountAndAuthority(account, authority)) =>
          for {
            xiEori <- dataStore.getXiEori(request.eoriNumber)
            result <- doSubmission(
                        request.userAnswers,
                        accountId,
                        authorityId,
                        authority.authorisedEori,
                        account,
                        xiEori.getOrElse(emptyString),
                        request.eoriNumber
                      )
          } yield result
      }
    }

  private def doSubmission(
    userAnswers: UserAnswers,
    accountId: String,
    authorityId: String,
    authorisedEori: String,
    account: AccountWithAuthoritiesWithId,
    xiEori: String,
    eori: String
  )(implicit hc: HeaderCarrier): Future[Result] = {

    val ownerEori = if (authorisedEori.startsWith(nIEORIPrefix)) xiEori else eori

    editAuthorityValidationService.validate(
      userAnswers,
      accountId,
      authorityId,
      authorisedEori,
      account,
      ownerEori
    ) match {
      case Right(payload) =>
        if (authorisedEori.startsWith(nIEORIPrefix)) {
          processPayloadForXIEori(userAnswers, xiEori, eori, payload, accountId, authorityId)
        } else {
          connector.grantAccountAuthorities(payload).map {
            case true  =>
              Redirect(navigator.nextPage(EditCheckYourAnswersPage(accountId, authorityId), NormalMode, userAnswers))
            case false =>
              logger.error("Edit authority request submission to backend failed")
              Redirect(controllers.routes.TechnicalDifficulties.onPageLoad)
          }
        }

      case _ =>
        logger.error("UserAnswers did not contain sufficient data to construct add authority request")
        Future.successful(Redirect(controllers.routes.TechnicalDifficulties.onPageLoad))
    }
  }

  /** Sends two calls to grant authority if Accounts has both DD (with XI ownerEORI ) and Cash/Guarantee accounts (with
    * GB EORI as ownerEORI) Sends only one call if Accounts has only DD account
    */
  private def processPayloadForXIEori(
    userAnswers: UserAnswers,
    xiEori: String,
    gbEori: String,
    payload: AddAuthorityRequest,
    accountId: String,
    authorityId: String
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val grantAccAuthRequests: Seq[GrantAccountAuthorityRequest] = grantAccountAuthRequestList(payload, xiEori, gbEori)

    for {
      result <- Future.sequence(grantAccAuthRequests.map { req =>
                  connector.grantAccountAuthorities(req.payload.copy(ownerEori = req.ownerEori))
                })
    } yield
      if (result.contains(false)) {
        logger.error("Edit authority request submission to backend failed")
        Redirect(controllers.routes.TechnicalDifficulties.onPageLoad)
      } else {
        Redirect(navigator.nextPage(EditCheckYourAnswersPage(accountId, authorityId), NormalMode, userAnswers))
      }
  }

  private def errorPage(error: ErrorResponse): Result = {
    logger.error(error.msg)
    Redirect(controllers.routes.TechnicalDifficulties.onPageLoad)
  }
}
