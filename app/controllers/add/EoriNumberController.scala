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
import javax.inject.Inject
import models.{CompanyDetails, Mode, UserAnswers}
import navigation.Navigator
import pages.add.EoriNumberPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.EoriNumberView
import scala.concurrent.{ExecutionContext, Future}
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
              _ <- sessionRepository.set(updatedAnswers)
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
  private def doSubmission(updatedAnswers: UserAnswers, eori: String, mode: Mode)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    connector.validateEori(eori) map {
      case Right(true) => Redirect(navigator.nextPage(EoriNumberPage, mode, updatedAnswers))
      case _ => BadRequest(view(form.withError("value", "eoriNumber.error.invalid").fill(eori), mode, navigator.backLinkRouteForEORINUmberPage(mode)))
    }
  }

  protected def formatGBEori(str: String): String = str.replaceAll("\\s", "").replace("\b[a-z]","\b[A-Z]")
}
