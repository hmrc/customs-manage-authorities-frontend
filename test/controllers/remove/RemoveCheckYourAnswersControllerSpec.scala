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

package controllers.remove

import base.SpecBase
import config.FrontendAppConfig
import connectors.CustomsFinancialsConnector
import models.domain.{AccountStatusOpen, AccountWithAuthoritiesWithId, AuthorisedUser, CdsCashAccount, StandingAuthority}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.remove.RemoveAuthorisedUserPage
import play.api.{Application, inject}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AccountAndAuthority, AuthoritiesCacheService, NoAccount, NoAuthority}
import viewmodels.CheckYourAnswersRemoveHelper
import views.html.remove.RemoveCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class RemoveCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  "onPageLoad" should {

    "redirect to error page if no account present" in new Setup {
      val app: Application = applicationBuilder(Some(emptyUserAnswers)).overrides(
        inject.bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService)
      ).build()

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAccount)))

      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TechnicalDifficulties.onPageLoad.url
      }
    }

    "redirect to page if no authority present" in new Setup {
      val app: Application = applicationBuilder(Some(emptyUserAnswers)).overrides(
        inject.bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService)
      ).build()

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAuthority)))

      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TechnicalDifficulties.onPageLoad.url
      }
    }

    "redirect to view authority if authorised user not present" in new Setup {
      val app: Application = applicationBuilder(Some(emptyUserAnswers)).overrides(
        inject.bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService)
      ).build()

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.ViewAuthorityController.onPageLoad("a", "b").url
      }
    }

    "return OK on successful request" in new Setup {
      val userAnswers = emptyUserAnswers.set(RemoveAuthorisedUserPage("a", "b"), AuthorisedUser("test", "test")).get

      val app: Application = applicationBuilder(Some(userAnswers)).overrides(
        inject.bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService)
      ).build()

      val view: RemoveCheckYourAnswersView = app.injector.instanceOf[RemoveCheckYourAnswersView]
      val appConfig = app.injector.instanceOf[FrontendAppConfig]
      val helper = new CheckYourAnswersRemoveHelper(userAnswers, "a", "b", AuthorisedUser("test", "test"), standingAuthority, accountsWithAuthoritiesWithId)(messages(app))

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe OK
        contentAsString(result) mustBe view(helper)(getRequest, messages(app), appConfig).toString()
      }
    }
  }

  "onSubmit" should {
    "redirect to error page when no account present" in new Setup {
      val app: Application = applicationBuilder(Some(emptyUserAnswers)).overrides(
        inject.bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService)
      ).build()

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAccount)))

      running(app) {
        val result = route(app, postRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TechnicalDifficulties.onPageLoad.url
      }
    }

    "redirect to error page when no authority present" in new Setup {
      val app: Application = applicationBuilder(Some(emptyUserAnswers)).overrides(
        inject.bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService)
      ).build()

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAuthority)))

      running(app) {
        val result = route(app, postRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TechnicalDifficulties.onPageLoad.url
      }
    }

    "redirect to error page if no authorised user present" in new Setup {
      val app: Application = applicationBuilder(Some(emptyUserAnswers)).overrides(
        inject.bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService)
      ).build()

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      running(app) {
        val result = route(app, postRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TechnicalDifficulties.onPageLoad.url
      }
    }

    "redirect to error page if revoke failed" in new Setup {
      val userAnswers = emptyUserAnswers.set(RemoveAuthorisedUserPage("a", "b"), AuthorisedUser("test", "test")).get

      val app: Application = applicationBuilder(Some(userAnswers)).overrides(
        inject.bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
        inject.bind[CustomsFinancialsConnector].toInstance(mockCustomsFinancialsConnector)
      ).build()

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      when(mockCustomsFinancialsConnector.revokeAccountAuthorities(any())(any()))
        .thenReturn(Future.successful(false))

      running(app) {
        val result = route(app, postRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TechnicalDifficulties.onPageLoad.url
      }
    }

    "redirect to confirmation page when successful" in new Setup {
      val userAnswers = emptyUserAnswers.set(RemoveAuthorisedUserPage("a", "b"), AuthorisedUser("test", "test")).get

      val app: Application = applicationBuilder(Some(userAnswers)).overrides(
        inject.bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
        inject.bind[CustomsFinancialsConnector].toInstance(mockCustomsFinancialsConnector)
      ).build()

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      when(mockCustomsFinancialsConnector.revokeAccountAuthorities(any())(any()))
        .thenReturn(Future.successful(true))

      running(app) {
        val result = route(app, postRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url
      }
    }
  }

  trait Setup {
    val mockAuthoritiesCacheService: AuthoritiesCacheService = mock[AuthoritiesCacheService]
    val mockCustomsFinancialsConnector: CustomsFinancialsConnector = mock[CustomsFinancialsConnector]

    val startDate: LocalDate = LocalDate.parse("2020-03-01")
    val endDate: LocalDate = LocalDate.parse("2020-04-01")
    val standingAuthority: StandingAuthority = StandingAuthority("EORI", startDate, Some(endDate), viewBalance = false)
    val accountsWithAuthoritiesWithId: AccountWithAuthoritiesWithId =
      AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority))

    val getRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, controllers.remove.routes.RemoveCheckYourAnswers.onPageLoad("a", "b").url)
    val postRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(POST, controllers.remove.routes.RemoveCheckYourAnswers.onSubmit("a", "b").url)
  }
}
