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

package controllers.add

import config.FrontendAppConfig
import connectors.{CustomsDataStoreConnector, CustomsFinancialsConnector}
import controllers.actions._
import forms.EoriNumberFormProvider
import models.requests.OptionalDataRequest
import models.{CompanyDetails, EoriDetailsCorrect, Mode, UserAnswers}
import navigation.Navigator
import pages.add.{AccountsPage, EoriDetailsCorrectPage, EoriNumberPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.StringUtils.{emptyString, isXIEori, removeSpaceAndConvertToUpperCase}
import views.html.add.EoriNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success
import scala.xml.dtd.ValidationException

class EoriNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  formProvider: EoriNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: EoriNumberView,
  connector: CustomsFinancialsConnector,
  dataStore: CustomsDataStoreConnector
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(EoriNumberPage)) match {
      case None        => form
      case Some(value) => form.fill(value.eori)
    }

    val isXiEoriEnabled = appConfig.xiEoriEnabled
    val isEuEoriEnabled = appConfig.euEoriEnabled

    Ok(view(preparedForm, mode, navigator.backLinkRouteForEORINUmberPage(mode), isXiEoriEnabled, isEuEoriEnabled))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              view(
                formWithErrors,
                mode,
                navigator.backLinkRouteForEORINUmberPage(mode),
                appConfig.xiEoriEnabled,
                appConfig.euEoriEnabled
              )
            )
          ),
        eoriNumber => processValidInput(mode, eoriNumber)
      )
  }

  private def processValidInput(
    mode: Mode,
    inputEoriNumber: String
  )(implicit
    request: OptionalDataRequest[AnyContent],
    appConfig: FrontendAppConfig,
    hc: HeaderCarrier,
    msgs: Messages
  ): Future[Result] = {
    val eori = removeSpaceAndConvertToUpperCase(inputEoriNumber)

    if (request.eoriNumber.equalsIgnoreCase(eori)) {
      errorView(mode, inputEoriNumber, "eoriNumber.error.authorise-own-eori")
    } else {
      val result = for {
        xiEori: Option[String] <- dataStore.getXiEori(request.eoriNumber)
      } yield performXiEoriChecks(xiEori, inputEoriNumber, eori, mode)

      result.flatten
    }
  }

  private def performXiEoriChecks(
    xiEoriNumber: Option[String],
    inputEoriNumber: String,
    eoriInUpperCase: String,
    mode: Mode
  )(implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier, msgs: Messages): Future[Result] =
    (xiEoriNumber, eoriInUpperCase) match {
      case (xiEori, inputEori) if isOwnXiEori(xiEori, inputEori) =>
        errorView(mode, inputEoriNumber, "eoriNumber.error.authorise-own-eori")
      case _                                                     =>
        processValidEoriAndSubmit(mode, eoriInUpperCase)
    }

  private def processValidEoriAndSubmit(
    mode: Mode,
    eori: String
  )(implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier, msgs: Messages): Future[Result] =
    (for {
      companyName                         <- dataStore.retrieveCompanyInformationThirdParty(eori)
      companyDetails                       = CompanyDetails(eori, companyName)
      updatedAnswers                      <- Future.fromTry(
                                               request.userAnswers
                                                 .getOrElse(UserAnswers(request.internalId.value))
                                                 .set(EoriNumberPage, companyDetails)
                                             )
      updatedAnswersWithRefreshedAccounts <-
        refreshAccountsInChangeMode(eori, eoriFromUserAnswers(request), updatedAnswers)
      _                                   <- sessionRepository.set(updatedAnswersWithRefreshedAccounts)
      result                              <- doSubmission(updatedAnswers, eori, mode)
    } yield result).recover {
      case _: ValidationException =>
        BadRequest(
          view(
            form.withError("value", "eoriNumber.error.invalid").fill(eori),
            mode,
            navigator.backLinkRouteForEORINUmberPage(mode),
            appConfig.xiEoriEnabled,
            appConfig.euEoriEnabled
          )
        )
      case _                      =>
        Redirect(controllers.routes.TechnicalDifficulties.onPageLoad)
    }

  private def doSubmission(
    updatedAnswers: UserAnswers,
    eori: String,
    mode: Mode
  )(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] =
    connector.validateEori(eori).map {
      case Right(true) =>
        Redirect(navigator.nextPage(EoriNumberPage, mode, updatedAnswers))
      case _           =>
        BadRequest(
          view(
            form.withError("value", "eoriNumber.error.invalid").fill(eori),
            mode,
            navigator.backLinkRouteForEORINUmberPage(mode),
            appConfig.xiEoriEnabled,
            appConfig.euEoriEnabled
          )
        )
    }

  private def errorView(
    mode: Mode,
    inputEoriNumber: String,
    errorMsgKey: String
  )(implicit request: Request[_], msgs: Messages, appConfig: FrontendAppConfig): Future[Result] =
    Future.successful(
      BadRequest(
        view(
          form.withError("value", errorMsgKey).fill(inputEoriNumber),
          mode,
          navigator.backLinkRouteForEORINUmberPage(mode),
          appConfig.xiEoriEnabled,
          appConfig.euEoriEnabled
        )
      )
    )

  private def eoriFromUserAnswers(request: OptionalDataRequest[AnyContent]): String =
    request.userAnswers
      .getOrElse(UserAnswers(request.internalId.value))
      .get(EoriNumberPage)
      .getOrElse(CompanyDetails(emptyString, None))
      .eori

  private def refreshAccountsInChangeMode(
    requestEori: String,
    eoriFromUserAnswers: String,
    userAnswers: UserAnswers
  ): Future[UserAnswers] =
    Future {
      if (!requestEori.equals(eoriFromUserAnswers)) {
        userAnswers.set(AccountsPage, List()) match {
          case Success(value) => value.set(EoriDetailsCorrectPage, EoriDetailsCorrect.No).getOrElse(value)
          case _              => userAnswers
        }
      } else {
        userAnswers
      }
    }

  private def isOwnXiEori(xiEori: Option[String], inputEori: String): Boolean =
    xiEori.nonEmpty && isXIEori(inputEori) && inputEori.equals(xiEori.getOrElse(emptyString))
}
