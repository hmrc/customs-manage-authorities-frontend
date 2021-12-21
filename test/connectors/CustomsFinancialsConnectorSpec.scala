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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import models._
import models.domain._
import models.requests._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{EitherValues, MustMatchers, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper
import java.time.LocalDate

class CustomsFinancialsConnectorSpec extends SpecBase
  with WireMockHelper
  with ScalaFutures
  with MustMatchers
  with IntegrationPatience
  with EitherValues
  with OptionValues
  with MockitoSugar {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.customs-financials-api.port" -> server.port)
      .build()

  ".retrieveAccounts" must {
    "return accounts" in {
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

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsFinancialsConnector]

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
    "return account authorities" in {
      val accountAuthorities = Seq(AccountWithAuthorities(
        domain.CdsCashAccount, "12345", Some(AccountStatusOpen), Seq.empty
      ))

      val app = application

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

        val connector = app.injector.instanceOf[CustomsFinancialsConnector]

        server.stubFor(
          get(urlEqualTo("/customs-financials-api/account-authorities"))
            .willReturn(ok(response))
        )
        val result = connector.retrieveAccountAuthorities().futureValue
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
        viewBalance = true
      ),
      AuthorisedUser("name", "job")
    )

    "return success response" in {
      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsFinancialsConnector]

        server.stubFor(
          post(urlEqualTo("/customs-financials-api/account-authorities/grant"))
            .willReturn(noContent())
        )
        val result = connector.grantAccountAuthorities(request).futureValue
        result mustBe true
      }
    }

    "return failure response" in {
      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsFinancialsConnector]

        server.stubFor(
          post(urlEqualTo("/customs-financials-api/account-authorities/grant"))
            .willReturn(serverError())
        )
        val result = connector.grantAccountAuthorities(request).futureValue
        result mustBe false
      }
    }

    "handle errors" in {
      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsFinancialsConnector]

        server.stubFor(
          post(urlEqualTo("/customs-financials-api/account-authorities/grant"))
            .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        )
        val result = connector.grantAccountAuthorities(request).futureValue
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

    "return success response" in {
      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsFinancialsConnector]

        server.stubFor(
          post(urlEqualTo("/customs-financials-api/account-authorities/revoke"))
            .willReturn(noContent())
        )
        val result = connector.revokeAccountAuthorities(request).futureValue
        result mustBe true
      }
    }

    "return failure response" in {
      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsFinancialsConnector]

        server.stubFor(
          post(urlEqualTo("/customs-financials-api/account-authorities/revoke"))
            .willReturn(serverError())
        )
        val result = connector.revokeAccountAuthorities(request).futureValue
        result mustBe false
      }
    }

    "handle errors" in {
      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsFinancialsConnector]

        server.stubFor(
          post(urlEqualTo("/customs-financials-api/account-authorities/revoke"))
            .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        )
        val result = connector.revokeAccountAuthorities(request).futureValue
        result mustBe false
      }
    }
  }

  ".validateEori" must {
      "return true for valid eori" in {
        val app = application

        running(app) {
          val connector = app.injector.instanceOf[CustomsFinancialsConnector]
          server.stubFor(
            get(urlEqualTo("/customs-financials-api/eori/121312/validate"))
              .willReturn(ok())
          )
          val result = connector.validateEori(121312).futureValue
          result mustBe Right(true)
        }
      }
    "return false for not found" in {
      val app = application

      running(app) {
        val connector = app.injector.instanceOf[CustomsFinancialsConnector]
        server.stubFor(
          get(urlEqualTo("/customs-financials-api/eori/121312/validate"))
            .willReturn(notFound())
        )
        val result = connector.validateEori(121312).futureValue
        result mustBe Right(false)
      }
    }
  }
}
