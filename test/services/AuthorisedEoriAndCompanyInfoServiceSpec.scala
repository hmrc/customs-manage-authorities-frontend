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

package services

import base.SpecBase
import connectors.CustomsDataStoreConnector
import models.InternalId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.{Application, inject}
import repositories.AuthorisedEoriAndCompanyInfoRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestData.COMPANY_NAME

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthorisedEoriAndCompanyInfoServiceSpec extends SpecBase {

  "retrieveAuthEorisAndCompanyInfoForId" must {

    "retrieve the data correctly" in new Setup {
      when(mockRepo.get(id.value)).thenReturn(Future.successful(Some(data)))

      service.retrieveAuthEorisAndCompanyInfoForId(id).map { result =>
        result mustEqual data
      }
    }
  }

  "retrieveAuthorisedEoriAndCompanyInfo" must {
    "retrieve the data correctly" when {
      "data is not present in cache" in new Setup {

        val eoris: Set[String]           = Set(eori1, eori2)
        val mapData: Map[String, String] = Map(eori1 -> COMPANY_NAME, eori2 -> COMPANY_NAME)

        implicit val hc: HeaderCarrier = HeaderCarrier()

        when(mockRepo.get(id.value)).thenReturn(Future.successful(None))
        when(mockRepo.set(id.value, mapData)).thenReturn(Future.successful(true))
        when(mockDataStoreConnector.getCompanyName(any)(any)).thenReturn(Future.successful(Some(COMPANY_NAME)))

        service.retrieveAuthorisedEoriAndCompanyInfo(id, eoris).map { result =>
          result mustEqual mapData
        }
      }

      "data is present in cache" in new Setup {
        val eoris: Set[String]           = Set(eori1, eori2)
        val mapData: Map[String, String] = Map(eori1 -> COMPANY_NAME, eori2 -> COMPANY_NAME)

        implicit val hc: HeaderCarrier = HeaderCarrier()

        when(mockRepo.get(id.value)).thenReturn(Future.successful(Some(mapData)))

        service.retrieveAuthorisedEoriAndCompanyInfo(id, eoris).map { result =>
          result mustEqual mapData
        }
      }
    }
  }

  "storeAuthEorisAndCompanyInfo" must {

    "successfully store the data" in new Setup {
      when(mockRepo.set(id.value, data)).thenReturn(Future.successful(true))

      service.storeAuthEorisAndCompanyInfo(id, data).map { result =>
        result mustEqual true
      }
    }
  }

  trait Setup {
    val id: InternalId            = InternalId("cache_id")
    val eori1                     = "test_eori_1"
    val eori2                     = "test_eori_2"
    val data: Map[String, String] = Map("eori_1" -> "company_1", "eori_2" -> "company_2")

    val mockRepo: AuthorisedEoriAndCompanyInfoRepository  = mock[AuthorisedEoriAndCompanyInfoRepository]
    val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]

    val application: Application = applicationBuilder()
      .overrides(
        inject.bind[AuthorisedEoriAndCompanyInfoRepository].toInstance(mockRepo),
        inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
      )
      .build()

    val service: AuthorisedEoriAndCompanyInfoService =
      application.injector.instanceOf[AuthorisedEoriAndCompanyInfoService]
  }
}
