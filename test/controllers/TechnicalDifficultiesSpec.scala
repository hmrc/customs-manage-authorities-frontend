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

package controllers

import base.SpecBase
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.{
  GET, contentAsString, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty
}

class TechnicalDifficultiesSpec extends SpecBase {

  "onPageLoad" should {
    "return OK" in new Setup {

      running(application()) {
        val request = FakeRequest(GET, routes.TechnicalDifficulties.onPageLoad.url)
        val result  = route(application(), request).value

        status(result) mustBe OK
        val contentAsStringResult = contentAsString(result)

        contentAsStringResult contains "service-technical-difficulties.title"
        contentAsStringResult contains "service.technical-difficulties.heading"
        contentAsStringResult contains "service.technical-difficulties.p"
      }
    }
  }

  trait Setup {
    implicit val msgs: Messages = messages
  }
}
