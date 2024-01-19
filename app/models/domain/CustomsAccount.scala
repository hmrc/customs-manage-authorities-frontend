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

package models.domain

import play.api.{Logger, LoggerLike}
import play.api.i18n.Messages
import play.api.libs.json._

import scala.math.Numeric.BigDecimalIsFractional.zero

sealed trait CDSAccountStatus

case object AccountStatusOpen extends CDSAccountStatus
case object AccountStatusClosed extends CDSAccountStatus
case object AccountStatusSuspended extends CDSAccountStatus
case object AccountStatusPending extends CDSAccountStatus

object CDSAccountStatus {

  val log: LoggerLike = Logger(this.getClass)

  implicit val CDSAccountStatusReads: Reads[CDSAccountStatus] = (json: JsValue) => {
    json.as[String] match {
      case status if status.equalsIgnoreCase("Open") => JsSuccess(AccountStatusOpen)
      case status if status.equalsIgnoreCase("Suspended") => JsSuccess(AccountStatusSuspended)
      case status if status.equalsIgnoreCase("Closed") => JsSuccess(AccountStatusClosed)
      case status if status.equalsIgnoreCase("Pending") => JsSuccess(AccountStatusPending)
      case unknown => log.warn(s"Invalid account status: $unknown"); JsSuccess(AccountStatusOpen)
    }
  }

  implicit val CDSAccountStatusWrites: Writes[CDSAccountStatus] = {
    case AccountStatusOpen => JsString("Open")
    case AccountStatusClosed => JsString("Closed")
    case AccountStatusSuspended => JsString("Suspended")
    case AccountStatusPending => JsString("Pending")
  }
}

trait Balances

case class DutyDefermentBalance(periodGuaranteeLimit: Option[BigDecimal],
                                periodAccountLimit: Option[BigDecimal],
                                periodAvailableGuaranteeBalance: Option[BigDecimal],
                                periodAvailableAccountBalance: Option[BigDecimal]
                               ) extends Balances {

  val (usedFunds, usedPercentage) = (periodAccountLimit, periodAvailableAccountBalance) match {
    case (Some(limit), Some(balance)) if limit <= BigDecimal(0) =>
      val fundsUsed = limit - balance

      (fundsUsed, BigDecimal(maxUsedPercentage))

    case (Some(limit), Some(balance)) =>
      val fundsUsed = limit - balance
      val percentage = fundsUsed / limit * BigDecimal(maxUsedPercentage)

      (fundsUsed, percentage)

    case _ => (BigDecimal(0), BigDecimal(0))
  }

  val availableBalance: BigDecimal = periodAvailableAccountBalance.getOrElse(BigDecimal(0))
}

case class GeneralGuaranteeBalance(GuaranteeLimit: BigDecimal,
                                   AvailableGuaranteeBalance: BigDecimal) extends Balances {

  val usedFunds: BigDecimal = GuaranteeLimit - AvailableGuaranteeBalance
  val usedPercentage: BigDecimal =
    if (GuaranteeLimit.compare(zero) == 0) zero else usedFunds / GuaranteeLimit * maxUsedPercentage

}

case class CDSCashBalance(AvailableAccountBalance: Option[BigDecimal]) extends Balances

object Balances {
  implicit val dutyDefermentBalanceFormat: Format[DutyDefermentBalance] = Json.format[DutyDefermentBalance]
  implicit val generalGuaranteeBalanceFormat: Format[GeneralGuaranteeBalance] = Json.format[GeneralGuaranteeBalance]
  implicit val cashBalanceFormat: Format[CDSCashBalance] = Json.format[CDSCashBalance]
}

sealed trait CDSAccount {
  val number: String
  val owner: String
  val accountType: String
  val isNiAccount: Boolean
}

case class DutyDefermentAccount(number: String,
                                owner: String,
                                status: CDSAccountStatus,
                                balances: DutyDefermentBalance,
                                isNiAccount: Boolean = false,
                                isIomAccount: Boolean = false
                               ) extends Ordered[DutyDefermentAccount] with CDSAccount {
  override def compare(that: DutyDefermentAccount): Int = number.compareTo(that.number)
  override val accountType: String = "dutyDeferment"
}

case class GeneralGuaranteeAccount(number: String,
                                   owner: String,
                                   status: CDSAccountStatus,
                                   balances: Option[GeneralGuaranteeBalance],
                                   isNiAccount: Boolean = false
                                  ) extends CDSAccount {
  override val accountType: String = "generalGuarantee"
}

case class CashAccount(number: String,
                       owner: String,
                       status: CDSAccountStatus,
                       balances: CDSCashBalance,
                       isNiAccount: Boolean = false
                      ) extends CDSAccount {
  override val accountType: String = "cash"
}

object CDSAccount {
  implicit val format: OFormat[CDSAccount] = Json.format[CDSAccount]
  implicit val writes: Writes[List[CDSAccount]] = Writes.iterableWrites2[CDSAccount, List[CDSAccount]]
  implicit val dutyDefermentAccountFormat: Format[DutyDefermentAccount] = Json.format[DutyDefermentAccount]
  implicit val generalGuaranteeAccountFormat: Format[GeneralGuaranteeAccount] = Json.format[GeneralGuaranteeAccount]
  implicit val cashAccountFormat: Format[CashAccount] = Json.format[CashAccount]

  def formattedAccountType(cdsAccount: CDSAccount)(implicit messages: Messages): String = {
    cdsAccount match {
      case CashAccount(_, _, _, _, _) => messages("remove.heading.caption.CdsCashAccount", cdsAccount.number)
      case DutyDefermentAccount(_, _, _, _, _, _) => messages("remove.heading.caption.CdsDutyDefermentAccount", cdsAccount.number)
      case GeneralGuaranteeAccount(_, _, _, _, _) => messages("remove.heading.caption.CdsGeneralGuaranteeAccount", cdsAccount.number)
    }
  }
}

case class CDSAccounts(eori: String, accounts: List[CDSAccount]) {
  lazy val myAccounts = accounts
  lazy val closedAccounts: Seq[CDSAccount] = myAccounts.flatMap {
    case value@GeneralGuaranteeAccount(_, _, status, _, _) if status == AccountStatusClosed => Some(value)
    case value@CashAccount(_, _, status, _, _) if status == AccountStatusClosed => Some(value)
    case value@DutyDefermentAccount(_, _, status, _, _, _) if status == AccountStatusClosed => Some(value)
    case _ => None
  }
  lazy val pendingAccounts: Seq[CDSAccount] = myAccounts.flatMap {
    case value@GeneralGuaranteeAccount(_, _, status, _, _) if status == AccountStatusPending => Some(value)
    case value@CashAccount(_, _, status, _, _) if status == AccountStatusPending => Some(value)
    case value@DutyDefermentAccount(_, _, status, _, _, _) if status == AccountStatusPending => Some(value)
    case _ => None
  }

  lazy val openAccounts: Seq[CDSAccount] = myAccounts.diff(closedAccounts).diff(pendingAccounts)
  def alreadyAuthorised(accountNumbers: Seq[String]): Seq[CDSAccount] = openAccounts.filter(accountNumbers contains _.number)
  def canAuthoriseAccounts(accountNumbers: Seq[String]): Seq[CDSAccount] = openAccounts.diff(alreadyAuthorised(accountNumbers))
}

object CDSAccounts {
  implicit val format: OFormat[CDSAccounts] = Json.format[CDSAccounts]
}
