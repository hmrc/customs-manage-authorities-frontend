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

package utils

object Constants {
  val MDG_ACK_REF_LENGTH = 32
  val RANDOM_GENERATION_INT_LENGTH = 10

  val CASH_ACCOUNT_TYPE = "cash"
  val GENERAL_GUARANTEE_ACCOUNT_TYPE = "generalGuarantee"
  val DUTY_DEFERMENT_ACCOUNT_TYPE = "dutyDeferment"

  val FIXED_DATE_TIME_YEAR = 2027
  val FIXED_DATE_TIME_MONTH_OF_YEAR = 12
  val FIXED_DATE_TIME_DAY_OF_MONTH = 20
  val FIXED_DATE_TIME_HOUR_OF_DAY = 12
  val FIXED_DATE_TIME_MIN_OF_HOUR = 30

  val ENROLMENT_KEY = "HMRC-CUS-ORG"
  val ENROLMENT_IDENTIFIER = "EORINumber"
}
