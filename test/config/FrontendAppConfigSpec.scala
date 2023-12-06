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

    "return false for fixedDateTime" in new Setup {
      running(app) {
        appConfig.fixedDateTime mustBe false
      }
    }

    "return email frontend url" in new Setup {
      running(app) {
        appConfig.emailFrontendUrl mustBe "http://localhost:9898/manage-email-cds/service/customs-finance"
      }
    }

    "return contact frontend url" in new Setup {
      running(app) {
        appConfig.contactFrontendUrl mustBe "http://localhost:9250/contact/report-technical-problem"
      }
    }

    "return customs data store base url and context" in new Setup {
      running(app) {
        appConfig.customsDataStore mustBe "http://localhost:9893/customs-data-store"
      }
    }

    "return customs financials frontend homepage url" in new Setup {
      running(app) {
        appConfig.customsFinancialsFrontendHomepageUrl mustBe "http://localhost:9876/customs/payment-records/"
      }
    }
  }

  trait Setup {
    val app = applicationBuilder(userAnswers = None).build()
    val appConfig = app.injector.instanceOf[FrontendAppConfig]
  }
}
