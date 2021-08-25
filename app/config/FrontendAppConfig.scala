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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  lazy val host: String = configuration.get[String]("host")

  lazy val feedbackUrl: String = configuration.getOptional[String]("feedback.url").getOrElse("/feedback") +
    configuration.getOptional[String]("feedback.source").getOrElse("/CDS-FIN")

  lazy val reportAProblem: Boolean = configuration.getOptional[Boolean]("features.report-a-problem").getOrElse(false)
  lazy val fixedDateTime: Boolean = configuration.get[Boolean]("features.fixed-system-time")

  lazy val timeout: Int = configuration.get[Int]("timeout.timeout")
  lazy val countdown: Int = configuration.get[Int]("timeout.countdown")

  lazy val signOutUrl: String = configuration.get[String]("urls.signOut")

  lazy val govukHome: String = configuration.get[String]("urls.govUkHome")
  lazy val loginUrl: String = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")

  lazy val authUrl: String = configuration.get[Service]("auth").baseUrl

  lazy val languageTranslationEnabled: Boolean = configuration.get[Boolean]("features.welsh-translation")

  lazy val origin: String = configuration.get[String]("origin")

  lazy val cookies: String         = host + configuration.get[String]("urls.footer.cookies")
  lazy val privacy: String         = host + configuration.get[String]("urls.footer.privacy")
  lazy val termsConditions: String = host + configuration.get[String]("urls.footer.termsConditions")
  lazy val govukHelp: String       = configuration.get[String]("urls.footer.govukHelp")
  lazy val accessibilityStatement: String = configuration.get[String]("urls.footer.accessibility")
  lazy val registerCdsUrl: String = configuration.get[String]("urls.cdsRegisterUrl")
  lazy val subscribeCdsUrl: String = configuration.get[String]("urls.cdsSubscribeUrl")
  lazy val applicationStatusCdsUrl: String = configuration.get[String]("urls.applicationStatusUrl")
  lazy val appName: String         = configuration.get[String]("appName")

  val customsFinancialsFrontendHomepageUrl: String = configuration.get[String]("microservice.services.customs-financials-frontend.homepage")
  lazy val contactFrontendUrl: String = configuration.get[String]("urls.contactFrontendUrl")
}
