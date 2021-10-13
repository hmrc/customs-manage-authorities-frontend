/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.{Json, OFormat}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait AuthorityStart




object AuthorityStart extends Enumerable.Implicits {
  val values: Seq[AuthorityStart] = Seq(
    Today,
    Setdate
  )

  case object Today extends WithName("today") with AuthorityStart
  case object Setdate extends WithName("setDate") with AuthorityStart

  def options(form: Form[_], conditionalHtml: Option[Html] = None)(implicit messages: Messages): Seq[RadioItem] = values.map {
    case value@Today => RadioItem(
      value = Some(value.toString),
      content = Text(messages(s"authorityStart.${value.toString}")),
      checked = form("value").value.contains(value.toString)
    )
    case value@Setdate => RadioItem(
      value = Some(value.toString),
      content = Text(messages(s"authorityStart.${value.toString}")),
      checked = form("value").value.contains(value.toString),
      conditionalHtml = conditionalHtml
    )

  }

  implicit val enumerable: Enumerable[AuthorityStart] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
