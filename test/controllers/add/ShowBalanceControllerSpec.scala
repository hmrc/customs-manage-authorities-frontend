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

import base.SpecBase
import config.FrontendAppConfig
import forms.ShowBalanceFormProvider
import models.domain.{AccountStatusOpen, CDSCashBalance, CashAccount, DutyDefermentAccount, DutyDefermentBalance}
import models.{NormalMode, ShowBalance, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{AccountsPage, ShowBalancePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import services.add.CheckYourAnswersValidationService
import views.html.ShowBalanceView

import scala.concurrent.Future

class ShowBalanceControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")
  private lazy val showBalanceRoute = controllers.add.routes.ShowBalanceController.onPageLoad(NormalMode).url

  private val formProvider = new ShowBalanceFormProvider()
  private val form = formProvider()
  val mockValidationService = mock[CheckYourAnswersValidationService]
  val cashAccount = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))
  val dutyDeferment = DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))
  val backLinkRoute: Call = controllers.add.routes.AuthorityEndController.onPageLoad(NormalMode)

  "ShowBalance Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId.value)
        .set(AccountsPage, List(cashAccount, dutyDeferment)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[Navigator].toInstance(new FakeNavigator(backLinkRoute))).build()

      running(application) {

        val request = fakeRequest(GET, showBalanceRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ShowBalanceView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]


        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, accountsLength = 2, NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId.value)
        .set(AccountsPage, List(cashAccount, dutyDeferment)).success.value
        .set(ShowBalancePage, ShowBalance.values.head)(ShowBalance.writes).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[Navigator].toInstance(new FakeNavigator(backLinkRoute))).build()

      running(application) {

        val request = fakeRequest(GET, showBalanceRoute)

        val view = application.injector.instanceOf[ShowBalanceView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(ShowBalance.values.head), accountsLength = 2, NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
      }
    }

    "redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          fakeRequest(POST, showBalanceRoute)
            .withFormUrlEncodedBody(("value", ShowBalance.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.set(AccountsPage, List(cashAccount, dutyDeferment)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[Navigator].toInstance(new FakeNavigator(backLinkRoute))).build()

      running(application) {

        val request =
          fakeRequest(POST, showBalanceRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ShowBalanceView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, accountsLength = 2, NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = fakeRequest(GET, showBalanceRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          fakeRequest(POST, showBalanceRoute)
            .withFormUrlEncodedBody(("value", ShowBalance.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }
  }
}
