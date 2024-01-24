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

package pages

import play.api.libs.json.{JsPath, Json, OFormat}
import queries.{Gettable, Settable}

case class ConfirmationDetails(eori: String,
                               startDate: Option[String],
                               companyName: Option[String],
                               multipleAccounts: Boolean)

object ConfirmationDetails {
  implicit val format: OFormat[ConfirmationDetails] = Json.format[ConfirmationDetails]
}

case object ConfirmationPage extends Page
  with Gettable[ConfirmationDetails]
  with Settable[ConfirmationDetails] {
  override def path: JsPath = JsPath \ toString

  override def toString: String = "confirmationDetails"
}
