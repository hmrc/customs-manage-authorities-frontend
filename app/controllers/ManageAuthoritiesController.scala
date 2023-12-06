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

package controllers

import config.FrontendAppConfig
import connectors.CustomsDataStoreConnector
import controllers.actions._
import models.domain.{AuthoritiesWithId, CDSAccounts, EORI}
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.i18n._
import play.api.mvc._
import services._
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.ManageAuthoritiesViewModel
import views.html._

import javax.inject.Inject
import scala.concurrent._
import scala.util.control.NonFatal

class ManageAuthoritiesController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             identify: IdentifierAction,
                                             checkEmailIsVerified: EmailAction,
                                             service: AuthoritiesCacheService,
                                             accountsCacheService: AccountsCacheService,
                                             dataStoreConnector: CustomsDataStoreConnector,
                                             noAccountsView: NoAccountsView,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: ManageAuthoritiesView,
                                             failureView: ManageAuthoritiesApiFailureView,
                                             invalidAuthorityView: ManageAuthoritiesGBNAuthorityView
                                           )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen checkEmailIsVerified).async {
    implicit request =>
      val response = for {
        xiEori <- dataStoreConnector.getXiEori(request.eoriNumber)
        accounts <- getAllAccounts(request.eoriNumber, xiEori)
        authorities <- getAllAuthorities(request.eoriNumber, xiEori, accounts)
      } yield (authorities, accounts)

      response.map {
        case (Some(authorities), accounts) =>
          Ok(view(ManageAuthoritiesViewModel(authorities, accounts)))
        case (None, _) =>
          Ok(noAccountsView())
      }.recover {
        case  UpstreamErrorResponse(e, INTERNAL_SERVER_ERROR, _, _) if e.contains("JSON Validation Error")=>
          logger.warn(s"[FetchAccountAuthorities API] Failed with JSON Validation error")
          Redirect(routes.ManageAuthoritiesController.validationFailure())
        case NonFatal(e) =>
          logger.warn(s"[FetchAccountAuthorities API] Failed with error: ${e.getMessage}")
          Redirect(routes.ManageAuthoritiesController.unavailable)
      }
  }

  private def getAllAccounts(eori: EORI, xiEori: Option[String])(implicit request: IdentifierRequest[AnyContent]): Future[CDSAccounts] = {
    val eoriList = Seq(eori, xiEori.getOrElse("")).filterNot(_ == "")
    for {
      accounts <- accountsCacheService.retrieveAccounts(request.internalId, eoriList)
    } yield accounts
  }

  private def getAllAuthorities(eori: EORI, xiEori: Option[String], accounts: CDSAccounts)
                               (implicit request: IdentifierRequest[AnyContent]): Future[Option[AuthoritiesWithId]] = {
    val eoriList = Seq(eori, xiEori.getOrElse("")).filterNot(_ == "")
    for {
      authorities <- if (accounts.openAccounts.nonEmpty || accounts.closedAccounts.nonEmpty) {
        service.retrieveAuthorities(request.internalId, eoriList).map(Some(_))
      } else { Future.successful(None) }
    } yield authorities
  }

  def unavailable(): Action[AnyContent] = identify.async {
    implicit request =>
      Future.successful(Ok(failureView()))
  }

  def validationFailure(): Action[AnyContent] = identify.async {
    implicit request =>
      Future.successful(Ok(invalidAuthorityView()))
  }
}
case object TimeoutResponse extends Exception
