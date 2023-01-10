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

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountsCacheService @Inject()(repository: AccountsRepository, connector: CustomsFinancialsConnector)(implicit ec: ExecutionContext) {

  def retrieveAccounts(internalId: InternalId, eori: String)(implicit hc: HeaderCarrier): Future[CDSAccounts] = {
    repository.get(internalId.value).flatMap {
      case Some(value) => Future.successful(value)
      case None =>
        for {
          accounts <- connector.retrieveAccounts(eori)
          _ <- repository.set(internalId.value, accounts)
        } yield accounts
    }
  }

}
