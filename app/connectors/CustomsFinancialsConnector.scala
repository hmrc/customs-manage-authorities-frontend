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

import config.Service
import models.domain.{AccountWithAuthorities, CDSAccounts, FileRole}
import models.requests._
import models.{CompanyName, EORIValidationError, ErrorResponse}
import play.api.Configuration
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.JsonBodyWritables._
import play.api.libs.ws.BodyWritable
import play.mvc.Http.Status
import services.MetricsReporterService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import utils.StringUtils.emptyString
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsFinancialsConnector @Inject() (
  config: Configuration,
  httpClient: HttpClientV2,
  metricsReporterService: MetricsReporterService
)(implicit ec: ExecutionContext) {

  implicit def jsonBodyWritable[T: Writes]: BodyWritable[T] = BodyWritable(
    json => writeableOf_JsValue.transform(Json.toJson(json)),
    "application/json"
  )

  private val baseUrl = config.get[Service]("microservice.services.customs-financials-api").baseUrl
  private val context = config.get[String]("microservice.services.customs-financials-api.context")

  private val baseApiUrl = s"$baseUrl$context"

  def retrieveAccounts(eori: String)(implicit hc: HeaderCarrier): Future[CDSAccounts] = {
    val retrieveAccountsUrl = s"$baseApiUrl/eori/accounts/"
    val request             = AccountsAndBalancesRequestContainer(
      AccountsAndBalancesRequest(AccountsRequestCommon.generate, AccountsRequestDetail(eori, None, None, None))
    )

    httpClient
      .post(url"$retrieveAccountsUrl")
      .withBody(request)
      .execute[AccountsAndBalancesResponseContainer]
      .map(_.toCdsAccounts(eori))
  }

  def retrieveAccountAuthorities(eori: String)(implicit hc: HeaderCarrier): Future[Seq[AccountWithAuthorities]] = {
    val retrieveAccountAuthoritiesUrl = s"$baseApiUrl/$eori/account-authorities"
    httpClient
      .get(url"$retrieveAccountAuthoritiesUrl")
      .execute[Seq[AccountWithAuthorities]]
  }

  def grantAccountAuthorities(addAuthorityRequest: AddAuthorityRequest, eori: String = emptyString)(implicit
    hc: HeaderCarrier
  ): Future[Boolean] = {
    val grantAccountAuthoritiesUrl = s"$baseApiUrl/$eori/account-authorities/grant"
    httpClient
      .post(url"$grantAccountAuthoritiesUrl")
      .withBody(addAuthorityRequest)
      .execute[HttpResponse]
      .map(_.status == Status.NO_CONTENT)
      .recover { case _ => false }
  }

  def revokeAccountAuthorities(revokeAuthorityRequest: RevokeAuthorityRequest, eori: String = emptyString)(implicit
    hc: HeaderCarrier
  ): Future[Boolean] = {
    val revokeAccountAuthoritiesUrl = s"$baseApiUrl/$eori/account-authorities/revoke"
    httpClient
      .post(url"$revokeAccountAuthoritiesUrl")
      .withBody(revokeAuthorityRequest)
      .execute[HttpResponse]
      .map(_.status == Status.NO_CONTENT)
      .recover { case _ => false }
  }

  def validateEori(eori: String)(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, Boolean]] = {
    val validateEoriUrl      = s"$baseApiUrl/eori/validate"
    val request: EoriRequest = EoriRequest(eori)
    httpClient
      .post(url"$validateEoriUrl")
      .withBody(request)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case Status.OK        => Right(true)
          case Status.NOT_FOUND => Right(false)
          case _                => Left(EORIValidationError)
        }
      }
      .recover {
        case _: NotFoundException => Right(false)
        case _                    => Left(EORIValidationError)
      }
  }

  def retrieveEoriCompanyName()(implicit hc: HeaderCarrier): Future[CompanyName] = {
    val retrieveEoriCompanyNameUrl = s"$baseApiUrl/subscriptions/company-name"
    httpClient
      .get(url"$retrieveEoriCompanyNameUrl")
      .execute[CompanyName]
  }

  def deleteNotification(fileRole: FileRole)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val deleteNotificationUrl = s"$baseApiUrl/eori/notifications/$fileRole"
    metricsReporterService.withResponseTimeLogging("customs-financials-api.delete.notification") {
      httpClient
        .delete(url"$deleteNotificationUrl")
        .execute[HttpResponse]
        .map(_.status == Status.OK)
        .recover { case _ => false }
    }
  }
}
