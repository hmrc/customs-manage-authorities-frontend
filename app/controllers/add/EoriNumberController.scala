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
import scala.util.{Success, Try}
import scala.xml.dtd.ValidationException

class EoriNumberController @Inject()(
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
                                    )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.get(EoriNumberPage)) match {
        case None => form
        case Some(value) => form.fill(value.eori)
      }

      val isXiEoriEnabled = appConfig.xiEoriEnabled
      Ok(view(preparedForm, mode, navigator.backLinkRouteForEORINUmberPage(mode), isXiEoriEnabled))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
            Future.successful(BadRequest(view(formWithErrors, mode, navigator.backLinkRouteForEORINUmberPage(mode), appConfig.xiEoriEnabled)))
        },
        eoriNumber => {
          processValidInput(mode, request, eoriNumber)(appConfig, hc, request2Messages)
        }
      )
  }

  private def processValidInput(mode: Mode,
                                request: OptionalDataRequest[AnyContent],
                                inputEoriNumber: String)(implicit appConfig: FrontendAppConfig,
                                                         hc: HeaderCarrier, msgs: Messages): Future[Result] = {
    val eori = removeSpaceAndConvertToUpperCase(inputEoriNumber)

    if (request.eoriNumber.equalsIgnoreCase(eori)) {
      errorView(mode, inputEoriNumber, "eoriNumber.error.authorise-own-eori")(request, msgs, appConfig)
    } else {
      val result = for {
        xiEori: Option[String] <- dataStore.getXiEori(request.eoriNumber)
      } yield {
        performXiEoriChecks(xiEori,
          inputEoriNumber,
          eori,
          mode,
          request,
          hc,
          msgs)
      }

      result.flatten
    }
  }

  /**
   * Performs below checks
   * 1.Whether associated XI EORI is empty and input EORI is XI EORI
   * 2.Whether input XI EORI is user's own XI EORI
   * if above checks match, error is raised with relevant msg
   * else proceed to submission
   */
  private def performXiEoriChecks(xiEoriNumber: Option[String],
                                  inputEoriNumber: String,
                                  eoriInUpperCase: String,
                                  mode: Mode,
                                  request: OptionalDataRequest[AnyContent],
                                  hc: HeaderCarrier,
                                  msgs: Messages): Future[Result] =
    (xiEoriNumber, eoriInUpperCase) match {
      case (xiEori, inputEori) if isNotRegisteredForXiEori(xiEori, inputEori) =>
        errorView(mode, inputEoriNumber, "eoriNumber.error.register-xi-eori")(request, msgs, appConfig)
      case (xiEori, inputEori) if isOwnXiEori(xiEori, inputEori) =>
        errorView(mode, inputEoriNumber, "eoriNumber.error.authorise-own-eori")(request, msgs, appConfig)
      case _ =>
        processValidEoriAndSubmit(mode, request, hc, msgs, eoriInUpperCase)
    }

  /**
   * Processes valid EORI
   */
  private def processValidEoriAndSubmit(mode: Mode,
                                        request: OptionalDataRequest[AnyContent],
                                        hc: HeaderCarrier,
                                        msgs: Messages,
                                        eori: String): Future[Result] = {
    (for {
      companyName <- dataStore.getCompanyName(eori)(hc)
      companyDetails = CompanyDetails(eori, companyName)
      updatedAnswers <- Future.fromTry(
        request.userAnswers.getOrElse(UserAnswers(request.internalId.value)).set(EoriNumberPage, companyDetails))
      updatedAnswersWithRefreshedAccounts <- refreshAccountsInChangeMode(
        mode, eori, eoriFromUserAnswers(mode, request), updatedAnswers)
      _ <- sessionRepository.set(updatedAnswersWithRefreshedAccounts)
      result <- doSubmission(updatedAnswers, eori, mode)(hc, request)
    } yield result).recover {
      case _: ValidationException =>
        BadRequest(view(form.withError(
          "value",
          "eoriNumber.error.invalid").fill(eori),
          mode, navigator.backLinkRouteForEORINUmberPage(mode), appConfig.xiEoriEnabled)(request, msgs, appConfig))
      case _ => Redirect(controllers.routes.TechnicalDifficulties.onPageLoad)
    }
  }

  private def doSubmission(updatedAnswers: UserAnswers,
                           eori: String,
                           mode: Mode)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    connector.validateEori(eori) map {
      case Right(true) => Redirect(navigator.nextPage(EoriNumberPage, mode, updatedAnswers))
      case _ => BadRequest(view(
        form.withError("value",
          "eoriNumber.error.invalid").fill(eori), mode, navigator.backLinkRouteForEORINUmberPage(mode), appConfig.xiEoriEnabled))
    }
  }

  private def errorView(mode: Mode,
                        inputEoriNumber: String,
                        errorMsgKey: String)(implicit request: Request[_],
                                             msgs: Messages,
                                             appConfig: FrontendAppConfig): Future[Result] =
    Future.successful(BadRequest(view(form.withError(
      "value",
      errorMsgKey).fill(inputEoriNumber),
      mode,
      navigator.backLinkRouteForEORINUmberPage(mode), appConfig.xiEoriEnabled)(request, msgs, appConfig))
    )

  /**
   * Gets the eori from UserAnswers. Sets eori to emptyString if not found
   */
  private def eoriFromUserAnswers(mode: Mode,
                                  request: OptionalDataRequest[AnyContent]): String =
        request.userAnswers.getOrElse(UserAnswers(request.internalId.value)).get(
          EoriNumberPage).getOrElse(CompanyDetails(emptyString, None)).eori

  /**
   * Updates the AccountsPage value to empty list (in UserAnswers)(to refresh the Accounts selection)
   * and EoriDetailsCorrectPage value to No
   *
   * Returns: UserAnswers updated with refreshed accounts
   */
  private def refreshAccountsInChangeMode(mode: Mode,
                                          requestEori: String,
                                          eoriFromUserAnswers: String,
                                          userAnswers: UserAnswers): Future[UserAnswers] =
    Future(
      if (!requestEori.equals(eoriFromUserAnswers)) {
        userAnswers.set(AccountsPage, List()) match {
          case Success(value) =>
            val finalUpdatedUserAnswers: Try[UserAnswers] = value.set(EoriDetailsCorrectPage, EoriDetailsCorrect.No)
            if (finalUpdatedUserAnswers.isSuccess) finalUpdatedUserAnswers.get else value
          case _ => userAnswers
        }
      } else {
        userAnswers
      }
    )

  /**
   * Checks whether user is registered for XI EORI
   */
  private def isNotRegisteredForXiEori(xiEori: Option[String],
                                    inputEori: String) = xiEori.isEmpty && isXIEori(inputEori)

  /**
   * Checks whether the provided XI EORI is user's own XI EORI
   */
  private def isOwnXiEori(xiEori: Option[String],
                          inputEori: String): Boolean =
    xiEori.nonEmpty && isXIEori(inputEori) && inputEori.equals(xiEori.getOrElse(emptyString))
}
