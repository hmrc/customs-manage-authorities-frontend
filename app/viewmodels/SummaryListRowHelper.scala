/*
 * Copyright 2022 HM Revenue & Customs
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

package viewmodels

import models.domain.AccountWithAuthoritiesWithId
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, Value}

trait SummaryListRowHelper {

  def summaryListRow(
                      label: String,
                      value: String,
                      secondValue: Option[String],
                      actions: Actions): SummaryListRow =
    SummaryListRow(
      key = Key(content = Text(label)),
      value = Value(content = HtmlContent(value)),
      secondValue = secondValue.map { value => Value(content = HtmlContent(value)) },
      classes = "",
      actions = Some(actions)
    )

  protected def yesOrNo(value: Boolean)(implicit messages: Messages): String =
    if (value) messages("site.yes") else messages("site.no")

  def span(contents: String): HtmlContent = HtmlContent(
    Html(s"""$contents""")
  )

  def accountNumberRow(account: AccountWithAuthoritiesWithId)(implicit messages: Messages): Option[SummaryListRow] = {
    val accountType = messages(s"manageAuthorities.table.heading.account.${account.accountType}", account.accountNumber)
    Some(summaryListRow(
      messages("edit-cya-account-number"),
      value = HtmlFormat.escape(accountType).toString,
      actions = Actions(items = Seq.empty),
      secondValue = None
    ))
  }
}