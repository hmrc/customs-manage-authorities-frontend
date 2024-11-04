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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.Helpers._
import play.api.{Application, inject}
import play.twirl.api.Html
import uk.gov.hmrc.http.{HttpClient, HttpResponse}
import uk.gov.hmrc.play.partials.HtmlPartial
import utils.StringUtils.emptyString

import scala.concurrent.Future

class SecureMessageConnectorSpec extends SpecBase {

  "getMessageCountBanner" should {

    "return a valid message banner when the upstream call returns OK" in new Setup {

      when[Future[HtmlPartial]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(HtmlPartial.Success(Some("Hello"), Html(emptyString))))

      running(app) {
        val result = await(connector.getMessageCountBanner(returnTo)(fakeRequest()))
        result.get mustEqual HtmlPartial.Success(Some("Hello"), Html(emptyString))
      }
    }

    "return None when the upstream call return HtmlPartial failure" in new Setup {
      when[Future[HtmlPartial]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(HtmlPartial.Failure()))

      running(app) {
        val result: Option[HtmlPartial] = await(connector.getMessageCountBanner(returnTo)(fakeRequest()))
        result mustBe empty
      }
    }

    "return None when the upstream call throws an Exception" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.failed(new RuntimeException("exception occurred")))

      running(app) {
        val result: Option[HtmlPartial] = await(connector.getMessageCountBanner(returnTo)(fakeRequest()))
        result mustBe empty
      }
    }

  }

  trait Setup {

    val mockHttpClient: HttpClient = mock[HttpClient]

    protected val returnTo = "gov.uk"

    val app: Application = applicationBuilder().overrides(
      inject.bind[HttpClient].toInstance(mockHttpClient)
    ).build()

    val connector: SecureMessageConnector = app.injector.instanceOf[SecureMessageConnector]
  }
}
