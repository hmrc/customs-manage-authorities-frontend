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

package services

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject
import play.api.test.Helpers._
import repositories.SessionRepository

import scala.concurrent.Future

class ConfirmationServiceSpec extends SpecBase{

  "successfully populate ConfirmationDetails to sessionCache" in {
    val mockSessionRepository = mock[SessionRepository]

    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

    val application = applicationBuilder().overrides(
      inject.bind[SessionRepository].toInstance(mockSessionRepository)
    ).build()

    val service = application.injector.instanceOf[ConfirmationService]
    running(application){
      val result = await(service.populateConfirmation("id", "eori"))
      result mustBe true
    }
  }

}
