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
import models.{EmailUnverifiedResponse, EmailVerifiedResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.OK
import play.api.inject
import play.api.test.Helpers.{GET, await, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class EmailControllerSpec extends SpecBase {

  "showUnverified" must {
    "return unverified email" in new Setup {
      when(mockConnector.isEmailUnverified(any)).thenReturn(Future.successful(Some("unverifiedEmail")))

      running(application) {
        val connector = application.injector.instanceOf[CustomsFinancialsConnector]

        val result: Future[Option[String]] = connector.isEmailUnverified(hc)
        await(result) mustBe expectedResult
      }
    }

    "return unverified email response" in new Setup {
       when(mockConnector.isEmailUnverified(any)).thenReturn(Future.successful(Some("test@test.com")))

      running(application) {
        val request = fakeRequest(GET, routes.EmailController.showUnverified().url)
        val result = route(application, request).value

        status(result) shouldBe OK
      }
    }
  }

  "showUndeliverable" must {
    "display undeliverableEmail page" in new Setup {
        when(mockConnector.verifiedEmail(any)).thenReturn(Future.successful(emailVerifiedResponse))

        running(application){
          val request = fakeRequest(GET, routes.EmailController.showUndeliverable().url)
          val result = route(application, request).value

          status(result) shouldBe OK
        }
    }
  }


  trait Setup {
    val expectedResult = Some("unverifiedEmail")
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockConnector: CustomsFinancialsConnector = mock[CustomsFinancialsConnector]

    val response = EmailUnverifiedResponse(Some("unverifiedEmail"))
    val emailVerifiedResponse: EmailVerifiedResponse = EmailVerifiedResponse(Some("test@test.com"))

    val application = applicationBuilder().overrides(
      inject.bind[CustomsFinancialsConnector].toInstance(mockConnector)
    ).build()
  }

}
