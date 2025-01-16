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

package controllers.edit

import base.SpecBase
import config.FrontendAppConfig
import forms.AuthorityEndFormProvider
import models.AuthorityEnd
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.edit.EditAuthorityEndPage
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import repositories.SessionRepository
import views.html.edit.EditAuthorityEndView

import scala.concurrent.Future

class EditAuthorityEndControllerSpec extends SpecBase {

  "onPageLoad" must {
    "return OK with pre-populated values if form has some values" in new SetUp {
      val app: Application = applicationBuilder(
        emptyUserAnswers.set(EditAuthorityEndPage("123", "12345"), AuthorityEnd.Setdate).toOption
      ).build()

      val appConfig: FrontendAppConfig   = app.injector.instanceOf[FrontendAppConfig]
      val form: AuthorityEndFormProvider = app.injector.instanceOf[AuthorityEndFormProvider]
      val view: EditAuthorityEndView     = app.injector.instanceOf[EditAuthorityEndView]

      running(app) {
        val result = route(app, getRequest).value
        status(result) shouldBe OK

        contentAsString(result) mustBe view(form().fill(AuthorityEnd.Setdate), "123", "12345")(
          getRequest,
          messages,
          appConfig
        ).toString()
      }
    }

    "return OK without pre-populated values if form has no values" in new SetUp {
      val app: Application = applicationBuilder(Option(emptyUserAnswers)).build()

      val appConfig: FrontendAppConfig   = app.injector.instanceOf[FrontendAppConfig]
      val form: AuthorityEndFormProvider = app.injector.instanceOf[AuthorityEndFormProvider]
      val view: EditAuthorityEndView     = app.injector.instanceOf[EditAuthorityEndView]

      running(app) {
        val result = route(app, getRequest).value
        status(result) shouldBe OK

        contentAsString(result) mustBe view(form(), "123", "12345")(getRequest, messages, appConfig).toString()
      }
    }
  }

  "onSubmit" must {
    "redirect to next page if form has no error" in new SetUp {
      val app: Application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      running(app) {
        val result = route(app, postRequest.withFormUrlEncodedBody("value" -> "setDate")).value
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).value mustBe
          routes.EditAuthorityEndDateController.onPageLoad("123", "12345").url
      }
    }

    "return BAD_REQUEST if form has errors" in new SetUp {
      val app: Application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(app) {
        val result = route(app, postRequest.withFormUrlEncodedBody("invalid" -> "field_value")).value
        status(result) shouldBe BAD_REQUEST
      }
    }
  }

  trait SetUp {
    val mockSessionRepository: SessionRepository = mock[SessionRepository]

    val getRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, controllers.edit.routes.EditAuthorityEndController.onPageLoad("123", "12345").url)

    val postRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(POST, controllers.edit.routes.EditAuthorityEndController.onSubmit("123", "12345").url)
  }
}
