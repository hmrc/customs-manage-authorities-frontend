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

package controllers.add

import base.SpecBase
import config.FrontendAppConfig
import forms.EoriDetailsCorrectFormProvider
import models.domain.{
  AccountStatusOpen, CDSAccount, CDSCashBalance, CashAccount, DutyDefermentAccount,
  DutyDefermentBalance, GeneralGuaranteeAccount, GeneralGuaranteeBalance
}
import models.{AuthorityStart, CheckMode, CompanyDetails, EoriDetailsCorrect, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{AccountsPage, AuthorityStartPage, EoriDetailsCorrectPage, EoriNumberPage}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import services.DateTimeService
import viewmodels.EoriDetailsCorrectHelper
import views.html.add.EoriDetailsCorrectView

import java.time.LocalDateTime
import scala.concurrent.Future

class EoriDetailsCorrectControllerSpec extends SpecBase with MockitoSugar {

  "AuthorityDetails Controller" must {

    "return OK and the correct view for a GET" in new SetUp {
      val userAnswers = userAnswer.set(AccountsPage, List(cashAccount)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = fakeRequest(GET, eoriDetailsCorrectRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[EoriDetailsCorrectView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val helper = EoriDetailsCorrectHelper(userAnswers, mockDateTimeService)(messages(application))

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode, backLinkRoute, helper)(request, messages(application), appConfig).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in new SetUp {

      val userAnswersEoriDetails: UserAnswers = UserAnswers("id")
        .set(AccountsPage, selectedAccounts).success.value
        .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
        .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
        .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersEoriDetails)).build()

      running(application) {

        val request = fakeRequest(GET, eoriDetailsCorrectRoute)
        val view: EoriDetailsCorrectView = application.injector.instanceOf[EoriDetailsCorrectView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val helper = EoriDetailsCorrectHelper(userAnswersEoriDetails, mockDateTimeService)(messages(application))

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(EoriDetailsCorrect.values.head),
            NormalMode,
            backLinkRoute,
            helper)(request, messages(application), appConfig).toString
      }
    }

    "redirect to the next page when valid data is submitted" in new SetUp {

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
          fakeRequest(POST, eoriDetailsCorrectRoute)
            .withFormUrlEncodedBody(("value", EoriDetailsCorrect.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "redirect to the next page and clear the AccountsPage selection when No,change EORI is selected" +
      " and form is submitted" in new SetUp {

      val mockSessionRepository: SessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application: Application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(
              new FakeNavigator(controllers.add.routes.EoriNumberController.onPageLoad(CheckMode))
            ),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

      running(application) {

        val request =
          fakeRequest(POST, eoriDetailsCorrectCheckModeSubmitRoute)
            .withFormUrlEncodedBody(("value", EoriDetailsCorrect.No.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.add.routes.EoriNumberController.onPageLoad(CheckMode).url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in new SetUp {

      val userAnswersEoriDetails: UserAnswers = UserAnswers("id")
        .set(AccountsPage, selectedAccounts).success.value
        .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
        .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
        .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersEoriDetails)).build()

      running(application) {

        val request = fakeRequest(POST, eoriDetailsCorrectRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[EoriDetailsCorrectView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val helper = EoriDetailsCorrectHelper(userAnswer, mockDateTimeService)(messages(application))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, NormalMode, backLinkRoute, helper)(request, messages(application), appConfig).toString
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in new SetUp {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = fakeRequest(GET, eoriDetailsCorrectRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in new SetUp {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = fakeRequest(POST, eoriDetailsCorrectRoute)
          .withFormUrlEncodedBody(("value", EoriDetailsCorrect.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }
  }

  trait SetUp {
    def onwardRoute: Call = Call("GET", "/foo")

    lazy val eoriDetailsCorrectRoute: String = controllers.add.routes.EoriDetailsCorrectController.onPageLoad(NormalMode).url
    lazy val eoriDetailsCorrectCheckModeSubmitRoute: String =
      controllers.add.routes.EoriDetailsCorrectController.onPageLoad(CheckMode).url

    val formProvider = new EoriDetailsCorrectFormProvider()
    val form: Form[EoriDetailsCorrect] = formProvider()
    val backLinkRoute: Call = controllers.add.routes.EoriNumberController.onPageLoad(NormalMode)

    val cashAccount: CashAccount = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))
    val dutyDeferment: DutyDefermentAccount = DutyDefermentAccount(
      "67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))
    val generalGuarantee: GeneralGuaranteeAccount = GeneralGuaranteeAccount(
      "54321", "GB000000000000", AccountStatusOpen, Some(GeneralGuaranteeBalance(50.00, 50.00)))
    val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)

    val userAnswer: UserAnswers = UserAnswers("id")
      .set(AccountsPage, selectedAccounts).success.value
      .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
      .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value

    val mockDateTimeService: DateTimeService = mock[DateTimeService]
    when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())
  }
}
