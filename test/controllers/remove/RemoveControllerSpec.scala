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
import connectors.CustomsFinancialsConnector
import forms.RemoveFormProvider
import models.domain.{AccountStatusOpen, AccountWithAuthoritiesWithId, AuthorisedUser, AuthoritiesWithId, CdsCashAccount, StandingAuthority}
import models.requests.RevokeAuthorityRequest
import org.mockito.Matchers.{any, eq => meq}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.Helpers._
import repositories.AuthoritiesRepository
import viewmodels.RemoveViewModel
import views.html.remove.RemoveView

import java.time.LocalDate
import scala.concurrent.Future

class RemoveControllerSpec extends SpecBase {

  val startDate = LocalDate.parse("2020-03-01")
  val endDate = LocalDate.parse("2020-04-01")
  val standingAuthority = StandingAuthority("EORI", startDate, Some(endDate), viewBalance = false)
  val accountsWithAuthoritiesWithId = AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority))
  val authoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
    ("a" -> accountsWithAuthoritiesWithId)
  ))

  private val formProvider = new RemoveFormProvider()
  private val form = formProvider()

  val mockRepository = mock[AuthoritiesRepository]
  when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
  when(mockRepository.clear(any())).thenReturn(Future.successful(true))

  "Remove Controller" must {

    "return OK and show view for GET" when {

      "accountId and authorityId can be found" in {
        val application = applicationBuilder()
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockRepository)
          )
          .build()

        running(application) {

          val request = fakeRequest(GET, controllers.remove.routes.RemoveController.onPageLoad("a", "b").url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RemoveView]

          val viewModel = RemoveViewModel("a", "b", accountsWithAuthoritiesWithId, standingAuthority)

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(form, viewModel)(request, messages(application)).toString()
        }
      }

    }

    "redirect to next page when valid data is submitted" in {
      val mockConnector = mock[CustomsFinancialsConnector]
      when(mockConnector.revokeAccountAuthorities(any())(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder()
        .overrides(
          bind[AuthoritiesRepository].toInstance(mockRepository),
          bind[CustomsFinancialsConnector].toInstance(mockConnector)
        )
        .build()

      running(application) {

        val request = fakeRequest(POST, controllers.remove.routes.RemoveController.onSubmit("a", "b").url)
          .withFormUrlEncodedBody(
            "fullName" -> "name",
            "jobRole" -> "jobRole",
            "confirmation" -> "true"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url

        val expectedRequest = RevokeAuthorityRequest(
          "12345",
          CdsCashAccount,
          "EORI",
          AuthorisedUser("name", "jobRole")
        )

        verify(mockConnector, times(1)).revokeAccountAuthorities(meq(expectedRequest))(any())
      }
    }

    "display technical difficulties page when valid data is submitted and submission fails" in {
      val mockConnector = mock[CustomsFinancialsConnector]
      when(mockConnector.revokeAccountAuthorities(any())(any())).thenReturn(Future.successful(false))

      val application = applicationBuilder()
        .overrides(
          bind[AuthoritiesRepository].toInstance(mockRepository),
          bind[CustomsFinancialsConnector].toInstance(mockConnector)
        )
        .build()

      running(application) {

        val request = fakeRequest(POST, controllers.remove.routes.RemoveController.onSubmit("a", "b").url)
          .withFormUrlEncodedBody(
            "fullName" -> "name",
            "jobRole" -> "jobRole",
            "confirmation" -> "true"
          )

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TechnicalDifficulties.onPageLoad.url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder()
        .overrides(
          bind[AuthoritiesRepository].toInstance(mockRepository)
        )
        .build()

      running(application) {

        val request = fakeRequest(POST, controllers.remove.routes.RemoveController.onSubmit("a", "b").url)
          .withFormUrlEncodedBody(
            "value" -> "",
          )

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveView]

        val boundForm = form.bind(Map("value" -> ""))

        val viewModel = RemoveViewModel("a", "b", accountsWithAuthoritiesWithId, standingAuthority)

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual view(boundForm, viewModel)(request, messages(application)).toString()
      }
    }

    "display technical difficulties page for get" when {

      "accountId cannot be found" in {
        val application = applicationBuilder()
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockRepository)
          )
          .build()

        running(application) {

          val request = fakeRequest(GET, controllers.remove.routes.RemoveController.onPageLoad("missing", "b").url)
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.TechnicalDifficulties.onPageLoad.url
        }
      }

      "authorityId cannot be found" in {
        val application = applicationBuilder()
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockRepository)
          )
          .build()

        running(application) {

          val request = fakeRequest(GET, controllers.remove.routes.RemoveController.onPageLoad("a", "missing").url)
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.TechnicalDifficulties.onPageLoad.url
        }
      }
    }

    "display technical difficulties page for POST" when {

      "accountId cannot be found" in {
        val application = applicationBuilder()
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockRepository)
          )
          .build()

        running(application) {

          val request = fakeRequest(POST, controllers.remove.routes.RemoveController.onSubmit("missing", "b").url)
            .withFormUrlEncodedBody(
              "fullName" -> "name",
              "jobRole" -> "jobRole",
              "confirmation" -> "true"
            )
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.TechnicalDifficulties.onPageLoad.url
        }
      }

      "authorityId cannot be found" in {
        val application = applicationBuilder()
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockRepository)
          )
          .build()

        running(application) {

          val request = fakeRequest(POST, controllers.remove.routes.RemoveController.onSubmit("a", "missing").url)
            .withFormUrlEncodedBody(
              "fullName" -> "name",
              "jobRole" -> "jobRole",
              "confirmation" -> "true"
            )
          val result =  route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.TechnicalDifficulties.onPageLoad.url
        }
      }
    }
  }
}
