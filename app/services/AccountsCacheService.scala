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

import connectors.CustomsFinancialsConnector
import models.InternalId
import models.domain.CDSAccounts
import repositories.AccountsRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.StringUtils.emptyString

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountsCacheService @Inject()(repository: AccountsRepository,
                                     connector: CustomsFinancialsConnector)(implicit ec: ExecutionContext) {

  def retrieveAccounts(internalId: InternalId,
                       eoriList: Seq[String])(implicit hc: HeaderCarrier): Future[CDSAccounts] = {
    repository.get(internalId.value).flatMap {
      case Some(value) => Future.successful(value)
      case None =>
        for {
          accounts <- Future.sequence(eoriList.map(eachEori => connector.retrieveAccounts(eachEori)))
          _ <- repository.set(internalId.value, merge(accounts))
        } yield merge(accounts)
    }
  }

  def merge(accounts: Seq[CDSAccounts]): CDSAccounts = {
    val mergedAccounts = accounts.flatMap(_.accounts).toList

    CDSAccounts(emptyString, mergedAccounts)
  }

  def retrieveAccountsForId(internalId: InternalId)(implicit hc: HeaderCarrier): Future[Option[CDSAccounts]] =
    {
      repository.get(internalId.value)
    }

}
