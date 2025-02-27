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

package views.components

import base.SpecBase
import config.FrontendAppConfig
import forms.EoriNumberFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import views.html.components.inputCheckboxes

class InputCheckboxesSpec extends SpecBase {
  "InputCheckboxes" should {

    "render correctly with no errors" in new Setup {
      val view: Document = Jsoup.parse(
        app.injector
          .instanceOf[inputCheckboxes]
          .apply(
            form = validForm,
            legend = "value",
            items = items,
            hint = "Hint"
          )
          .body
      )

      view.getElementsByTag("label").html() mustBe messages(app)(
        "accounts.type.cash"
      )
      view.getElementById("value-hint").html() mustBe "Hint"
    }

    "display an error message when form validation fails" in new Setup {
      val view: Document = Jsoup.parse(
        app.injector
          .instanceOf[inputCheckboxes]
          .apply(
            form = invalidForm,
            legend = "value",
            items = items,
            hint = "Hint"
          )
          .body
      )

      view.getElementById("value-error").childNodes().size() must be > 0
      view.getElementsByClass("govuk-visually-hidden").html() mustBe "Error:"
    }

  }

  trait Setup {
    val app: Application       = applicationBuilder().build()
    implicit val msg: Messages = messages(app)
    private val appConfig      = app.injector.instanceOf[FrontendAppConfig]

    val validForm: Form[String]   = new EoriNumberFormProvider(appConfig)
      .apply()
      .bind(Map("value" -> "GB123456789012"))
    val invalidForm: Form[String] =
      new EoriNumberFormProvider(appConfig).apply().bind(Map("value" -> "3456789012"))
    val items                     = Seq(
      CheckboxItem(
        name = Some("value[0]"),
        id = Some("value"),
        value = "account_0",
        content = Text(messages(app)("accounts.type.cash")),
        checked = false
      )
    )

  }
}
