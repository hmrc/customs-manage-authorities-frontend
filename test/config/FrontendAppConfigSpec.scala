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
    "load all configuration values correctly" in new Setup {
      running(app) {
        appConfig.appName mustBe "customs-manage-authorities-frontend"
        appConfig.feedbackUrl mustBe "http://localhost:9514/feedback/CDS-FIN"
        appConfig.xiEoriEnabled mustBe true
        appConfig.euEoriEnabled mustBe false

        appConfig.emailFrontendUrl mustBe "http://localhost:9898/manage-email-cds/service/customs-finance"
        appConfig.authorityToUseUrl mustBe "http://localhost:9876/customs/payment-records/authority-to-use"
        appConfig.customsSecureMessagingBannerEndpoint mustBe "http://localhost:9842/customs/secure-messaging/banner"
        appConfig.manageAuthoritiesServiceUrl mustBe "http://localhost:9000"
        appConfig.signOutUrl mustBe "http://localhost:9553/bas-gateway/sign-out-without-state"
        appConfig.loginUrl mustBe "http://localhost:9553/bas-gateway/sign-in"
        appConfig.loginContinueUrl mustBe "http://localhost:8322"
        appConfig.subscribeCdsUrl mustBe "https://www.tax.service.gov.uk/customs-enrolment-services/cds/subscribe"
        appConfig.govukHome mustBe "https://www.gov.uk"
        appConfig.helpMakeGovUkBetterUrl mustBe
          "https://survey.take-part-in-research.service.gov.uk/jfe/form/SV_74GjifgnGv6GsMC?Source=BannerList_HMRC_CDS_MIDVA"

        appConfig.xClientIdHeader mustBe "c10ef6c6-8ffe-4a45-a159-d707ef90cf07"
        appConfig.timeout mustBe 900
        appConfig.countdown mustBe 120

        appConfig.customsDataStore mustBe "http://localhost:9893/customs-data-store"
        appConfig.customsFinancialsFrontendHomepageUrl mustBe "http://localhost:9876/customs/payment-records/"
        appConfig.sdesApi mustBe "http://localhost:9754/customs-financials-sdes-stub"

        val fileRole = FileRole("StandingAuthority")
        appConfig.filesUrl(fileRole) mustBe
          "http://localhost:9754/customs-financials-sdes-stub/files-available/list/StandingAuthority"
      }
    }
  }

  trait Setup {
    val app       = applicationBuilder(userAnswers = None).build()
    val appConfig = app.injector.instanceOf[FrontendAppConfig]
  }
}
