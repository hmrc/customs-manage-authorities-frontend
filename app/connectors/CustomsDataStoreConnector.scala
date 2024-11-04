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

package connectors

import config.FrontendAppConfig
import models.{
  CompanyInformation, EmailResponse, EmailResponses, EmailUnverifiedResponse,
  EmailVerifiedResponse, UndeliverableEmail, UnverifiedEmail, XiEoriInformationResponse
}
import play.api.Logger
import play.api.http.Status.NOT_FOUND
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsDataStoreConnector @Inject()(appConfig: FrontendAppConfig,
                                          httpClient: HttpClient
                                         )(implicit ec: ExecutionContext) extends HttpErrorFunctions {

  val log = Logger(this.getClass)

  def getCompanyName(eori: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val dataStoreEndpoint = appConfig.customsDataStore + s"/eori/$eori/company-information"
    httpClient.GET[CompanyInformation](dataStoreEndpoint).map(response => {
      response.consent match {
        case "1" => Some(response.name)
        case _ => None
      }
    }).recover { case e =>
      log.error(s"Call to data stored failed for getCompanyName exception=$e")
      None
    }
  }

  def getXiEori(eori: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val dataStoreEndpoint = appConfig.customsDataStore + s"/eori/$eori/xieori-information"
    val isXiEoriEnabled: Boolean = appConfig.xiEoriEnabled

    if (isXiEoriEnabled) {
      httpClient.GET[XiEoriInformationResponse](dataStoreEndpoint).map(
        response => if (response.xiEori.isEmpty) None else Some(response.xiEori)
      ).recover { case e =>
        log.error(s"Call to data stored failed for getXiEori exception=$e")
        None
      }
    } else {
      Future.successful(None)
    }
  }

  def getEmail(eori: String)(implicit hc: HeaderCarrier): Future[Either[EmailResponses, Email]] = {
    val dataStoreEndpoint = appConfig.customsDataStore + s"/eori/$eori/verified-email"
    httpClient.GET[EmailResponse](dataStoreEndpoint).map {
      case EmailResponse(Some(address), _, None) => Right(Email(address))
      case EmailResponse(Some(email), _, Some(_)) => Left(UndeliverableEmail(email))
      case _ => Left(UnverifiedEmail)
    }.recover {
      case UpstreamErrorResponse(_, NOT_FOUND, _, _) => Left(UnverifiedEmail)
    }
  }

  def isEmailUnverified(implicit hc: HeaderCarrier): Future[Option[String]] = {
    httpClient.GET[EmailUnverifiedResponse](appConfig.customsDataStore + "/subscriptions/unverified-email-display")
      .map(res => res.unVerifiedEmail)
  }

  def verifiedEmail(implicit hc: HeaderCarrier): Future[EmailVerifiedResponse] =
    httpClient.GET[EmailVerifiedResponse](appConfig.customsDataStore + "/subscriptions/email-display")

}
