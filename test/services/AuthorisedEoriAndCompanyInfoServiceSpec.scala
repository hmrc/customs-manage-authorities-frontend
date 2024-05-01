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
import models.InternalId
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.{Application, inject}
import repositories.AuthorisedEoriAndCompanyInfoRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthorisedEoriAndCompanyInfoServiceSpec extends SpecBase {

  "retrieveAuthEorisAndCompanyInfo" must {

    "retrieve the data correctly" in new Setup {
      when(mockRepo.get(id.value)).thenReturn(Future.successful(Some(data)))

      service.retrieveAuthEorisAndCompanyInfo(id).map {
        result => result mustEqual data
      }
    }
  }

  "storeAuthEorisAndCompanyInfo" must {

    "successfully store the data" in new Setup {
      when(mockRepo.set(id.value, data)).thenReturn(Future.successful(true))

      service.storeAuthEorisAndCompanyInfo(id, data).map {
        result => result mustEqual true
      }
    }
  }

  trait Setup {
    val id: InternalId = InternalId("cache_id")
    val data: Map[String, String] = Map("eori_1" -> "company_1", "eori_2" -> "company_2")

    val mockRepo: AuthorisedEoriAndCompanyInfoRepository = mock[AuthorisedEoriAndCompanyInfoRepository]

    val application: Application = applicationBuilder().overrides(
      inject.bind[AuthorisedEoriAndCompanyInfoRepository].toInstance(mockRepo)
    ).build()

    val service: AuthorisedEoriAndCompanyInfoService =
      application.injector.instanceOf[AuthorisedEoriAndCompanyInfoService]
  }
}
