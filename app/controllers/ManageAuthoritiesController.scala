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
import connectors.{CustomsDataStoreConnector, SecureMessageConnector}
import controllers.actions._
import models.domain.{AuthoritiesWithId, CDSAccounts, EORI}
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.i18n._
import play.api.mvc._
import services._
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.StringUtils.emptyString
import viewmodels.ManageAuthoritiesViewModel
import views.html._

import javax.inject.Inject
import scala.concurrent._
import scala.util.control.NonFatal

class ManageAuthoritiesController @Inject()(override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            checkEmailIsVerified: EmailAction,
                                            service: AuthoritiesCacheService,
                                            accountsCacheService: AccountsCacheService,
                                            authEoriAndCompanyInfoService:AuthorisedEoriAndCompanyInfoService,
                                            dataStoreConnector: CustomsDataStoreConnector,
                                            secureMessageConnector: SecureMessageConnector,
                                            noAccountsView: NoAccountsView,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: ManageAuthoritiesView,
                                            failureView: ManageAuthoritiesApiFailureView,
                                            invalidAuthorityView: ManageAuthoritiesGBNAuthorityView
                                           )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
  extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen checkEmailIsVerified).async {
    implicit request =>

      val returnToUrl = appConfig.manageAuthoritiesServiceUrl + routes.ManageAuthoritiesController.onPageLoad.url

      val response = for {
        xiEori <- dataStoreConnector.getXiEori(request.eoriNumber)
        accountsFromCache <- accountsCacheService.retrieveAccountsForId(request.internalId)
        accounts <- fetchAccounts(xiEori, accountsFromCache)
        authoritiesFromCache <- service.retrieveAuthoritiesForId(request.internalId)
        authorities <- fetchAuthorities(xiEori, accounts, authoritiesFromCache)
        authEoriAndCompanyInfo <- fetchAuthEoriAndCompanyInfoForTheView(
          authorities.fold[Set[EORI]](Set())(authId => authId.uniqueAuthorisedEORIs))
        messageBanner <- secureMessageConnector.getMessageCountBanner(returnToUrl)
      } yield (authorities, accounts, authEoriAndCompanyInfo.getOrElse(Map.empty), messageBanner)

      response.map {
        case (Some(authorities), accounts, authEoriAndCompanyInfo, messageBanner) =>
          Ok(view(
            ManageAuthoritiesViewModel(authorities, accounts, authEoriAndCompanyInfo),
            messageBanner.map(_.successfulContentOrEmpty)
          ))
        case (None, _, _, _) =>
          Ok(noAccountsView())
      }.recover {
        case UpstreamErrorResponse(e, INTERNAL_SERVER_ERROR, _, _) if e.contains("JSON Validation Error") =>
          logger.warn(s"[FetchAccountAuthorities API] Failed with JSON Validation error")
          Redirect(routes.ManageAuthoritiesController.validationFailure())
        case NonFatal(e) =>
          logger.warn(s"[FetchAccountAuthorities API] Failed with error: ${e.getMessage}")
          Redirect(routes.ManageAuthoritiesController.unavailable)
      }
  }

  def fetchAuthoritiesOnMIDVAHomePageLoad(eori: EORI): Action[AnyContent] =
    (identify andThen checkEmailIsVerified).async {
      implicit request =>
        val fetchedAuthorities: Future[Option[AuthoritiesWithId]] = for {
          xiEori <- dataStoreConnector.getXiEori(eori)
          accounts <- getAllAccounts(eori, xiEori)
          authorities <- getAllAuthorities(eori, xiEori, accounts)
        } yield authorities

        fetchCompanyDetailsForAuthorisedEORIs(fetchedAuthorities)

        fetchedAuthorities.map {
          case Some(_) => Future.successful(Ok)
          case _ => Future.successful(NoContent)
        }.recover {
          case _ =>
            logger.warn(s"Authorities could not be fetched and saved in cache for Eori number : $eori")
            Future.successful(InternalServerError)
        }.flatten
    }

  private def fetchAccounts(xiEori: Option[EORI],
                            accountsFromCache: Option[CDSAccounts])
                           (implicit request: IdentifierRequest[AnyContent]): Future[CDSAccounts] = {
    if (accountsFromCache.isEmpty) {
      getAllAccounts(request.eoriNumber, xiEori)
    } else {
      Future(accountsFromCache.get)
    }
  }

  private def fetchAuthorities(xiEori: Option[EORI],
                               accounts: CDSAccounts,
                               authoritiesFromCache: Option[AuthoritiesWithId])
                              (implicit request: IdentifierRequest[AnyContent]): Future[Option[AuthoritiesWithId]] = {
    if (authoritiesFromCache.isEmpty) {
      getAllAuthorities(request.eoriNumber, xiEori, accounts)
    } else {
      Future(authoritiesFromCache)
    }
  }

  private def fetchAuthEoriAndCompanyInfoForTheView(authorisedEoris: Set[EORI])
                                                   (implicit request: IdentifierRequest[AnyContent])= {
    if(authorisedEoris.isEmpty) {
      Future(None)
    } else {
      authEoriAndCompanyInfoService.retrieveAuthorisedEoriAndCompanyInfo(request.internalId, authorisedEoris)
    }
  }

  private def getAllAccounts(eori: EORI,
                             xiEori: Option[String])
                            (implicit request: IdentifierRequest[AnyContent]): Future[CDSAccounts] = {
    val eoriList = Seq(eori, xiEori.getOrElse(emptyString)).filterNot(_ == emptyString)

    for {
      accounts <- accountsCacheService.retrieveAccounts(request.internalId, eoriList)
    } yield accounts
  }

  private def getAllAuthorities(eori: EORI,
                                xiEori: Option[String],
                                accounts: CDSAccounts)
                               (implicit request: IdentifierRequest[AnyContent]): Future[Option[AuthoritiesWithId]] = {
    val eoriList = Seq(eori, xiEori.getOrElse(emptyString)).filterNot(_ == emptyString)

    for {
      authorities <- if (accounts.openAccounts.nonEmpty || accounts.closedAccounts.nonEmpty) {
        service.retrieveAuthorities(request.internalId, eoriList).map(Some(_))
      } else {
        Future.successful(None)
      }
    } yield authorities
  }

  private def fetchCompanyDetailsForAuthorisedEORIs(authWithId: Future[Option[AuthoritiesWithId]])
                                                   (implicit request: IdentifierRequest[AnyContent]): Future[Unit] = {
    authWithId.map {
      case Some(authorities) =>

        authEoriAndCompanyInfoService
          .retrieveAuthorisedEoriAndCompanyInfo(request.internalId, authorities.uniqueAuthorisedEORIs)
        logger.info(s"Company info is saved in cache")

      case _ => logger.info(s"Company info could not be saved in cache")
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
