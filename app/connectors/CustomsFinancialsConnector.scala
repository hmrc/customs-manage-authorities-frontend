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

import config.Service
import models.domain.{AccountWithAuthorities, CDSAccounts}
import models.requests._
import models.{CompanyName, EORIValidationError, EmailUnverifiedResponse, ErrorResponse, EmailVerifiedResponse}
import play.api.Configuration
import play.mvc.Http.Status
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, HttpResponse, NotFoundException}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsFinancialsConnector @Inject()(
                                            config: Configuration,
                                            httpClient: HttpClient
                                          )(implicit ec: ExecutionContext) extends HttpErrorFunctions {

  private val baseUrl = config.get[Service]("microservice.services.customs-financials-api")
  private val context = config.get[String]("microservice.services.customs-financials-api.context")

  def retrieveAccounts(eori: String)(implicit hc: HeaderCarrier): Future[CDSAccounts] = {
    val request = AccountsAndBalancesRequestContainer(AccountsAndBalancesRequest(
      AccountsRequestCommon.generate, AccountsRequestDetail(eori, None, None, None)))

    httpClient.POST[AccountsAndBalancesRequestContainer,
      AccountsAndBalancesResponseContainer](baseUrl.toString + context + "/eori/accounts/", request).map(_.toCdsAccounts(eori))
  }

  def retrieveAccountAuthorities(eori: String)(implicit hc: HeaderCarrier): Future[Seq[AccountWithAuthorities]] = {
    httpClient.GET[Seq[AccountWithAuthorities]](baseUrl.toString + context + s"/$eori/account-authorities")
  }

  def grantAccountAuthorities(addAuthorityRequest: AddAuthorityRequest, eori: String = "")(implicit hc: HeaderCarrier): Future[Boolean] = {
    httpClient.POST[AddAuthorityRequest, HttpResponse](baseUrl.toString + context + s"/$eori/account-authorities/grant", addAuthorityRequest)
      .map(_.status == Status.NO_CONTENT).recover { case _ => false }
  }

  def revokeAccountAuthorities(revokeAuthorityRequest: RevokeAuthorityRequest, eori: String = "")(implicit hc: HeaderCarrier): Future[Boolean] = {
    httpClient.POST[RevokeAuthorityRequest, HttpResponse](baseUrl.toString + context + s"/$eori/account-authorities/revoke", revokeAuthorityRequest)
      .map(_.status == Status.NO_CONTENT).recover { case _ => false }
  }

  def validateEori(eori: String)(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, Boolean]] = {
    httpClient.GET[HttpResponse](baseUrl.toString + context + s"/eori/$eori/validate")
      .map(response => { response.status match {
          case Status.OK => Right(true)
          case Status.NOT_FOUND => Right(false)
          case _ => Left(EORIValidationError)
        }
      }).recover {
        case _: NotFoundException => Right(false)
        case _ => Left(EORIValidationError)
      }
  }

  def retrieveEoriCompanyName()(implicit hc: HeaderCarrier): Future[CompanyName] = {
    httpClient.GET[CompanyName](baseUrl.toString + context + "/subscriptions/company-name")
  }

  def isEmailUnverified(implicit hc: HeaderCarrier): Future[Option[String]] = {
    httpClient.GET[EmailUnverifiedResponse](baseUrl.toString + context + "/subscriptions/unverified-email-display")
      .map( res => res.unVerifiedEmail)
  }

  def verifiedEmail(implicit hc: HeaderCarrier): Future[EmailVerifiedResponse] =
    httpClient.GET[EmailVerifiedResponse](baseUrl.toString + context +  "/subscriptions/email-display")
}
