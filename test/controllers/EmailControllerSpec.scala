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

package controllers

import base.SpecBase
import connectors.CustomsFinancialsConnector
import models.EmailUnverifiedResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.OK
import play.api.inject
import play.api.test.Helpers.{GET, await, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.Future

class EmailControllerSpec extends SpecBase {

  "EmailController" must {
    "return unverified email" in new Setup {
      running(application) {
        val connector = application.injector.instanceOf[CustomsFinancialsConnector]

        val result: Future[Option[String]] = connector.isEmailUnverified(hc)
        await(result) mustBe expectedResult
      }
    }

    "return unverified email response" in new Setup {
      running(application) {
        val request = fakeRequest(GET, routes.EmailController.showUnverified().url)
        val result = route(application, request).value
        status(result) shouldBe OK
      }
    }
  }

  trait Setup {
    val expectedResult = Some("unverifiedEmail")
    implicit val hc: HeaderCarrier = HeaderCarrier()
    private val mockHttpClient = mock[HttpClient]

    val response = EmailUnverifiedResponse(Some("unverifiedEmail"))

    when[Future[EmailUnverifiedResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
      .thenReturn(Future.successful(response))

    val application = applicationBuilder().overrides(
      inject.bind[HttpClient].toInstance(mockHttpClient)
    ).build()
  }

}
