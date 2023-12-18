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

package controllers.actions

import base.SpecBase
import connectors.CustomsDataStoreConnector
import models.requests.IdentifierRequest
import models.{InternalId, UnverifiedEmail, UndeliverableEmail}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.Helpers.LOCATION
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Email}
import uk.gov.hmrc.http.ServiceUnavailableException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailActionSpec extends SpecBase with MockitoSugar with ScalaFutures {

  "Email Action" should {
    "allow requests with valida email" in new Setup {
      val action = new Harness()

      when(
        mockDataStoreConnector.getEmail("GB123456789012")(any())
      ) thenReturn Future.successful(Right(Email("some@email.com")))

      val futureResult = action.filter(identifierRequest())
      whenReady(futureResult) { result =>
        result mustBe None
      }
    }

    "allow requests, when getEmail throws service unavailable exception" in new Setup {
      val action = new Harness()

      when(
        mockDataStoreConnector.getEmail("GB123456789012")(any())
      ) thenReturn Future.failed(new ServiceUnavailableException(""))

      val futureResult = action.filter(identifierRequest())
      whenReady(futureResult) { result =>
        result mustBe None
      }
    }

    "Redirect users with unverified emails" in new Setup {
      val action = new Harness()

      when(
        mockDataStoreConnector.getEmail("GB123456789012")(any())
      ) thenReturn Future.successful(Left(UnverifiedEmail))

      val futureResult = action.filter(identifierRequest())
      whenReady(futureResult) { result =>
        result.get.header.status mustBe SEE_OTHER
        result.get.header.headers(LOCATION) must include("/verify-your-email")
      }
    }

    "redirect the requests to undeliverable email page when dataStoreService returns undeliverable email" in new Setup {
      val action = new Harness()
      val emailId = "test@test.com"

      when(
        mockDataStoreConnector.getEmail("GB123456789012")(any())
      ) thenReturn Future.successful(Left(UndeliverableEmail(emailId)))

      val futureResult = action.filter(identifierRequest())
      whenReady(futureResult) { result =>
        result.get.header.status mustBe SEE_OTHER
        result.get.header.headers(LOCATION) must include("/undeliverable-email")
      }
    }
  }

  trait Setup {
    val mockDataStoreConnector: CustomsDataStoreConnector =
    mock[CustomsDataStoreConnector]

    def identifierRequest() = IdentifierRequest(
      fakeRequest(),
      InternalId("id"),
      Credentials("", ""),
      Organisation,
      None,
      None,
      "GB123456789012"
    )

    class Harness extends EmailAction(mockDataStoreConnector)(global, any()) {
      def callFilter[A](request: IdentifierRequest[A]): Future[Option[Result]] =
        filter(request)
    }

  }
}
