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

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration, servicesConfig: ServicesConfig) {

  lazy val host: String = configuration.get[String]("host")

  lazy val feedbackUrl: String = configuration.getOptional[String]("feedback.url").getOrElse("/feedback") +
    configuration.getOptional[String]("feedback.source").getOrElse("/CDS-FIN")

  lazy val helpMakeGovUkBetterUrl: String = configuration.get[String]("urls.helpMakeGovUkBetterUrl")

  lazy val fixedDateTime: Boolean = configuration.get[Boolean]("features.fixed-system-time")
  lazy val xiEoriEnabled: Boolean = configuration.get[Boolean]("features.xi-eori-enabled")
  lazy val timeout: Int = configuration.get[Int]("timeout.timeout")

  lazy val countdown: Int = configuration.get[Int]("timeout.countdown")

  lazy val signOutUrl: String = configuration.get[String]("urls.signOut")
  lazy val govukHome: String = configuration.get[String]("urls.govUkHome")
  lazy val loginUrl: String = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")

  lazy val subscribeCdsUrl: String = configuration.get[String]("urls.cdsSubscribeUrl")
  lazy val appName: String = configuration.get[String]("appName")

  val customsFinancialsFrontendHomepageUrl: String =
    configuration.get[String]("microservice.services.customs-financials-frontend.homepage")

  val authorizedToViewUrl: String = s"${customsFinancialsFrontendHomepageUrl}authorized-to-view"

  lazy val customsDataStore: String = servicesConfig.baseUrl("customs-data-store") +
    configuration.get[String]("microservice.services.customs-data-store.context")

  val emailFrontendUrl: String = servicesConfig.baseUrl("customs-email-frontend") +
    configuration.get[String]("microservice.services.customs-email-frontend.context") +
    configuration.get[String]("microservice.services.customs-email-frontend.url")
}
