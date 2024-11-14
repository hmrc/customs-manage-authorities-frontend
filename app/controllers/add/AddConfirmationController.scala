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
import controllers.actions._
import pages.ConfirmationPage
import pages.add.{AccountsPage, AuthorityStartDatePage, EoriNumberPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{AccountsRepository, AuthoritiesRepository, SessionRepository}
import services.ConfirmationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.ManageAuthoritiesViewModel.dateAsDayMonthAndYear
import views.html.add.AddConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddConfirmationController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           sessionRepository: SessionRepository,
                                           accountsRepository: AccountsRepository,
                                           authoritiesRepository: AuthoritiesRepository,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           val controllerComponents: MessagesControllerComponents,
                                           confirmationService: ConfirmationService,
                                           view: AddConfirmationView
                                         )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
  extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val startDate: Option[String] = request.userAnswers.get(AuthorityStartDatePage).map(dateAsDayMonthAndYear)
      val multipleAccounts = request.userAnswers.get(AccountsPage).exists(_.size > 1)

      request.userAnswers.get(EoriNumberPage) match {
        case Some(companyDetails) =>
          for {
            _ <- sessionRepository.clear(request.userAnswers.id)
            _ <- accountsRepository.clear(request.internalId.value)
            _ <- authoritiesRepository.clear(request.internalId.value)
            _ <- confirmationService.populateConfirmation(
              request.internalId.value,
              companyDetails.eori,
              startDate,
              companyDetails.name,
              multipleAccounts)
          } yield Ok(view(companyDetails.eori, startDate, companyDetails.name, multipleAccounts))

        case None =>
          request.userAnswers.get(ConfirmationPage) match {
            case Some(value) =>
              Future.successful(Ok(view(value.eori, value.startDate, value.companyName, value.multipleAccounts)))
            case None =>
              logger.warn("No EORI number could be found for add confirmation page")
              Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
          }

      }
  }
}
