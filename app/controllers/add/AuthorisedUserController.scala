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
import controllers.grantAccountAuthRequestList
import forms.AuthorisedUserFormProviderWithConsent
import models.requests.{AddAuthorityRequest, GrantAccountAuthorityRequest}
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import pages.add.AuthorisedUserPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.DateTimeService
import services.add.{AddAuthorityValidationService, CheckYourAnswersValidationService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.StringUtils.nIEORIPrefix
import viewmodels.CheckYourAnswersHelper
import views.html.add.AuthorisedUserView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedUserController @Inject()(override val messagesApi: MessagesApi,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         connector: CustomsFinancialsConnector,
                                         formProvider: AuthorisedUserFormProviderWithConsent,
                                         cyaValidationService: CheckYourAnswersValidationService,
                                         addAuthorityValidationService: AddAuthorityValidationService,
                                         dateTimeService: DateTimeService,
                                         verifyAccountNumbers: VerifyAccountNumbersAction,
                                         val controllerComponents: MessagesControllerComponents,
                                         dataStore: CustomsDataStoreConnector,
                                         view: AuthorisedUserView
                                        )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
                                        extends FrontendBaseController with I18nSupport with Logging {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData andThen verifyAccountNumbers) {
    implicit request =>
      cyaValidationService.validate(request.userAnswers)
        .fold(errorPage("UserAnswers did not contain sufficient data for Check your answers")) {
          _ =>  Ok(view(form, CheckYourAnswersHelper(request.userAnswers, dateTimeService)))}
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData andThen verifyAccountNumbers).async {
    implicit request =>
      for {
        xiEori <- dataStore.getXiEori(request.eoriNumber)
        result <- doSubmission(request.userAnswers, xiEori.getOrElse(""), request.eoriNumber)
      } yield result
  }

  def doSubmission(userAnswers: UserAnswers, xiEori: String, gbEori: String)(implicit hc: HeaderCarrier): Future[Result] = {
    addAuthorityValidationService.validate(userAnswers)
      .fold(
        Future.successful(errorPage("UserAnswers did not contain sufficient data to construct add authority request"))
      ) { payload =>
        val enteredEori = (userAnswers.data \ "eoriNumber" \ "eori").as[String]
        val ownerEori = if (enteredEori.startsWith(nIEORIPrefix)) xiEori else gbEori

        if (enteredEori.startsWith(nIEORIPrefix)) {
          processPayloadForXIEori(userAnswers, xiEori, gbEori, payload)
        } else {
          connector.grantAccountAuthorities(payload, ownerEori).map {
            case true => Redirect(navigator.nextPage(AuthorisedUserPage, NormalMode, userAnswers))
            case false => errorPage(("Add authority request submission to backend failed", payload))
          }
        }
      }
  }

  /**
   * Sends two calls to grant authority if Accounts has both DD (with XI ownerEORI )
   *   and Cash/Guarantee accounts (with GB EORI as ownerEORI)
   * Sends only one call if Accounts has only DD account
   */
  private def processPayloadForXIEori(userAnswers: UserAnswers,
                                      xiEori: String,
                                      gbEori: String,
                                      payload: AddAuthorityRequest)(implicit hc: HeaderCarrier): Future[Result] = {
    val grantAccAuthRequests: Seq[GrantAccountAuthorityRequest] = grantAccountAuthRequestList(payload, xiEori, gbEori)

    for {
      result <- Future.sequence(grantAccAuthRequests.map {
        req => connector.grantAccountAuthorities(req.payload, req.ownerEori)
      })
    } yield {
      if (result.contains(false)) {
        errorPage(("Add authority request submission to backend failed", payload))
      } else {
        Redirect(navigator.nextPage(AuthorisedUserPage, NormalMode, userAnswers))
      }
    }
  }

  private def errorPage(msg:String): Result = {
    logger.error(msg)
    Redirect(controllers.routes.TechnicalDifficulties.onPageLoad)
  }
}
