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

package views

import play.api.data.Form
import play.api.i18n.Messages
import utils.StringUtils.emptyString

object ViewUtils {

  def title(form: Form[_],
            titleStr: String,
            section: Option[String],
            titleMessageArgs: Seq[String])
           (implicit messages: Messages): String = {
    titleNoForm(s"${errorPrefix(form)} ${messages(titleStr, titleMessageArgs: _*)}", section, Seq())
  }

  def titleNoForm(title: String,
                  section: Option[String],
                  titleMessageArgs: Seq[String])(implicit messages: Messages): String =
    s"${
      messages(title, titleMessageArgs: _*)
    } - ${
      section.fold(emptyString)(messages(_) + " - ")
    }${
      messages("service.name")
    } - ${
      messages("site.govuk")
    }"

  private def errorPrefix(form: Form[_])(implicit messages: Messages): String = {
    if (form.hasErrors || form.hasGlobalErrors) messages("error.browser.title.prefix") else emptyString
  }

  case class DetailsHint(summaryText: String,
                         text: String,
                         classes: String = emptyString,
                         attributes: Map[String, String] = Map.empty,
                         open: Boolean = false)

  case class LabelHint(labelText: String, classes: String = emptyString)

  case class InputTextHint(detailsHint: Option[DetailsHint] = None,
                           labelHint: Option[LabelHint] = None)
}
