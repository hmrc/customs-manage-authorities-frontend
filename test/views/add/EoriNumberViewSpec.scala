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

package views.add

import base.SpecBase
import config.FrontendAppConfig
import forms.EoriNumberFormProvider
import models.{CheckMode, Mode, NormalMode}
import navigation.Navigator
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.BeforeAndAfterAll
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import views.html.add.EoriNumberView

class EoriNumberViewSpec extends SpecBase with BeforeAndAfterAll {
  private lazy val app: Application = applicationBuilder().build()
  private lazy val appConfig        = app.injector.instanceOf[FrontendAppConfig]
  private val form                  = new EoriNumberFormProvider()()

  override def afterAll(): Unit = {
    app.stop().futureValue
    super.afterAll()
  }

  private def render(mode: Mode, language: String, input: Form[String] = form): Document = {
    implicit val msgs: Messages = MessagesImpl(Lang(language), app.injector.instanceOf[MessagesApi])
    val back                    = app.injector.instanceOf[Navigator].backLinkRouteForEORINUmberPage(mode)
    Jsoup.parse(app.injector.instanceOf[EoriNumberView].apply(input, mode, back)(fakeRequest(), msgs, appConfig).body)
  }

  "Add authority page" should {
    for {
      language <- Seq("en", "cy")
      mode     <- Seq(NormalMode, CheckMode)
    } {
      s"display the agreed guidance and accessible input in $language and $mode" in {
        val msgs: Messages = MessagesImpl(Lang(language), app.injector.instanceOf[MessagesApi])
        val doc            = render(mode, language, form.fill("GB345834921000"))
        doc.title                                     must include(msgs("eoriNumber.title"))
        doc.select("main h1").size mustBe 1
        doc.select("main h1").text mustBe msgs("eoriNumber.heading")
        doc.select("#add-authority-description").text mustBe msgs("eoriNumber.introduction")
        doc.select(".govuk-inset-text").text mustBe msgs("eoriNumber.details.text.eu")
        doc.select("label[for=value]").text mustBe msgs("eoriNumber.label")
        doc.select("#value-hint-text").text mustBe msgs("eoriNumber.hint.eu")
        doc.select("#value").attr("aria-describedby") must include("value-hint")
        doc.select("#value").attr("value") mustBe "GB345834921000"
        doc.select("form").attr("action") mustBe controllers.add.routes.EoriNumberController.onSubmit(mode).url
        doc.select("form").attr("method").toLowerCase mustBe "post"
        doc.select("input[name=csrfToken]").size mustBe 1
        doc.select("form button").text mustBe msgs("site.saveAndContinue")
        doc.select(".govuk-back-link").attr("href") mustBe
          app.injector.instanceOf[Navigator].backLinkRouteForEORINUmberPage(mode).url
      }

      s"associate validation errors with the EORI input in $language and $mode" in {
        val msgs: Messages = MessagesImpl(Lang(language), app.injector.instanceOf[MessagesApi])
        val doc            = render(mode, language, form.withError("value", "eoriNumber.error.required"))
        doc.select(".govuk-error-summary a").attr("href") mustBe "#value"
        doc.select("#value-error").text               must include(msgs("eoriNumber.error.required"))
        doc.select("#value").attr("aria-describedby") must include("value-error")
        doc.title                                     must startWith(msgs("error.browser.title.prefix"))
      }
    }
  }
}
