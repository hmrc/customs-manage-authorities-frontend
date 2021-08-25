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

package models.domain

import models.{StartDateError, UserAnswers}
import pages.edit.{EditAuthorityEndDatePage, EditAuthorityStartDatePage, EditAuthorityStartPage}
import play.api.libs.json.{Format, Json, Reads, Writes}

import java.time.LocalDate

case class StandingAuthority(authorisedEori: EORI,
                             authorisedFromDate: LocalDate,
                             authorisedToDate: Option[LocalDate],
                             viewBalance: Boolean) {

  def containsEori(eori: EORI): Boolean = authorisedEori == eori

  def canEditStartDate(now: LocalDate): Boolean = authorisedFromDate.isAfter(now)

  def startChanged(userAnswers: UserAnswers, accountId: String, authorityId: String, now: LocalDate): Either[StartDateError.type, Boolean] = {
    userAnswers.get(EditAuthorityStartDatePage(accountId, authorityId)) match {
      case Some(userStartDate) if userStartDate == authorisedFromDate => Right(false)
      case Some(_) => Right(true)
      case None => userAnswers.get(EditAuthorityStartPage(accountId, authorityId)) match {
        case Some(_) if authorisedFromDate == now => Right(false)
        case Some(_) => Right(true)
        case None => Left(StartDateError)
      }
    }
  }

  def endChanged(userAnswers: UserAnswers, accountId: String, authorityId: String, now: LocalDate): Boolean = {
    (userAnswers.get(EditAuthorityEndDatePage(accountId, authorityId)), authorisedToDate) match {
      case (Some(userEndDate), Some(existingEndDate)) if userEndDate == existingEndDate => false
      case (Some(_), _) => true
      case (None, Some(_)) => true
      case _ => false
    }
  }
}

object StandingAuthority {
  implicit val standingAuthorityReads: Reads[StandingAuthority] = Json.reads[StandingAuthority]
  implicit val standingAuthorityWrites: Writes[StandingAuthority] = Json.writes[StandingAuthority]
  implicit val standingAuthorityFormat: Format[StandingAuthority] = Format(standingAuthorityReads, standingAuthorityWrites)
}
