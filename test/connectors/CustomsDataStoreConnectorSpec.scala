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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import config.FrontendAppConfig
import models.{UndeliverableEmail, UnverifiedEmail}
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{EitherValues, MustMatchers, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

class CustomsDataStoreConnectorSpec extends SpecBase
  with WireMockHelper
  with ScalaFutures
  with MustMatchers
  with IntegrationPatience
  with EitherValues
  with OptionValues
  with MockitoSugar {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  ".getCompanyName" must {
    "return companyName when consent is defined as 1(True)" in new Setup {
      val response =
        """
          |{
          |   "name":"Tony Stark",
          |   "consent":"1",
          |   "address":{
          |      "streetAndNumber":"86 Mysore Road",
          |      "city":"London",
          |      "postalCode":"SW11 5RZ",
          |      "countryCode":"GB"
          |   }
          |}
          |""".stripMargin

      val expectedResult = Some("Tony Stark")

      running(app) {
        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/company-information"))
            .willReturn(ok(response))
        )
        val result = connector.getCompanyName("GB123456789012").futureValue
        result mustBe expectedResult
      }
    }


    "return None when consent is defined as 0(False)" in new Setup {
      val response =
        """
          |{
          |   "name":"Tony Stark",
          |   "consent":"0",
          |   "address":{
          |      "streetAndNumber":"86 Mysore Road",
          |      "city":"London",
          |      "postalCode":"SW11 5RZ",
          |      "countryCode":"GB"
          |   }
          |}
          |""".stripMargin

      running(app) {
        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/company-information"))
            .willReturn(ok(response))
        )
        val result = connector.getCompanyName("GB123456789012").futureValue
        result mustBe expected
      }
    }

    "return None when consent is empty" in new Setup {
      val response =
        """
          |{
          |   "name":"Tony Stark",
          |   "consent": None,
          |   "address":{
          |      "streetAndNumber":"86 Mysore Road",
          |      "city":"London",
          |      "postalCode":"SW11 5RZ",
          |      "countryCode":"GB"
          |   }
          |}
          |""".stripMargin

      running(app) {
        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/company-information"))
            .willReturn(ok(response))
        )
        val result = connector.getCompanyName("GB123456789012").futureValue
        result mustBe expected
      }
    }
  }

  ".get XiEori" must {
    "return xi eori when it is returned from data store" in new Setup {
      val response =
        """
          |{
          |   "xiEori":"XI1234567",
          |   "consent":"1",
          |   "address":{
          |      "pbeAddressLine1":"86 street",
          |      "pbeAddressLine2":"London",
          |      "pbeAddressLine3":"GB"
          |   }
          |}
          |""".stripMargin

      val expectedResult = Some("XI1234567")

      running(app) {

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/xieori-information"))
            .willReturn(ok(response))
        )
        val result = connector.getXiEori("GB123456789012").futureValue
        result mustBe expectedResult
      }
    }

    "return None when empty XI EORI value is returned from data store" in new Setup {
      val response =
        """
          |{
          |   "xiEori":"",
          |   "consent":"1",
          |   "address":{
          |      "pbeAddressLine1":"86 street",
          |      "pbeAddressLine2":"London",
          |      "pbeAddressLine3":"GB"
          |   }
          |}
          |""".stripMargin

      running(app) {

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/xieori-information"))
            .willReturn(ok(response))
        )
        val result = connector.getXiEori("GB123456789012").futureValue
        result mustBe expected
      }
    }

    "return None when error response is returned" in new Setup {

      running(app) {

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/xieori-information"))
            .willReturn(serverError())
        )
        val result = connector.getXiEori("GB123456789012").futureValue
        result mustBe expected
      }
    }

    "return None when feature flag is false" in new Setup {
      val mockAppConfig = mock[FrontendAppConfig]
      when(mockAppConfig.xiEoriEnabled).thenReturn(false)

      running(app) {
        val result = connector.getXiEori("GB123456789012").futureValue
        result mustBe expected
      }
    }
  }

  "getEmail" should {
    "return an email address when the request is successful and undeliverable is not present in the response" in new Setup {
      val emailResponse =
        """{
          |"address": "some@email.com"
          |}""".stripMargin

      running(app) {

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/verified-email"))
            .willReturn(ok(emailResponse))
        )
        val result = connector.getEmail("GB123456789012").futureValue
        result mustBe Right(Email("some@email.com"))
      }
    }

    "return no email address when the request is successful and undeliverable is present in the response" in new Setup {
      val emailResponse =
        """{
          |"address": "some@email.com",
          |"timestamp": "2022-10-10T11:22:22Z",
          |"undeliverableInformation": {
          |"subject": "someSubject"
          |}
          |}""".stripMargin

      running(app) {
        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/verified-email"))
            .willReturn(ok(emailResponse))
        )

        val result = connector.getEmail("GB123456789012").futureValue
        result mustBe Left(UndeliverableEmail("some@email.com"))
      }
    }

    "return unverifiedEmail when the request is successful and email address is not present in the response" in new Setup {
      val emailResponse = """{}"""

      running(app) {
        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/verified-email"))
            .willReturn(ok(emailResponse))
        )

        val result = connector.getEmail("GB123456789012").futureValue
        result mustBe Left(UnverifiedEmail)
      }
    }

    "return no email when a NOT_FOUND response is returned" in new Setup {

      running(app) {

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/verified-email"))
            .willReturn(notFound)
        )
        val result = connector.getEmail("GB123456789012").futureValue
        result mustBe Left(UnverifiedEmail)
      }
    }
  }

  trait Setup {

    private def application: Application =
      new GuiceApplicationBuilder()
        .configure("microservice.services.customs-data-store.port" -> server.port)
        .build()

    val expected = None
    val app = application
    val connector = app.injector.instanceOf[CustomsDataStoreConnector]
  }
}

