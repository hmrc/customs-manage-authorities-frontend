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

package config

import base.SpecBase
import play.api.test.Helpers.running

class FrontendAppConfigSpec extends SpecBase {

  "FrontendAppConfig" should {

    "include the app name" in new Setup {
      running(app) {
        appConfig.appName mustBe ("customs-manage-authorities-frontend")
      }
    }

    "return true for xiEoriEnabled" in new Setup {
      running(app) {
        appConfig.xiEoriEnabled mustBe true
      }
    }
  }

  trait Setup {
    val app = applicationBuilder(userAnswers = None).build()
    val appConfig = app.injector.instanceOf[FrontendAppConfig]
  }
}
