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

package controllers

import config.FrontendAppConfig
import controllers.actions._
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
                                             service: AuthoritiesCacheService,
                                             accountsCacheService: AccountsCacheService,
                                             noAccountsView: NoAccountsView,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: ManageAuthoritiesView,
                                             failureView: ManageAuthoritiesApiFailureView,
                                             invalidAuthorityView: ManageAuthoritiesGBNAuthorityView
                                           )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = identify.async {
    implicit request =>
      val response = for {
        accounts <- accountsCacheService.retrieveAccounts(request.internalId, request.eoriNumber)
        authorities <- if (accounts.openAccounts.nonEmpty || accounts.closedAccounts.nonEmpty) {
          service.retrieveAuthorities(request.internalId).map(Some(_))
        } else {
          Future.successful(None)
        }
      } yield authorities

      response.map {
        case Some(authorities) =>
          Ok(view(ManageAuthoritiesViewModel(authorities)))
        case None =>
          Ok(noAccountsView())
      }.recover {
        case  UpstreamErrorResponse(e, INTERNAL_SERVER_ERROR, _, _) if e.contains("JSON Validation Error")=>
          logger.warn(s"[FetchAccountAuthorities API] Failed with JSON Validation error")
          Redirect(routes.ManageAuthoritiesController.validationFailure())
        case NonFatal(e) =>
          logger.warn(s"[FetchAccountAuthorities API] Failed with error: ${e.getMessage}")
          Redirect(routes.ManageAuthoritiesController.unavailable())
      }
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
