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

import models.Language
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._

class LanguageSwitchControllerSpec extends FreeSpec with MustMatchers with OptionValues with ScalaFutures {

  "switching language" - {

    "when translation is enabled" - {

      "should set the language to Cymraeg" in {

        val application = new GuiceApplicationBuilder()
          .configure(
            "features.welsh-translation" -> true
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.LanguageSwitchController.switchToLanguage(Language.Cymraeg).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          cookies(result).get("PLAY_LANG").value.value mustEqual "cy"
        }
      }

      "should set the language to English" in {

        val application = new GuiceApplicationBuilder()
          .configure(
            "features.welsh-translation" -> true
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.LanguageSwitchController.switchToLanguage(Language.English).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          cookies(result).get("PLAY_LANG").value.value mustEqual "en"
        }
      }
    }

    "when translation is disabled" - {

      "should set the language to English regardless of what is requested" in {

        val application = new GuiceApplicationBuilder()
          .configure(
            "features.welsh-translation" -> false
          )
          .build()

        running(application) {
          val cymraegRequest = FakeRequest(GET, routes.LanguageSwitchController.switchToLanguage(Language.Cymraeg).url)
          val englishRequest = FakeRequest(GET, routes.LanguageSwitchController.switchToLanguage(Language.English).url)

          val cymraegResult = route(application, cymraegRequest).value
          val englishResult = route(application, englishRequest).value

          status(cymraegResult) mustEqual SEE_OTHER
          cookies(cymraegResult).get("PLAY_LANG").value.value mustEqual "en"

          status(englishResult) mustEqual SEE_OTHER
          cookies(englishResult).get("PLAY_LANG").value.value mustEqual "en"
        }
      }
    }
  }
}
