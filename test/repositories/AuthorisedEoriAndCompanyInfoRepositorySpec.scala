/*
 * Copyright 2025 HM Revenue & Customs
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

package repositories

import base.SpecBase
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{await, running}
import play.api.test.Helpers.defaultAwaitTimeout

class AuthorisedEoriAndCompanyInfoRepositorySpec extends SpecBase {

  "get" should {

    "return None if no data exists for the given id" in new Setup {
      running(app) {
        val result = await(repository.get(testId))
        result mustBe None
      }
    }

    "return the stored data for a given id" in new Setup {
      running(app) {
        await(repository.set(testId, testData))
        val result = await(repository.get(testId))
        result mustBe Some(testData)
      }
    }
  }

  "set" should {

    "insert or update data for a given id" in new Setup {
      running(app) {
        await(repository.set(testId, Map("initial" -> "data")))
        val updated = await(repository.set(testId, testData))
        updated mustBe true

        val result = await(repository.get(testId))
        result mustBe Some(testData)
      }
    }
  }

  "clear" should {

    "remove the data associated with the given id" in new Setup {
      running(app) {
        await(repository.set(testId, testData))
        val beforeClear = await(repository.get(testId))
        beforeClear mustBe Some(testData)

        await(repository.clear(testId)) mustBe true

        val afterClear = await(repository.get(testId))
        afterClear mustBe None
      }
    }
  }

  trait Setup {

    val app: Application                                   = new GuiceApplicationBuilder().build()
    val repository: AuthorisedEoriAndCompanyInfoRepository =
      app.injector.instanceOf[AuthorisedEoriAndCompanyInfoRepository]
    val testId: String                                     = "someSessionId"
    val testData: Map[String, String]                      = Map("eori" -> "GB123456789000", "companyName" -> "Test Ltd")
  }

  def afterEach(): Unit = {
    val app: Application                                   = new GuiceApplicationBuilder().build()
    val repository: AuthorisedEoriAndCompanyInfoRepository =
      app.injector.instanceOf[AuthorisedEoriAndCompanyInfoRepository]
    running(app) {
      await(repository.clear("someSessionId"))
    }
  }
}
