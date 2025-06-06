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
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.ArgumentCaptor
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import play.api.test.Helpers.*
import play.api.{Application, inject}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.play.partials.HtmlPartial
import utils.StringUtils.emptyString

import java.net.URL
import scala.concurrent.Future

class SecureMessageConnectorSpec extends SpecBase {

  "getMessageCountBanner" should {

    "return a valid message banner when the upstream call returns OK" in new Setup {
      val url = new URL(s"${mockFrontendAppConfig.customsSecureMessagingBannerEndpoint}?return_to=$returnTo")

      when(mockRequestBuilder.execute[HtmlPartial](any, any))
        .thenReturn(Future.successful(HtmlPartial.Success(Some("Hello"), play.twirl.api.Html(emptyString))))

      when(mockHttpClientV2.get(any[URL])(any)).thenReturn(mockRequestBuilder)

      running(app) {
        val result = await(connector.getMessageCountBanner(returnTo)(fakeRequest()))
        result.get mustEqual HtmlPartial.Success(Some("Hello"), play.twirl.api.Html(emptyString))
      }

      val urlCaptor = ArgumentCaptor.forClass(classOf[URL])
      verify(mockHttpClientV2).get(urlCaptor.capture())(any)
      assert(urlCaptor.getValue.toString == url.toString)

      verify(mockRequestBuilder).execute[HtmlPartial](any, any)
    }

    "return None when the upstream call returns HtmlPartial.Failure" in new Setup {
      val url = new URL(s"${mockFrontendAppConfig.customsSecureMessagingBannerEndpoint}?return_to=$returnTo")

      when(mockRequestBuilder.execute[HtmlPartial](any, any))
        .thenReturn(Future.successful(HtmlPartial.Failure()))

      when(mockHttpClientV2.get(any[URL])(any)).thenReturn(mockRequestBuilder)

      running(app) {
        val result = await(connector.getMessageCountBanner(returnTo)(fakeRequest()))
        result mustBe empty
      }

      val urlCaptor = ArgumentCaptor.forClass(classOf[URL])
      verify(mockHttpClientV2).get(urlCaptor.capture())(any)
      assert(urlCaptor.getValue.toString == url.toString)

      verify(mockRequestBuilder).execute[HtmlPartial](any, any)
    }

    "return None when the upstream call throws an Exception" in new Setup {
      val url = new URL(s"${mockFrontendAppConfig.customsSecureMessagingBannerEndpoint}?return_to=$returnTo")

      when(mockRequestBuilder.execute[HtmlPartial](any, any))
        .thenReturn(Future.failed(new RuntimeException("exception occurred")))
      
      

      when(mockHttpClientV2.get(any[URL])(any)).thenReturn(mockRequestBuilder)

      running(app) {
        val result = await(connector.getMessageCountBanner(returnTo)(fakeRequest()))
        result mustBe empty
      }

      val urlCaptor = ArgumentCaptor.forClass(classOf[URL])
      verify(mockHttpClientV2).get(urlCaptor.capture())(any)
      assert(urlCaptor.getValue.toString == url.toString)

      verify(mockRequestBuilder).execute[HtmlPartial](any, any)
    }

  }

  trait Setup {
    val hc: HeaderCarrier                        = HeaderCarrier()
    implicit val messages: Messages              = stubMessages()
    val mockHttpClientV2: HttpClientV2           = mock[HttpClientV2]
    val mockRequestBuilder: RequestBuilder       = mock[RequestBuilder]
    val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

    val returnTo = "gov.uk"

    when(mockFrontendAppConfig.customsSecureMessagingBannerEndpoint).thenReturn("http://localhost:12345/banner")

    val app: Application = applicationBuilder()
      .overrides(
        inject.bind[HttpClientV2].toInstance(mockHttpClientV2),
        inject.bind[FrontendAppConfig].toInstance(mockFrontendAppConfig)
      )
      .build()

    val connector: SecureMessageConnector = app.injector.instanceOf[SecureMessageConnector]
  }
}
