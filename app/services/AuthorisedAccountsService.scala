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

package services

import models.AuthorisedAccounts
import models.domain.{CDSAccount, EORI}
import models.requests.DataRequest
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedAccountsService @Inject()(
                                    authoritiesCache: AuthoritiesCacheService,
                                    accountsService: AccountsCacheService)(implicit executionContext: ExecutionContext) {

  def getAuthorisedAccounts(enteredEori: EORI)(implicit request: DataRequest[_], hc: HeaderCarrier): Future[AuthorisedAccounts] = for {
    authorities <- authoritiesCache.retrieveAuthorities(request.internalId)
    accounts <- accountsService.retrieveAccounts(request.internalId, Seq(request.eoriNumber))
  } yield {
    val availableAccountNumbers = authorities.authorisedWithEori(enteredEori).map(_.accountNumber)
    AuthorisedAccounts(
      accounts.alreadyAuthorised(availableAccountNumbers),
      accounts.canAuthoriseAccounts(availableAccountNumbers),
      filterAccounts(enteredEori,accounts.closedAccounts),
      filterAccounts(enteredEori,accounts.pendingAccounts),
      enteredEori
    )
  }

  def filterAccounts(enteredEori: EORI, accounts: Seq[CDSAccount]): Seq[CDSAccount] =
    if(enteredEori.startsWith("GB")) {
      accounts.filter(!_.isNiAccount)
    } else { accounts.filter(_.isNiAccount)}

}
