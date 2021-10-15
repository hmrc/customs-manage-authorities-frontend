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

package controllers.edit

import base.SpecBase
import forms.AuthorisedUserFormProvider
import models.domain.AuthorisedUser
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.edit.EditAuthorisedUserPage
import play.api.{Application, inject}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.edit.EditAuthorisedUserView

import scala.concurrent.Future

class EditAuthorisedUserControllerSpec extends SpecBase with MockitoSugar {

  "onPageLoad" should {
    "return OK with pre-populated form if answers present" in new Setup {
      val app: Application = applicationBuilder(emptyUserAnswers.set(EditAuthorisedUserPage("a", "b"), AuthorisedUser("test", "test2")).toOption).build()
      val form: AuthorisedUserFormProvider = app.injector.instanceOf[AuthorisedUserFormProvider]
      val view: EditAuthorisedUserView = app.injector.instanceOf[EditAuthorisedUserView]

      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe OK
        contentAsString(result) mustBe view(form().fill(AuthorisedUser("test", "test2")), "a", "b")(getRequest, messages(app)).toString()
      }
    }

    "return OK without pre-populated form if answers not present" in new Setup {
      val app: Application = applicationBuilder(Some(emptyUserAnswers)).build()
      val form: AuthorisedUserFormProvider = app.injector.instanceOf[AuthorisedUserFormProvider]
      val view: EditAuthorisedUserView = app.injector.instanceOf[EditAuthorisedUserView]

      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe OK
        contentAsString(result) mustBe view(form(), "a", "b")(getRequest, messages(app)).toString()
      }
    }
  }

  "onSubmit" should {
    "return BAD_REQUEST on invalid request" in new Setup {

      val app: Application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(app) {
        val result = route(app, postRequest.withFormUrlEncodedBody("invalid" -> "date")).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return SEE_OTHER on valid submission" in new Setup {
      val mockSessionRepository: SessionRepository = mock[SessionRepository]

      val app: Application = applicationBuilder(Some(emptyUserAnswers)).overrides(
        inject.bind[SessionRepository].toInstance(mockSessionRepository)
      ).build()

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      running(app) {
        val result = route(app, postRequest.withFormUrlEncodedBody("fullName" -> "testing", "jobRole" -> "testing2")).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.EditCheckYourAnswersController.onPageLoad("a", "b").url
      }
    }
  }

  trait Setup {

    val getRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, controllers.edit.routes.EditAuthorisedUserController.onPageLoad("a", "b").url)

    val postRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(POST, controllers.edit.routes.EditAuthorisedUserController.onSubmit("a", "b").url)


  }
}
