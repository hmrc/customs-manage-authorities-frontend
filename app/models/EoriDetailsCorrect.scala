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

package models

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait EoriDetailsCorrect

object EoriDetailsCorrect extends Enumerable.Implicits {
  val values: Seq[EoriDetailsCorrect] = Seq(
    Yes,
    No
  )

  case object Yes extends WithName("radioYes") with EoriDetailsCorrect

  case object No extends WithName("radioNo") with EoriDetailsCorrect

  def options(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = values.map {
    case value@Yes => RadioItem(
      value = Some(value.toString),
      content = Text(messages(s"eoriDetails.${value.toString}")),
      checked = form("value").value.contains(value.toString)
    )

    case value@No => RadioItem(
      value = Some(value.toString),
      content = Text(messages(s"eoriDetails.${value.toString}")),
      checked = form("value").value.contains(value.toString)
    )
  }

  implicit val enumerable: Enumerable[EoriDetailsCorrect] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
