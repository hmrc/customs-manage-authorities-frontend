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

import play.api.test.Helpers._
import play.twirl.api.Html
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import base.SpecBase
import uk.gov.hmrc.play.partials.HtmlPartial
import play.api.{inject, Application}
import scala.concurrent.Future
import scala.util.Try
import utils.StringUtils.emptyString
import play.api.mvc.RequestHeader

class SecureMessageConnectorSpec extends SpecBase {

  "getMessageCountBanner" should {

    "return a valid message banner when the upstream call returns OK" in new Setup {
      when(mockConnector.getMessageCountBanner(any[String])(any[RequestHeader]))
        .thenReturn(Future.successful(Some(HtmlPartial.Success(Some("Hello"), Html(emptyString)))))

      running(app) {
        val result = await(mockConnector.getMessageCountBanner(returnTo)(fakeRequest()))
        result.get mustEqual HtmlPartial.Success(Some("Hello"), Html(emptyString))
      }
    }

    "return None when the upstream call throws an Exception" in new Setup {
      when(mockConnector.getMessageCountBanner(any[String])(any[RequestHeader]))
        .thenReturn(Future.failed(new Exception("ahh")))

      running(app) {
        val result = Try(await(mockConnector.getMessageCountBanner(returnTo)(fakeRequest()))).toOption
        result.isEmpty mustEqual true
      }
    }

    "return None when the upstream call returns an unhappy response" in new Setup {
      when(mockConnector.getMessageCountBanner(any[String])(any[RequestHeader]))
        .thenReturn(Future.successful(None))

      running(app) {
        val result = await(mockConnector.getMessageCountBanner(returnTo)(fakeRequest()))
        result mustBe None
      }
    }
  }

  trait Setup {
    val mockConnector: SecureMessageConnector = mock[SecureMessageConnector]

    protected val returnTo = "gov.uk"

    val app: Application = applicationBuilder().overrides(
      inject.bind[SecureMessageConnector].toInstance(mockConnector)
    ).build()
    
  }
}
