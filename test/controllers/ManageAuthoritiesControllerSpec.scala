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

import base.SpecBase
import config.FrontendAppConfig
import connectors.CustomsFinancialsConnector
import models.domain.{AccountStatusClosed, AccountStatusOpen, AccountWithAuthorities, AccountWithAuthoritiesWithId, AuthoritiesWithId, CDSAccounts, CDSCashBalance, CashAccount, CdsCashAccount, StandingAuthority}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.Helpers._
import repositories.AuthoritiesRepository
import services.AccountsCacheService
import uk.gov.hmrc.http.UpstreamErrorResponse
import viewmodels.ManageAuthoritiesViewModel
import views.html.{ManageAuthoritiesApiFailureView, ManageAuthoritiesView, NoAccountsView}

import java.time.LocalDate
import scala.concurrent.Future

class ManageAuthoritiesControllerSpec extends SpecBase with MockitoSugar {

  private lazy val manageAuthoritiesRoute = routes.ManageAuthoritiesController.onPageLoad().url
  private lazy val manageAuthoritiesUnavailableRoute = routes.ManageAuthoritiesController.unavailable().url
  private lazy val manageAuthoritiesGBNValidationRoute = routes.ManageAuthoritiesController.validationFailure().url

  val startDate = LocalDate.parse("2020-03-01")
  val endDate = LocalDate.parse("2020-04-01")
  val standingAuthority = StandingAuthority("EORI", startDate, Some(endDate), viewBalance = false)
  val accounts = Seq(AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq(standingAuthority)))

  val authoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
    ("a" -> AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority)))
  ))

  "ManageAuthorities Controller" when {

    "API call succeeds" must {

      "return OK and the correct view if no accounts associated with a EORI" in {
        val accounts = CDSAccounts("GB123456789012", List())

        val mockRepository = mock[AuthoritiesRepository]
        val mockAccountsCacheService = mock[AccountsCacheService]
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockAccountsCacheService.retrieveAccounts(any(), any())(any())).thenReturn(Future.successful(accounts))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockRepository),
            bind[AccountsCacheService].toInstance(mockAccountsCacheService)
          ).configure("features.edit-journey" -> true)
          .build()

        running(application) {

          val request = fakeRequest(GET, manageAuthoritiesRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[NoAccountsView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view()(request, messages(application), appConfig).toString
        }
      }

      "return OK and the correct view" in {
        val accounts = CDSAccounts("GB123456789012", List(
          CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))),
          CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
        ))

        val mockRepository = mock[AuthoritiesRepository]
        val mockAccountsCacheService = mock[AccountsCacheService]
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockAccountsCacheService.retrieveAccounts(any(), any())(any())).thenReturn(Future.successful(accounts))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockRepository),
            bind[AccountsCacheService].toInstance(mockAccountsCacheService)
          ).configure("features.edit-journey" -> true)
          .build()

        running(application) {

          val request = fakeRequest(GET, manageAuthoritiesRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ManageAuthoritiesView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(ManageAuthoritiesViewModel(authoritiesWithId))(request, messages(application), appConfig).toString
        }
      }
    }

    "API call fails" must {

      "redirect to 'unavailable' page" in {

        val mockRepository = mock[AuthoritiesRepository]
        when(mockRepository.get(any())).thenReturn(Future.successful(None))

        val failingConnector = mock[CustomsFinancialsConnector]
        when(failingConnector.retrieveAccountAuthorities()(any())).thenReturn(Future.failed(UpstreamErrorResponse("upstream 502", 502)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[CustomsFinancialsConnector].toInstance(failingConnector),
            bind[AuthoritiesRepository].toInstance(mockRepository)

          )
          .build()

        running(application) {

          val request = fakeRequest(GET, manageAuthoritiesRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual manageAuthoritiesUnavailableRoute
        }
      }

      "serve unavailable page on a separate route" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {

          val request = fakeRequest(GET, manageAuthoritiesUnavailableRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ManageAuthoritiesApiFailureView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view()(request, messages(application), appConfig).toString
        }
      }
    }

    "API call fails due to GBN EORI Json Validation" must {

      "redirect to 'account unavailable' page" in {
        val mockAccountsCacheService = mock[AccountsCacheService]
        when(mockAccountsCacheService.retrieveAccounts(any(), any())(any())).thenReturn(Future.failed(UpstreamErrorResponse("JSON Validation Error", 500)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AccountsCacheService].toInstance(mockAccountsCacheService)
          )
          .build()


        running(application) {
          val request = fakeRequest(GET, manageAuthoritiesRoute)
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual manageAuthoritiesGBNValidationRoute
        }
      }
    }
  }
}
