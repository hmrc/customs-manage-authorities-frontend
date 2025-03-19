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

package utils

import base.SpecBase
import connectors.CustomsDataStoreConnector
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.{AnyContent, Request, AnyContentAsEmpty}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import models.InternalId
//import org.scalatest.concurrent.ScalaFutures
//import org.scalatestplus.mockito.MockitoSugar
//import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.retrieve.Credentials
import utils.StringUtils.emptyString

//import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext
//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UtilsSpec extends SpecBase {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockDataStoreConnector: CustomsDataStoreConnector = mock(classOf[CustomsDataStoreConnector])

  "getXiEori" should {

    "call dataStoreConnector when a valid prefix is used" in {
      val fakeRequest: Request[AnyContent] = FakeRequest().withBody(AnyContentAsEmpty)

      val request = IdentifierRequest(
        fakeRequest,
        InternalId("id"),
        Credentials(emptyString, emptyString),
        Organisation,
        Some("email"),
        "GB123456789"
      )

      when(mockDataStoreConnector.getXiEori(any())).thenReturn(Future.successful(Some("XI123456789")))

      val result = Utils.getXiEori(mockDataStoreConnector)(request, ec, hc)

      whenReady(result) { res =>
        res mustBe Some("XI123456789")
        verify(mockDataStoreConnector, times(1)).getXiEori(any())
      }
    }

    "return None when an invalid prefix is passed" in {
      val fakeRequest: Request[AnyContent] = FakeRequest().withBody(AnyContentAsEmpty)

      val request = IdentifierRequest(
        fakeRequest,
        InternalId("id"),
        Credentials(emptyString, emptyString),
        Organisation,
        Some("email"),
        "FR123456789"
      )

      val result = Utils.getXiEori(mockDataStoreConnector)(request, ec, hc)

      whenReady(result) { res =>
        res mustBe None
      }
    }
  }
}
