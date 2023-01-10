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

package viewmodels

import models.{CompanyDetails, UserAnswers}
import pages.add._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import services.DateTimeService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions

case class EoriDetailsCorrectHelper(userAnswers: UserAnswers, dateTimeService: DateTimeService)
                                   (implicit val messages: Messages) extends SummaryListRowHelper {

  val companyName: Option[String] = userAnswers.get(EoriNumberPage).map(x => x.name).get

  val companyNameWithConsent: String = companyName match {
    case Some(value)=> messages("authorisedUser.declaration.singular")
    case _ => messages("authorisedUser.declaration.plural")
  }


  def companyDetailsRows: Seq[SummaryListRow] = {
    if (companyName.isEmpty) {
      Seq(
        eoriNumberRow(userAnswers.get(EoriNumberPage))
      ).flatten
    }
    else {
        Seq(
          eoriNumberRow(userAnswers.get(EoriNumberPage)),
          companyNameRow(userAnswers.get(EoriNumberPage))

        ).flatten
    }
  }

  private def eoriNumberRow(companyDetails: Option[CompanyDetails]): Option[SummaryListRow] = {
    companyDetails.map(x =>
      summaryListRow(
        messages("eoriDetail.eoriNumber.label"),
        value = HtmlFormat.escape(x.eori).toString(),
        actions = Actions(items = Seq()),
        secondValue = None
      )
    )
  }

  private def companyNameRow(companyDetails: Option[CompanyDetails]): Option[SummaryListRow] = {
    companyDetails.map(x =>
      summaryListRow(
        messages("eoriDetail.companyName.label"),
        value = HtmlFormat.escape(x.name.get).toString(),
        actions = Actions(items = Seq()),
        secondValue = None
      )
    )
  }
}
