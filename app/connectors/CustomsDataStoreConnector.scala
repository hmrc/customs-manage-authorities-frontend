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

package connectors

import config.FrontendAppConfig
import models.{
  CompanyInformation, EmailResponse, EmailResponses, EmailUnverifiedResponse, EmailVerifiedResponse, UndeliverableEmail,
  UnverifiedEmail, XiEoriInformationResponse
}
import models.requests.EoriRequest
import play.api.Logger
import play.api.http.Status.NOT_FOUND
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.HttpClientV2
import utils.Constants.NON_EU_EORI_PREFIXES

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsDataStoreConnector @Inject() (appConfig: FrontendAppConfig, httpClient: HttpClientV2)(implicit
  ec: ExecutionContext
) extends HttpErrorFunctions {

  val log                      = Logger(this.getClass)
  private val baseDataStoreUrl = appConfig.customsDataStore

  def getCompanyName(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val endpoint = s"$baseDataStoreUrl/eori/company-information"
    httpClient
      .get(url"$endpoint")
      .execute[CompanyInformation]
      .map { response =>
        response.consent match {
          case Some("1") => Some(response.name)
          case _         => None
        }
      }
      .recover { case e =>
        log.error(s"Call to data stored failed for getCompanyName exception=$e")
        None
      }
  }

  def retrieveCompanyInformationThirdParty(eori: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val endpoint             = s"$baseDataStoreUrl/eori/company-information-third-party"
    val request: EoriRequest = EoriRequest(eori)

    httpClient
      .post(url"$endpoint")
      .withBody(request)
      .execute[CompanyInformation]
      .map { response =>
        response.consent match {
          case Some("1") => Some(response.name)
          case _         => None
        }
      }
      .recover { case e =>
        log.error(s"Call to data stored failed for retrieveCompanyInformationThirdParty exception=$e")
        None
      }
  }

  def getXiEori(eori: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val endpoint                 = s"$baseDataStoreUrl/eori/xieori-information"
    val isXiEoriEnabled: Boolean = appConfig.xiEoriEnabled

    if (isXiEoriEnabled && NON_EU_EORI_PREFIXES.exists(prefix => eori.startsWith(prefix))) {
      httpClient
        .get(url"$endpoint")
        .execute[XiEoriInformationResponse]
        .map { response =>
          if (response.xiEori.isEmpty) None else Some(response.xiEori)
        }
        .recover { case e =>
          log.error(s"Call to data stored failed for getXiEori exception=$e")
          None
        }
    } else {
      Future.successful(None)
    }
  }

  def getEmail(implicit hc: HeaderCarrier): Future[Either[EmailResponses, Email]] = {
    val endpoint = s"$baseDataStoreUrl/eori/verified-email"
    httpClient
      .get(url"$endpoint")
      .execute[EmailResponse]
      .map {
        case EmailResponse(Some(address), _, None)  => Right(Email(address))
        case EmailResponse(Some(email), _, Some(_)) => Left(UndeliverableEmail(email))
        case _                                      => Left(UnverifiedEmail)
      }
      .recover { case UpstreamErrorResponse(_, NOT_FOUND, _, _) =>
        Left(UnverifiedEmail)
      }
  }

  def unverifiedEmail(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val endpoint = s"$baseDataStoreUrl/subscriptions/unverified-email-display"
    httpClient
      .get(url"$endpoint")
      .execute[EmailUnverifiedResponse]
      .map(_.unVerifiedEmail)
  }

  def verifiedEmail(implicit hc: HeaderCarrier): Future[EmailVerifiedResponse] = {
    val endpoint = s"$baseDataStoreUrl/subscriptions/email-display"
    httpClient
      .get(url"$endpoint")
      .execute[EmailVerifiedResponse]
  }
}
