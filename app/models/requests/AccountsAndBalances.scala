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

package models.requests


import models.domain
import models.domain.{AccountStatusOpen, CDSAccount, CDSAccountStatus, GeneralGuaranteeBalance}
import play.api.libs.json.{Json, OFormat, Reads}

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import scala.util.Random

case class AccountsRequestDetail(EORINo: String, accountType: Option[String], accountNumber: Option[String], referenceDate: Option[String])

case class AccountsAndBalancesRequest(requestCommon: AccountsRequestCommon, requestDetail: AccountsRequestDetail)

case class AccountsAndBalancesRequestContainer(accountsAndBalancesRequest: AccountsAndBalancesRequest)

object AccountsAndBalancesRequestContainer {

  implicit val accountsRequestCommonFormat: OFormat[AccountsRequestCommon] = Json.format[AccountsRequestCommon]
  implicit val accountsRequestDetailFormat: OFormat[AccountsRequestDetail] = Json.format[AccountsRequestDetail]
  implicit val accountsAndBalancesRequestFormat: OFormat[AccountsAndBalancesRequest] = Json.format[AccountsAndBalancesRequest]
  implicit val accountsAndBalancesRequestContainerFormat: OFormat[AccountsAndBalancesRequestContainer] = Json.format[AccountsAndBalancesRequestContainer]

}

case class AccountsRequestCommon(PID: Option[String], originatingSystem: Option[String], receiptDate: String, acknowledgementReference: String, regime: String)

object AccountsRequestCommon {
  private val MDG_ACK_REF_LENGTH = 32

  def generate: AccountsRequestCommon = {
    val (pid, originatingSystem) = (None, None)

    val isoLocalDateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS))
    val acknowledgmentRef = generateStringOfRandomDigits(MDG_ACK_REF_LENGTH)
    val regime = "CDS"

    AccountsRequestCommon(pid, originatingSystem, isoLocalDateTime, acknowledgmentRef, regime)
  }

  private def generateStringOfRandomDigits(length: Int) = {
    (1 to length).map(_ => Random.nextInt(10)).mkString // scalastyle:ignore magic.number
  }
}

case class AccountWithStatus(number: String,
                             `type`: String,
                             owner: String,
                             accountStatus: CDSAccountStatus = AccountStatusOpen,
                             viewBalanceIsGranted: Boolean = false
                            )

case class Limits(periodGuaranteeLimit: String, periodAccountLimit: String)

case class DefermentBalances(periodAvailableGuaranteeBalance: String, periodAvailableAccountBalance: String)

case class ReturnParameters(paramName: String, paramValue: String)

case class DutyDefermentAccount(account: AccountWithStatus, isNiAccount: Option[Boolean] = Some(false), isIomAccount: Option[Boolean] = Some(false),
                                limits: Option[Limits], balances: Option[DefermentBalances]) {
  def toDomain(): domain.DutyDefermentAccount = {
    val balance = domain.DutyDefermentBalance(
      limits.map(limit => BigDecimal(limit.periodGuaranteeLimit)),
      limits.map(limit => BigDecimal(limit.periodAccountLimit)),
      balances.map(balance => BigDecimal(balance.periodAvailableGuaranteeBalance)),
      balances.map(balance => BigDecimal(balance.periodAvailableAccountBalance)))
    domain.DutyDefermentAccount(account.number, account.owner, account.accountStatus, balance, isNiAccount.getOrElse(false), isIomAccount.getOrElse(false))
  }
}

case class GeneralGuaranteeAccount(account: AccountWithStatus, guaranteeLimit: Option[String], availableGuaranteeBalance: Option[String]) {
  def toDomain: domain.GeneralGuaranteeAccount = {
    val balance = (guaranteeLimit, availableGuaranteeBalance) match {
      case (Some(limit), Some(balance)) => Some(GeneralGuaranteeBalance(BigDecimal(limit), BigDecimal(balance)))
      case _ => None
    }
    domain.GeneralGuaranteeAccount(account.number, account.owner, account.accountStatus, balance)
  }
}

case class CdsCashAccount(account: AccountWithStatus, availableAccountBalance: Option[String]) {
  def toDomain: domain.CashAccount = {
    val balance = domain.CDSCashBalance(availableAccountBalance.map(BigDecimal(_)))
    domain.CashAccount(account.number, account.owner, account.accountStatus, balance)
  }
}

case class AccountResponseDetail(EORINo: Option[String],
                                 referenceDate: Option[String],
                                 dutyDefermentAccount: Option[Seq[DutyDefermentAccount]],
                                 generalGuaranteeAccount: Option[Seq[GeneralGuaranteeAccount]],
                                 cdsCashAccount: Option[Seq[CdsCashAccount]]) {
  val totalNumberOfAccounts: Int = dutyDefermentAccount.fold(0)(_.size) + generalGuaranteeAccount.fold(0)(_.size) + cdsCashAccount.fold(0)(_.size)
}

case class AccountResponseCommon(status: String, statusText: Option[String], processingDate: String, returnParameters: Option[Seq[ReturnParameters]])

case class AccountsAndBalancesResponse(responseCommon: Option[AccountResponseCommon], responseDetail: AccountResponseDetail)

case class AccountsAndBalancesResponseContainer(accountsAndBalancesResponse: AccountsAndBalancesResponse) {
  def toCdsAccounts(eori: String): domain.CDSAccounts = {
    val details = this.accountsAndBalancesResponse.responseDetail
    val accounts: List[CDSAccount] = List(
      details.dutyDefermentAccount.map(_.map(_.toDomain())),
      details.generalGuaranteeAccount.map(_.map(_.toDomain)),
      details.cdsCashAccount.map(_.map(_.toDomain))
    ).flatten.flatten.filter(_.owner == eori)
    domain.CDSAccounts(eori, accounts)
  }
}

object AccountsAndBalancesResponseContainer {

  implicit val returnParametersReads: Reads[ReturnParameters] = Json.reads[ReturnParameters]

  implicit val accountWithStatusReads: Reads[AccountWithStatus] = Json.reads[AccountWithStatus]
  implicit val limitsReads: Reads[Limits] = Json.reads[Limits]
  implicit val balancesReads: Reads[DefermentBalances] = Json.reads[DefermentBalances]

  implicit val dutyDefermentAccountReads: Reads[DutyDefermentAccount] = Json.reads[DutyDefermentAccount]
  implicit val generalGuaranteeAccountReads: Reads[GeneralGuaranteeAccount] = Json.reads[GeneralGuaranteeAccount]
  implicit val cashAccountReads: Reads[CdsCashAccount] = Json.reads[CdsCashAccount]

  implicit val accountResponseDetailReads: Reads[AccountResponseDetail] = Json.reads[AccountResponseDetail]
  implicit val accountResponseCommonReads: Reads[AccountResponseCommon] = Json.reads[AccountResponseCommon]
  implicit val accountsAndBalancesResponseReads: Reads[AccountsAndBalancesResponse] = Json.reads[AccountsAndBalancesResponse]
  implicit val accountsAndBalancesResponseContainerReads: Reads[AccountsAndBalancesResponseContainer] = Json.reads[AccountsAndBalancesResponseContainer]

}
