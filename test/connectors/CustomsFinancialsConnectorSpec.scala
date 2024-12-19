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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import models._
import models.domain._
import models.requests._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

import java.time.LocalDate

class CustomsFinancialsConnectorSpec
    extends SpecBase
    with WireMockHelper
    with ScalaFutures
    with IntegrationPatience
    with EitherValues
    with OptionValues
    with MockitoSugar {

  implicit private lazy val hc: HeaderCarrier        = HeaderCarrier()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.customs-financials-api.port" -> server.port)
      .build()

  ".retrieveAccounts" must {
    "return accounts" in new Setup {
      val response =
        """
          |{
          |  "accountsAndBalancesResponse": {
          |    "responseCommon": {
          |      "status": "OK",
          |      "statusText": "a",
          |      "processingDate": "2018-08-02T16:44:16Z"
          |    },
          |    "responseDetail": {
          |      "EORINo": "EORIXXXXX0",
          |      "referenceDate": "2018-08-02T16:44:16Z",
          |      "dutyDefermentAccount": [],
          |      "generalGuaranteeAccount": [],
          |      "cdsCashAccount": [
          |        {
          |          "account": {
          |            "number": "12345",
          |            "type": "CDSCash",
          |            "accountStatus": "Open",
          |            "owner": "GB123456789012",
          |            "viewBalanceIsGranted": true
          |          },
          |          "availableAccountBalance": "100.00"
          |        }
          |      ]
          |    }
          |  }
          |}
          |""".stripMargin

      val expected = CDSAccounts(
        "GB123456789012",
        List(CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))))
      )

      running(app) {

        server.stubFor(
          post(urlEqualTo("/customs-financials-api/eori/accounts/"))
            .willReturn(ok(response))
        )
        val result = connector.retrieveAccounts("GB123456789012").futureValue
        result mustBe expected
      }
    }
  }

  ".retrieveAccountAuthorities" must {
    "return account authorities" in new Setup {
      val accountAuthorities = Seq(
        AccountWithAuthorities(
          domain.CdsCashAccount,
          "12345",
          Some(AccountStatusOpen),
          Seq.empty
        )
      )

      val response =
        """
          |[
          |   {
          |       "accountType":"CDSCash",
          |       "accountNumber":"12345",
          |       "accountStatus":"Open",
          |       "authorities":[]
          |   }
          |]
          |""".stripMargin

      running(app) {
        server.stubFor(
          get(urlEqualTo("/customs-financials-api/()/account-authorities"))
            .willReturn(ok(response))
        )

        val result = connector.retrieveAccountAuthorities((): Unit).futureValue

        result mustBe accountAuthorities
      }
    }
  }

  ".grantAccountAuthorities" must {

    val request = AddAuthorityRequest(
      Accounts(
        Some("12345"),
        Seq("67890"),
        None
      ),
      StandingAuthority(
        "GB123456789012",
        LocalDate.now(),
        Option(LocalDate.now().plusDays(1)),
        viewBalance = true
      ),
      AuthorisedUser("name", "job")
    )

    "return success response" in new Setup {

      running(app) {
        server.stubFor(
          post(urlEqualTo("/customs-financials-api/someEori/account-authorities/grant"))
            .willReturn(noContent())
        )
        val result = connector.grantAccountAuthorities(request, "someEori").futureValue
        result mustBe true
      }
    }

    "return failure response" in new Setup {

      running(app) {
        server.stubFor(
          post(urlEqualTo("/customs-financials-api/someEori/account-authorities/grant"))
            .willReturn(serverError())
        )
        val result = connector.grantAccountAuthorities(request, "someEori").futureValue
        result mustBe false
      }
    }

    "handle errors" in new Setup {

      running(app) {
        server.stubFor(
          post(urlEqualTo("/customs-financials-api/someEori/account-authorities/grant"))
            .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        )
        val result = connector.grantAccountAuthorities(request, "someEori").futureValue
        result mustBe false
      }
    }
  }

  ".revokeAccountAuthorities" must {

    val request = RevokeAuthorityRequest(
      "12345",
      domain.CdsCashAccount,
      "authorisedEori",
      AuthorisedUser("name", "job")
    )

    "return success response" in new Setup {

      running(app) {
        server.stubFor(
          post(urlEqualTo("/customs-financials-api/someEori/account-authorities/revoke"))
            .willReturn(noContent())
        )
        val result = connector.revokeAccountAuthorities(request, "someEori").futureValue
        result mustBe true
      }
    }

    "return failure response" in new Setup {

      running(app) {
        server.stubFor(
          post(urlEqualTo("/customs-financials-api/someEori/account-authorities/revoke"))
            .willReturn(serverError())
        )
        val result = connector.revokeAccountAuthorities(request, "someEori").futureValue
        result mustBe false
      }
    }

    "handle errors" in new Setup {

      running(app) {
        server.stubFor(
          post(urlEqualTo("/customs-financials-api/someEori/account-authorities/revoke"))
            .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        )
        val result = connector.revokeAccountAuthorities(request, "someEori").futureValue
        result mustBe false
      }
    }
  }

  ".validateEori" must {
    "return true for valid eori" in new Setup {

      running(app) {

        server.stubFor(
          get(urlEqualTo("/customs-financials-api/eori/121312/validate"))
            .willReturn(ok())
        )

        val result = connector.validateEori(eoriNumber).futureValue

        result mustBe Right(true)
      }
    }

    "return false for not found" in new Setup {

      running(app) {

        server.stubFor(
          get(urlEqualTo("/customs-financials-api/eori/121312/validate"))
            .willReturn(notFound())
        )

        val result = connector.validateEori(eoriNumber).futureValue

        result mustBe Right(false)
      }
    }

    "return validation error for internal server error" in new Setup {

      running(app) {

        server.stubFor(
          get(urlEqualTo("/customs-financials-api/eori/121312/validate"))
            .willReturn(serverError())
        )

        val result = connector.validateEori(eoriNumber).futureValue

        result mustBe Left(EORIValidationError)
      }
    }
  }

  ".retrieve eori company name" must {
    "return success response" in new Setup {

      running(app) {

        val json   = """{"name" : "ABCD", "consent":"1"}"""
        server.stubFor(
          get(urlEqualTo("/customs-financials-api/subscriptions/company-name"))
            .willReturn(ok(json))
        )
        val result = connector.retrieveEoriCompanyName().futureValue
        result.name mustBe Some("ABCD")
      }
    }
  }

  "delete notifications should return a boolean based on the result" in new Setup {

    running(app) {

      server.stubFor(
        delete(urlEqualTo(s"/customs-financials-api/eori/$eoriNumber/notifications/${FileRole.StandingAuthority}"))
          .willReturn(ok())
      )

      val result = connector.deleteNotification(eoriNumber, FileRole.StandingAuthority)(hc).futureValue
      result mustBe true
    }
  }

  trait Setup {
    val eoriNumber: String                    = "121312"
    val app: Application                      = application
    val connector: CustomsFinancialsConnector = app.injector.instanceOf[CustomsFinancialsConnector]
  }
}
