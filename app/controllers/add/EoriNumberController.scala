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

package controllers.add

import config.FrontendAppConfig
import connectors.CustomsFinancialsConnector
import controllers.actions._
import forms.EoriNumberFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.add.EoriNumberPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.GBNEoriErrorView
import views.html.add.EoriNumberView

import javax.inject.Inject
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
                                      gbnEoriView: GBNEoriErrorView,
                                      connector: CustomsFinancialsConnector
                                    )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.get(EoriNumberPage)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, navigator.backLinkRouteForEORINUmberPage(mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>{
          val errorMessages = formWithErrors.errors.flatMap(_.messages)
          if(errorMessages.contains("eoriNumber.error.gbnEori.format")) {
            Future.successful(Redirect(controllers.add.routes.GBNEoriController.showGBNEori()))
          } else{
            Future.successful(BadRequest(view(formWithErrors, mode, navigator.backLinkRouteForEORINUmberPage(mode))))
          }
        },

        eoriNumber => {
          val eori = stripWhitespace(eoriNumber)
          if (request.eoriNumber.equalsIgnoreCase(eori)) {
            Future.successful(BadRequest(view(form.withError("value", "eoriNumber.error.authorise-own-eori").fill(eoriNumber), mode, navigator.backLinkRouteForEORINUmberPage(mode))))
          } else {
            (for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.internalId.value)).set(EoriNumberPage, eori))
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

  protected def stripWhitespace(str: String): String =
    str.replaceAll("\\s", "")
}
