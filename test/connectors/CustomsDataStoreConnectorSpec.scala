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
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{EitherValues, MustMatchers, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
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

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.customs-data-store.port" -> server.port)
      .build()

  ".getCompanyName" must {
    "return companyName when consent is defined as 1(True)" in {
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

      val expected = Some("Tony Stark")

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsDataStoreConnector]

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/company-information"))
            .willReturn(ok(response))
        )
        val result = connector.getCompanyName("GB123456789012").futureValue
        result mustBe expected
      }
    }


    "return None when consent is defined as 0(False)" in {
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

      val expected = None

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsDataStoreConnector]

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/company-information"))
            .willReturn(ok(response))
        )
        val result = connector.getCompanyName("GB123456789012").futureValue
        result mustBe expected
      }
    }

    "return None when consent is empty" in {
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

      val expected = None

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsDataStoreConnector]

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
    "return xi eori when it is returned from data store" in {
      val response =
        """
          |{
          |   "xiEori":"XI1234567",
          |   "consent":"1",
          |   "address":{
          |      "streetNumber1":"86 street",
          |      "city":"London",
          |      "countryCode":"GB"
          |   }
          |}
          |""".stripMargin

      val expected = Some("XI1234567")

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsDataStoreConnector]

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/xieori-information"))
            .willReturn(ok(response))
        )
        val result = connector.getXiEori("GB123456789012").futureValue
        result mustBe expected
      }
    }

    "return None when empty XI EORI value is returned from data store" in {
      val response =
        """
          |{
          |   "xiEori":"",
          |   "consent":"1",
          |   "address":{
          |      "streetNumber1":"86 street",
          |      "city":"London",
          |      "countryCode":"GB"
          |   }
          |}
          |""".stripMargin

      val expected = None

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsDataStoreConnector]

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/xieori-information"))
            .willReturn(ok(response))
        )
        val result = connector.getXiEori("GB123456789012").futureValue
        result mustBe expected
      }
    }

    "return None when error response is returned" in {

      val expected = None

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[CustomsDataStoreConnector]

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/GB123456789012/xieori-information"))
            .willReturn(serverError())
        )
        val result = connector.getXiEori("GB123456789012").futureValue
        result mustBe expected
      }
    }
  }
}
