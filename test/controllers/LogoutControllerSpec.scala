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

package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._

class LogoutControllerSpec extends SpecBase {

  private val expectedSignoutUrl = "http://localhost:9553/bas-gateway/sign-out-without-state"

  "logout" must {
    "redirect the user to logout with the continue as the feedback survey url" in {
      val application = applicationBuilder(userAnswers = None)
        .configure("feedback.url" -> "/feedback-continue", "feedback.source" -> "/source")
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.LogoutController.logout.url)
        val result = route(application, request).value

        redirectLocation(result).value mustEqual s"${expectedSignoutUrl}?continue=%2Ffeedback-continue%2Fsource"
      }
    }
  }

  "logoutNoSurvey" must {
    "redirect the user to logout with no continue location" in {
      val application = applicationBuilder(userAnswers = None)
        .build()
      running(application) {
        val request = FakeRequest(GET, routes.LogoutController.logoutNoSurvey.url)
        val result = route(application, request).value

        redirectLocation(result).value mustEqual expectedSignoutUrl
      }
    }
  }
}
