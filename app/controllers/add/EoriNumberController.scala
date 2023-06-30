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
import models.{CheckMode, CompanyDetails, EoriDetailsCorrect, Mode, UserAnswers}
import navigation.Navigator
import pages.add.{AccountsPage, EoriDetailsCorrectPage, EoriNumberPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.StringUtils.emptyString
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

      Ok(view(preparedForm, mode, navigator.backLinkRouteForEORINUmberPage(mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>{
            Future.successful(BadRequest(view(formWithErrors, mode, navigator.backLinkRouteForEORINUmberPage(mode))))
        },

        eoriNumber => {
          val eori = formatGBEori(eoriNumber)

          if (request.eoriNumber.equalsIgnoreCase(eori)) {
            Future.successful(BadRequest(view(form.withError("value", "eoriNumber.error.authorise-own-eori").fill(eoriNumber), mode, navigator.backLinkRouteForEORINUmberPage(mode))))
          } else {
            (for {
              companyName <- dataStore.getCompanyName(eori)
              companyDetails = CompanyDetails(eori, companyName)
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.internalId.value)).set(EoriNumberPage, companyDetails))
              updatedAnswersWithRefreshedAccounts <- refreshAccountsInChangeMode(
                                                      mode, eori, eoriFromUserAnswers(mode, request), updatedAnswers)
              _ <- sessionRepository.set(updatedAnswersWithRefreshedAccounts)
              result <- doSubmission(updatedAnswers, eori, mode)
            } yield result).recover {
              case _: ValidationException =>
                BadRequest(view(form.withError("value", "eoriNumber.error.invalid").fill(eori), mode, navigator.backLinkRouteForEORINUmberPage(mode)))
              case _ => Redirect(controllers.routes.TechnicalDifficulties.onPageLoad)
            }
          }
        }
      )
  }

  private def doSubmission(updatedAnswers: UserAnswers,
                           eori: String, mode: Mode)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    connector.validateEori(eori) map {
      case Right(true) => Redirect(navigator.nextPage(EoriNumberPage, mode, updatedAnswers))
      case _ => BadRequest(view(
        form.withError("value",
          "eoriNumber.error.invalid").fill(eori), mode, navigator.backLinkRouteForEORINUmberPage(mode)))
    }
  }

  protected def formatGBEori(str: String): String = str.replaceAll("\\s", "").toUpperCase

  /**
   * Gets the eori from UserAnswers in CheckMode otherwise
   * empty string
   */
  private def eoriFromUserAnswers(mode: Mode,
                                  request: OptionalDataRequest[AnyContent]): String =
    if (mode == CheckMode) {
      request.userAnswers.getOrElse(UserAnswers(request.internalId.value)).get(
        EoriNumberPage).getOrElse(CompanyDetails(emptyString, None)).eori
    } else {
      emptyString
    }

  /**
   * Updates the AccountsPage value to empty list (in UserAnswers) to refresh the Accounts selection
   * and EoriDetailsCorrectPage value to No in CheckMode
   *
   * Returns: UserAnswers updated with refreshed accounts
   */
  private def refreshAccountsInChangeMode(mode: Mode,
                                          requestEori: String,
                                          eoriFromUserAnswers: String,
                                          userAnswers: UserAnswers): Future[UserAnswers] =
    Future(
      if (mode == CheckMode && !requestEori.equals(eoriFromUserAnswers)) {
        userAnswers.set(AccountsPage, List()) match {
          case Success(value) =>
            val finalUpdatedUserAnswers: Try[UserAnswers] = value.set(EoriDetailsCorrectPage, EoriDetailsCorrect.No)
            if(finalUpdatedUserAnswers.isSuccess) finalUpdatedUserAnswers.get else value
          case _ => userAnswers
        }
      } else {
        userAnswers
      }
    )
}
