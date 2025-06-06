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
import config.FrontendAppConfig
import models.{EmailVerifiedResponse, UndeliverableEmail, UnverifiedEmail}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.api.{Application, inject}
import utils.WireMockHelper
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HeaderCarrier

class CustomsDataStoreConnectorSpec
    extends SpecBase
    with WireMockHelper
    with ScalaFutures
    with IntegrationPatience
    with EitherValues
    with OptionValues
    with MockitoSugar {

  implicit private lazy val hc: HeaderCarrier        = HeaderCarrier()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  "retrieveCompanyInformationThirdParty" must {
    "return Company Information Third Party when consent is defined as 1(True)" in new Setup {
      val response: String =
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

      val expectedResult: Option[String] = Some("Tony Stark")

      running(app) {
        server.stubFor(
          post(urlEqualTo("/customs-data-store/eori/company-information-third-party"))
            .willReturn(ok(response))
        )

        val result = connector.retrieveCompanyInformationThirdParty("GB123456789012").futureValue
        result mustBe expectedResult
      }
    }

    "return None when consent is defined as 0(False) for company info" in new Setup {
      val response: String =
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
          post(urlEqualTo("/customs-data-store/eori/company-information-third-party"))
            .willReturn(ok(response))
        )

        val result = connector.retrieveCompanyInformationThirdParty("GB123456789012").futureValue
        result mustBe expected
      }
    }

    "return None when consent is empty for company info" in new Setup {
      val response: String =
        """
          |{
          |   "name":"Tony Stark",
          |   "consent": null,
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
          post(urlEqualTo("/customs-data-store/eori/company-information-third-party"))
            .willReturn(ok(response))
        )

        val result = connector.retrieveCompanyInformationThirdParty("GB123456789012").futureValue
        result mustBe expected
      }
    }
  }

  ".getCompanyName" must {
    "return companyName when consent is defined as 1(True)" in new Setup {
      val response: String =
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

      val expectedResult: Option[String] = Some("Tony Stark")

      running(app) {
        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/company-information"))
            .willReturn(ok(response))
        )

        val result = connector.getCompanyName.futureValue
        result mustBe expectedResult
      }
    }

    "return None when consent is defined as 0(False) for company info" in new Setup {
      val response: String =
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
          get(urlEqualTo("/customs-data-store/eori/company-information"))
            .willReturn(ok(response))
        )

        val result = connector.getCompanyName.futureValue
        result mustBe expected
      }
    }

    "return None when consent is empty for company info" in new Setup {
      val response: String =
        """
          |{
          |   "name":"Tony Stark",
          |   "consent": null,
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
          get(urlEqualTo("/customs-data-store/eori/company-information"))
            .willReturn(ok(response))
        )

        val result = connector.getCompanyName.futureValue
        result mustBe expected
      }
    }
  }

  ".get XiEori" must {
    "return xi eori when it is returned from data store" in new Setup {
      val response: String =
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

      val expectedResult: Option[String] = Some("XI1234567")

      running(app) {

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/xieori-information"))
            .willReturn(ok(response))
        )

        val result = connector.getXiEori("GB123456789").futureValue
        result mustBe expectedResult
      }
    }

    "return None when empty XI EORI value is returned from data store" in new Setup {
      val response: String =
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
          get(urlEqualTo("/customs-data-store/eori/xieori-information"))
            .willReturn(ok(response))
        )
        val result = connector.getXiEori("GB123456789").futureValue
        result mustBe expected
      }
    }

    "return None when error response is returned" in new Setup {

      running(app) {

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/xieori-information"))
            .willReturn(serverError())
        )
        val result = connector.getXiEori("GB123456789").futureValue
        result mustBe expected
      }
    }

    "return None when feature flag is false" in {

      val mockAppConfig = mock[FrontendAppConfig]
      when(mockAppConfig.xiEoriEnabled).thenReturn(false)
      when(mockAppConfig.customsDataStore).thenReturn("some/string")

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

      val application: Application =
        new GuiceApplicationBuilder()
          .overrides(inject.bind[FrontendAppConfig].toInstance(mockAppConfig))
          .configure("microservice.services.customs-data-store.port" -> server.port)
          .build()

      val connector = application.injector.instanceOf[CustomsDataStoreConnector]

      running(application) {

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/xieori-information"))
            .willReturn(ok(response))
        )

        val result = connector.getXiEori("GB123456789").futureValue
        result mustBe None
      }
    }

    "return None when an EU Eori is passed" in new Setup {
      running(app) {
        val result = connector.getXiEori("FR123456789").futureValue
        result mustBe None
      }
    }

    "return an EORI when an GB Eori is passed" in new Setup {

      val response: String =
        """
          |{
          |   "xiEori":"GB123456789",
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
          get(urlEqualTo("/customs-data-store/eori/xieori-information"))
            .willReturn(ok(response))
        )

        val result = connector.getXiEori("GB123456789").futureValue
        result mustBe Some("GB123456789")
      }
    }
  }

  "getEmail" should {
    "return an email address when the request is successful and undeliverable" +
      " is not present in the response" in new Setup {

        val emailResponse: String =
          """{
            |"address": "some@email.com"
            |}""".stripMargin

        running(app) {

          server.stubFor(
            get(urlEqualTo("/customs-data-store/eori/verified-email"))
              .willReturn(ok(emailResponse))
          )
          val result = connector.getEmail.futureValue
          result mustBe Right(Email("some@email.com"))
        }
      }

    "return no email address when the request is successful and undeliverable is present in the response" in new Setup {
      val emailResponse: String =
        """{
            |"address": "some@email.com",
            |"timestamp": "2022-10-10T11:22:22Z",
            |"undeliverable": {
            |"subject": "someSubject"
            |}
            |}""".stripMargin

      running(app) {
        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/verified-email"))
            .willReturn(ok(emailResponse))
        )

        val result = connector.getEmail.futureValue
        result mustBe Left(UndeliverableEmail("some@email.com"))
      }
    }

    "return unverifiedEmail when the request is successful and" +
      " email address is not present in the response" in new Setup {
        val emailResponse = """{}"""

        running(app) {
          server.stubFor(
            get(urlEqualTo("/customs-data-store/eori/verified-email"))
              .willReturn(ok(emailResponse))
          )

          val result = connector.getEmail.futureValue
          result mustBe Left(UnverifiedEmail)
        }
      }

    "return no email when a NOT_FOUND response is returned" in new Setup {

      running(app) {

        server.stubFor(
          get(urlEqualTo("/customs-data-store/eori/verified-email"))
            .willReturn(notFound)
        )
        val result = connector.getEmail.futureValue
        result mustBe Left(UnverifiedEmail)
      }
    }
  }

  "unverifiedEmail" must {

    "return success response" in new Setup {

      running(app) {
        server.stubFor(
          get(urlEqualTo("/customs-data-store/subscriptions/unverified-email-display"))
            .willReturn(ok("""{"unVerifiedEmail": "unverified@email.com"}"""))
        )

        val result = connector.unverifiedEmail(hc).futureValue
        result mustBe Some("unverified@email.com")
      }
    }

    "return failure response of None" in new Setup {

      running(app) {
        server.stubFor(
          get(urlEqualTo("/customs-data-store/subscriptions/unverified-email-display"))
            .willReturn(ok("""{}"""))
        )

        val result = connector.unverifiedEmail(hc).futureValue
        result mustBe None
      }
    }
  }

  "verifiedEmail" must {

    "return correct email" in new Setup {

      running(app) {
        server.stubFor(
          get(urlEqualTo("/customs-data-store/subscriptions/email-display"))
            .willReturn(ok("""{"verifiedEmail": "test@test.com"}"""))
        )

        val result = connector.verifiedEmail(hc).futureValue
        result mustBe emailVerifiedRes
      }
    }
  }

  trait Setup {

    val emailValue: String                      = "test@test.com"
    val emailVerifiedRes: EmailVerifiedResponse = EmailVerifiedResponse(Some(emailValue))

    private def application: Application =
      new GuiceApplicationBuilder()
        .configure("microservice.services.customs-data-store.port" -> server.port)
        .build()

    val expected: Option[Nothing]            = None
    val app: Application                     = application
    val connector: CustomsDataStoreConnector = app.injector.instanceOf[CustomsDataStoreConnector]
  }
}
