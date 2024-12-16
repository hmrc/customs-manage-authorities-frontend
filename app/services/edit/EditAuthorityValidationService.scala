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

package services.edit

import com.google.inject.Inject
import models.{ErrorResponse, UnknownAccountType, UserAnswers}
import models.domain.{
  AccountWithAuthoritiesWithId, CdsCashAccount, CdsDutyDefermentAccount, CdsGeneralGuaranteeAccount, UnknownAccount
}
import models.requests.{Accounts, AddAuthorityRequest}
import pages.edit.EditAuthorisedUserPage

class EditAuthorityValidationService @Inject() (editCyaValidationService: EditCheckYourAnswersValidationService) {

  def validate(
    userAnswers: UserAnswers,
    accountId: String,
    authorityId: String,
    authorisedEori: String,
    account: AccountWithAuthoritiesWithId
  ): Either[ErrorResponse, AddAuthorityRequest] = {

    val maybeAccounts = for {
      standingAuthority <- editCyaValidationService.validate(userAnswers, accountId, authorityId, authorisedEori)
      authorisedUser    <- userAnswers.get(EditAuthorisedUserPage(accountId, authorityId))

      accounts = checkAndRetrieveAccounts(account)
    } yield accounts match {
      case Right(value) => Right(AddAuthorityRequest(value, standingAuthority, authorisedUser, editRequest = true))
      case Left(error)  => Left(error)
    }

    maybeAccounts match {
      case Some(authorities) => authorities
      case None              => Left(UnknownAccountType)
    }
  }

  private def checkAndRetrieveAccounts(
    account: AccountWithAuthoritiesWithId
  ): Either[UnknownAccountType.type, Accounts] =
    account.accountType match {
      case CdsCashAccount             => Right(Accounts(Some(account.accountNumber), Seq.empty, None))
      case CdsDutyDefermentAccount    => Right(Accounts(None, Seq(account.accountNumber), None))
      case CdsGeneralGuaranteeAccount => Right(Accounts(None, Seq.empty, Some(account.accountNumber)))
      case UnknownAccount             => Left(UnknownAccountType)
    }

}
