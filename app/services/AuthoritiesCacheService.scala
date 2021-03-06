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

package services

import connectors.CustomsFinancialsConnector
import models.InternalId
import models.domain.{AccountWithAuthoritiesWithId, AuthoritiesWithId, StandingAuthority}
import repositories.AuthoritiesRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthoritiesCacheService @Inject()(repository: AuthoritiesRepository, connector: CustomsFinancialsConnector)(implicit ec: ExecutionContext) {

  def retrieveAuthorities(internalId: InternalId)(implicit hc: HeaderCarrier): Future[AuthoritiesWithId] = {
    repository.get(internalId.value).flatMap {
      case Some(value) => Future.successful(value)
      case None =>
        for {
          authorities <- connector.retrieveAccountAuthorities()
          authoritiesWithId = AuthoritiesWithId(authorities)
          _ <- repository.set(internalId.value, authoritiesWithId)
        } yield authoritiesWithId
    }
  }

  def getAccountAndAuthority(internalId: InternalId, authorityId: String, accountId: String)
                            (implicit hc: HeaderCarrier): Future[Either[AuthoritiesCacheErrorResponse, AccountAndAuthority]] = {
    retrieveAuthorities(internalId).map { accountsWithAuthorities =>
      accountsWithAuthorities.authorities.get(accountId).map { account =>
        account.authorities.get(authorityId).map { authority =>
          Right(AccountAndAuthority(account, authority))
        }.getOrElse(Left(NoAuthority))
      }.getOrElse(Left(NoAccount))
    }
  }
}

case class AccountAndAuthority(account: AccountWithAuthoritiesWithId, authority: StandingAuthority)

sealed trait AuthoritiesCacheErrorResponse

case object NoAuthority extends AuthoritiesCacheErrorResponse

case object NoAccount extends AuthoritiesCacheErrorResponse
