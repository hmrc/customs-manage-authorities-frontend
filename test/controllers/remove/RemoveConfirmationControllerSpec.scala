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

package controllers.remove

import base.SpecBase
import config.FrontendAppConfig
import models.domain._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{ConfirmationDetails, ConfirmationPage}
import play.api.inject.bind
import play.api.test.Helpers._
import repositories.AuthoritiesRepository
import views.html.remove.RemoveConfirmationView
import java.time.LocalDate
import scala.concurrent.Future

class RemoveConfirmationControllerSpec extends SpecBase {

  val startDate = LocalDate.parse("2020-03-01")
  val endDate = LocalDate.parse("2020-04-01")
  val standingAuthority = StandingAuthority("EORI", startDate, Some(endDate), viewBalance = false)
  val accounts = Seq(AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq(standingAuthority)))
  val authoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
    ("a" -> AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority)))
  ))

  "RemoveConfirmation Controller" must {

    "return OK and clear repository entry" when {

      "The user is returning to the page " in {
        val userAnswers = emptyUserAnswers.set(ConfirmationPage , ConfirmationDetails("eori", None, Some("Tony Stark"), true)).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application){

          val request = fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RemoveConfirmationView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view("eori", Some("Tony Stark"))(request, messages(application), appConfig).toString
        }
      }

      // "accountId and authorityId can be found" in {
      //   val mockRepository = mock[AuthoritiesRepository]
      //   when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
      //   when(mockRepository.clear(any())).thenReturn(Future.successful(true))

      //   val application = applicationBuilder()
      //     .overrides(
      //       bind[AuthoritiesRepository].toInstance(mockRepository)
      //     )
      //     .build()

      //   running(application) {

      //     val request = fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url)

      //     val result = route(application, request).value

      //     val view = application.injector.instanceOf[RemoveConfirmationView]
      //     val appConfig = application.injector.instanceOf[FrontendAppConfig]

      //     status(result) mustEqual OK

      //     contentAsString(result) mustEqual view("EORI", Some("Tony Stark"))(request, messages(application), appConfig).toString()

      //     verify(mockRepository, times(1)).clear("id")
      //   }
      // }

    }

    "throw an exception" when {

      "accountId cannot be found" in {
        val mockRepository = mock[AuthoritiesRepository]
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))

        val application = applicationBuilder()
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockRepository)
          )
          .build()

        running(application) {

          val request = fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("missing", "b").url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
        }
      }

      "authorityId cannot be found" in {
        val mockRepository = mock[AuthoritiesRepository]
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))

        val application = applicationBuilder()
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockRepository)
          )
          .build()

        running(application) {

          val request = fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "missing").url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
        }
      }

    }

  }
}
