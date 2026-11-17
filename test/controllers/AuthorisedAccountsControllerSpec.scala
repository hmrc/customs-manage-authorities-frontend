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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import connectors.{CustomsDataStoreConnector, CustomsFinancialsConnector, SdesConnector, SecureMessageConnector}
import models.UnverifiedEmail
import models.domain.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.Helpers.*
import services.{AccountsCacheService, AuthorisedEoriAndCompanyInfoService, AuthoritiesCacheService}
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.TestData.{OPEN_CASH_ACC_WITH_AUTH_WITH_ID, testEmail}

import scala.concurrent.Future

class AuthorisedAccountsControllerSpec extends SpecBase with MockitoSugar {
  private val pageRoute = routes.ManageAuthoritiesController.onAuthorisedAccountsPageLoad().url

  "Authorised accounts route" must {
    "render cached authorities with company details without depending on CSV downloads" in new Setup {
      running(app) {
        val result = route(app, fakeRequest(GET, pageRoute)).value
        status(result) mustBe OK
        contentAsString(result) must include("Accounts you have already authorised")
        contentAsString(result) must include("Tony Stark")
        verify(authoritiesService, never()).retrieveAuthorities(any(), any())(any())
        verify(accountsService, never()).retrieveAccounts(any(), any())(any())
        verifyNoInteractions(sdes, financials)
        verify(messagesConnector).getMessageCountBanner(
          eqTo(app.injector.instanceOf[FrontendAppConfig].manageAuthoritiesServiceUrl + pageRoute),
          eqTo(Some("authorities"))
        )(any())
      }
    }

    "load authorities and accounts when the page is bookmarked without a cache" in new Setup {
      when(accountsService.retrieveAccountsForId(any())).thenReturn(Future.successful(None))
      when(authoritiesService.retrieveAuthoritiesForId(any())).thenReturn(Future.successful(None))
      when(accountsService.retrieveAccounts(any(), any())(any())).thenReturn(Future.successful(accounts))
      when(authoritiesService.retrieveAuthorities(any(), any())(any())).thenReturn(Future.successful(authorities))
      running(app) {
        status(route(app, fakeRequest(GET, pageRoute)).value) mustBe OK
        verify(accountsService).retrieveAccounts(any(), any())(any())
        verify(authoritiesService).retrieveAuthorities(any(), any())(any())
      }
    }

    "render EORI details when the company name is unavailable" in new Setup {
      when(companyService.retrieveAuthorisedEoriAndCompanyInfo(any(), any())(any())).thenReturn(Future.successful(None))
      running(app) {
        val result = route(app, fakeRequest(GET, pageRoute)).value
        status(result) mustBe OK
        contentAsString(result) must include(OPEN_CASH_ACC_WITH_AUTH_WITH_ID.authorities.values.head.authorisedEori)
      }
    }

    "return traders without authorities to the landing page" in new Setup {
      when(authoritiesService.retrieveAuthoritiesForId(any()))
        .thenReturn(Future.successful(Some(AuthoritiesWithId(Map.empty))))
      running(app) {
        redirectLocation(route(app, fakeRequest(GET, pageRoute)).value) mustBe Some(
          routes.ManageAuthoritiesController.onPageLoad().url
        )
        verifyNoInteractions(companyService)
      }
    }

    "return traders without accounts to the landing page" in new Setup {
      when(accountsService.retrieveAccountsForId(any()))
        .thenReturn(Future.successful(Some(CDSAccounts("GB123456789012", Nil))))
      when(authoritiesService.retrieveAuthoritiesForId(any())).thenReturn(Future.successful(None))
      running(app) {
        redirectLocation(route(app, fakeRequest(GET, pageRoute)).value) mustBe Some(
          routes.ManageAuthoritiesController.onPageLoad().url
        )
        verify(authoritiesService, never()).retrieveAuthorities(any(), any())(any())
      }
    }

    "retain the existing unavailable page when authorities cannot be retrieved" in new Setup {
      when(authoritiesService.retrieveAuthoritiesForId(any()))
        .thenReturn(Future.failed(new RuntimeException("unavailable")))
      running(app) {
        redirectLocation(route(app, fakeRequest(GET, pageRoute)).value) mustBe Some(
          routes.ManageAuthoritiesController.unavailable().url
        )
      }
    }

    "retain the existing validation failure page for invalid upstream data" in new Setup {
      when(authoritiesService.retrieveAuthoritiesForId(any()))
        .thenReturn(Future.failed(UpstreamErrorResponse("JSON Validation Error", INTERNAL_SERVER_ERROR)))
      running(app) {
        redirectLocation(route(app, fakeRequest(GET, pageRoute)).value) mustBe Some(
          routes.ManageAuthoritiesController.validationFailure().url
        )
      }
    }

    "require a verified email before displaying authorities" in new Setup {
      when(dataStore.getEmail(any())).thenReturn(Future.successful(Left(UnverifiedEmail)))
      running(app) {
        redirectLocation(route(app, fakeRequest(GET, pageRoute)).value) mustBe Some(
          routes.EmailController.showUnverified().url
        )
        verifyNoInteractions(accountsService, authoritiesService)
      }
    }
  }

  private trait Setup {
    val accountsService    = mock[AccountsCacheService]
    val authoritiesService = mock[AuthoritiesCacheService]
    val companyService     = mock[AuthorisedEoriAndCompanyInfoService]
    val dataStore          = mock[CustomsDataStoreConnector]
    val sdes               = mock[SdesConnector]
    val financials         = mock[CustomsFinancialsConnector]
    val messagesConnector  = mock[SecureMessageConnector]
    val accounts           = CDSAccounts(
      "GB123456789012",
      List(CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100))))
    )
    val authorities        = AuthoritiesWithId(Map("cash" -> OPEN_CASH_ACC_WITH_AUTH_WITH_ID))
    val companies          = authorities.uniqueAuthorisedEORIs.map(_ -> "Tony Stark").toMap

    when(dataStore.getEmail(any())).thenReturn(Future.successful(Right(testEmail)))
    when(dataStore.getXiEori(any())(any())).thenReturn(Future.successful(Some("XI123456789012")))
    when(accountsService.retrieveAccountsForId(any())).thenReturn(Future.successful(Some(accounts)))
    when(authoritiesService.retrieveAuthoritiesForId(any())).thenReturn(Future.successful(Some(authorities)))
    when(companyService.retrieveAuthorisedEoriAndCompanyInfo(any(), any())(any()))
      .thenReturn(Future.successful(Some(companies)))
    when(messagesConnector.getMessageCountBanner(any(), any())(any())).thenReturn(Future.successful(None))

    lazy val app = applicationBuilder()
      .overrides(
        bind[AccountsCacheService].toInstance(accountsService),
        bind[AuthoritiesCacheService].toInstance(authoritiesService),
        bind[AuthorisedEoriAndCompanyInfoService].toInstance(companyService),
        bind[CustomsDataStoreConnector].toInstance(dataStore),
        bind[SdesConnector].toInstance(sdes),
        bind[CustomsFinancialsConnector].toInstance(financials),
        bind[SecureMessageConnector].toInstance(messagesConnector)
      )
      .build()
  }
}
