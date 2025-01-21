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

package config

import base.SpecBase
import models.domain.FileRole
import play.api.test.Helpers.running

class FrontendAppConfigSpec extends SpecBase {

  "FrontendAppConfig" should {
    "load all configuration values correctly" in {
      running(application()) {
        appConfig.appName mustBe "customs-manage-authorities-frontend"
        appConfig.feedbackUrl mustBe "https://www.development.tax.service.gov.uk/feedback/CDS-FIN"
        appConfig.xiEoriEnabled mustBe true
        appConfig.emailFrontendUrl mustBe "http://localhost:9898/manage-email-cds/service/customs-finance"
        appConfig.authorizedToViewUrl mustBe "http://localhost:9876/customs/payment-records/authorized-to-view"
        appConfig.customsSecureMessagingBannerEndpoint mustBe "http://localhost:9842/customs/secure-messaging/banner"
        appConfig.manageAuthoritiesServiceUrl mustBe "http://localhost:9000"
        appConfig.signOutUrl mustBe "http://localhost:9553/bas-gateway/sign-out-without-state"
        appConfig.loginUrl mustBe "http://localhost:9553/bas-gateway/sign-in"
        appConfig.loginContinueUrl mustBe "http://localhost:8322"
        appConfig.subscribeCdsUrl mustBe "https://www.tax.service.gov.uk/customs-enrolment-services/cds/subscribe"
        appConfig.govukHome mustBe "https://www.gov.uk"
        appConfig.xClientIdHeader mustBe "c10ef6c6-8ffe-4a45-a159-d707ef90cf07"
        appConfig.customsDataStore mustBe "http://localhost:9893/customs-data-store"
        appConfig.customsFinancialsFrontendHomepageUrl mustBe "http://localhost:9876/customs/payment-records/"
        appConfig.sdesApi mustBe "http://localhost:9754/customs-financials-sdes-stub"
        appConfig.timeout mustBe 900
        appConfig.countdown mustBe 120

        appConfig.helpMakeGovUkBetterUrl mustBe
          "https://signup.take-part-in-research.service.gov.uk?utm_campaign=CDSfinancials&utm_source=Other&utm_medium=other&t=HMRC&id=249"

        val fileRole = FileRole("StandingAuthority")
        appConfig.filesUrl(fileRole) mustBe
          "http://localhost:9754/customs-financials-sdes-stub/files-available/list/StandingAuthority"
      }
    }
  }
}
