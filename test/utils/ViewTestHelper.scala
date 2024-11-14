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
import config.FrontendAppConfig
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

trait ViewTestHelper extends SpecBase {

  implicit lazy val app: Application = applicationBuilder().build()
  implicit val messages: Messages = messages(app)
  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest()

  def titleShouldBeCorrect(view: Document,
                           titleMsgKey: String): Assertion =
    view.title() mustBe s"${messages(titleMsgKey)} - ${messages("service.name")} - GOV.UK"

  def shouldContainBackLinkUrl(view: Document,
                               url: String): Assertion =
    view.html().contains(url) mustBe true
}
