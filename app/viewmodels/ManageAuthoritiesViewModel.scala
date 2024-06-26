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

import models.domain.{AccountWithAuthoritiesWithId, AuthoritiesWithId, CDSAccounts}
import utils.DateUtils
import viewmodels.ManageAuthoritiesViewModel.accountWithAuthoritiesOrdering

import java.time.LocalDate
import java.time.chrono.ChronoLocalDate
import scala.collection.immutable.ListMap

case class ManageAuthoritiesViewModel(authorities: AuthoritiesWithId,
                                      accounts: CDSAccounts,
                                      auhorisedEoriAndCompanyMap: Map[String, String] = Map.empty) {

  def hasAccounts: Boolean = authorities.accounts.nonEmpty

  def hasNoAccounts: Boolean = authorities.accounts.isEmpty

  def sortedAccounts: ListMap[String, AccountWithAuthoritiesWithId] =
    ListMap(authorities.authorities.toSeq.sortBy(_._2): _*)

  def niIndicator(acc: String): Boolean =
    accounts.accounts.filter(_.number == acc).map(_.isNiAccount).headOption.getOrElse(false)
}

object ManageAuthoritiesViewModel extends DateUtils {

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.by(identity[ChronoLocalDate])
  implicit val accountWithAuthoritiesOrdering: Ordering[AccountWithAuthoritiesWithId] = Ordering.by(_.toString)
}
