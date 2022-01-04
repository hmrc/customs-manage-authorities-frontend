/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.AuthorisedUserFormProvider
import models.UserAnswers
import models.domain.{AccountStatusOpen, AccountWithAuthoritiesWithId, AuthorisedUser, CdsCashAccount, StandingAuthority}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.remove.RemoveAuthorisedUserPage
import play.api.{Application, inject}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AccountAndAuthority, AuthoritiesCacheService, NoAccount, NoAuthority}
import viewmodels.RemoveViewModel
import views.html.remove.RemoveAuthorisedUserView

import java.time.LocalDate
import scala.concurrent.Future

class RemoveAuthorisedUserControllerSpec extends SpecBase with MockitoSugar {

  "onPageLoad" should {
    "return error page when no authority found in the cache" in new Setup {
      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAuthority)))

      val app: Application = application()

      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TechnicalDifficulties.onPageLoad.url
      }
    }

    "return error page when no account found in the cache" in new Setup {
      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAccount)))

      val app: Application = application()


      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TechnicalDifficulties.onPageLoad.url
      }
    }

    "return a pre-populated form if the user-answers already present" in new Setup {
      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      val app: Application = application(emptyUserAnswers.set(RemoveAuthorisedUserPage("a", "b"), AuthorisedUser("test", "test")).get)
      val appConfig = app.injector.instanceOf[FrontendAppConfig]

      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe OK
        contentAsString(result) mustBe
          view(app)(form(app)().fill(AuthorisedUser("test", "test")),
            RemoveViewModel("a", "b", accountsWithAuthoritiesWithId, standingAuthority)
          )(getRequest, messages(app), appConfig).toString()
      }
    }

    "return OK without a pre-populated form if the user-answers empty" in new Setup {
      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      val app: Application = application()
      val appConfig = app.injector.instanceOf[FrontendAppConfig]

      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe OK
        contentAsString(result) mustBe
          view(app)(form(app)(),
            RemoveViewModel("a", "b", accountsWithAuthoritiesWithId, standingAuthority)
          )(getRequest, messages(app), appConfig).toString()
      }
    }
  }

  "onSubmit" should {
    "return error page when no authority found in the cache" in new Setup {
      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAuthority)))

      val app: Application = application()

      running(app) {
        val result = route(app, postRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TechnicalDifficulties.onPageLoad.url
      }
    }

    "return error page when no account found in the cache" in new Setup {
      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAccount)))

      val app: Application = application()


      running(app) {
        val result = route(app, postRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TechnicalDifficulties.onPageLoad.url
      }
    }

    "return BAD_REQUEST when invalid data submitted" in new Setup {
      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      val app: Application = application()


      running(app) {
        val result = route(app, postRequest.withFormUrlEncodedBody("invalid" -> "data")).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return SEE_OTHER on successful submission" in new Setup {
      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val app: Application = application()

      running(app) {
        val result = route(app, postRequest.withFormUrlEncodedBody("fullName" -> "testing", "jobRole" -> "testing2")).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.remove.routes.RemoveCheckYourAnswers.onSubmit("a", "b").url
      }
    }

  }

  trait Setup {
    val mockAuthoritiesCacheService: AuthoritiesCacheService = mock[AuthoritiesCacheService]
    val mockSessionRepository: SessionRepository = mock[SessionRepository]

    val startDate: LocalDate = LocalDate.parse("2020-03-01")
    val endDate: LocalDate = LocalDate.parse("2020-04-01")
    val standingAuthority: StandingAuthority = StandingAuthority("EORI", startDate, viewBalance = false)
    val accountsWithAuthoritiesWithId: AccountWithAuthoritiesWithId =
      AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority))

    val getRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, controllers.remove.routes.RemoveAuthorisedUserController.onPageLoad("a", "b").url)

    val postRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(POST, controllers.remove.routes.RemoveAuthorisedUserController.onSubmit("a", "b").url)

    def application(userAnswers: UserAnswers = emptyUserAnswers): Application = applicationBuilder(Some(userAnswers)).overrides(
      inject.bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
      inject.bind[SessionRepository].toInstance(mockSessionRepository)
    ).build()

    def form(app: Application): AuthorisedUserFormProvider = app.injector.instanceOf[AuthorisedUserFormProvider]

    def view(app: Application): RemoveAuthorisedUserView = app.injector.instanceOf[RemoveAuthorisedUserView]
  }
}
