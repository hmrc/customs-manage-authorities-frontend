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

package models

import play.api.libs.json.{Json, OFormat}

case class EmailVerifiedResponse(verifiedEmail: Option[String])

object EmailVerifiedResponse {
  implicit val format: OFormat[EmailVerifiedResponse] = Json.format[EmailVerifiedResponse]
}

case class EmailUnverifiedResponse(unVerifiedEmail: Option[String])

object EmailUnverifiedResponse {
  implicit val format: OFormat[EmailUnverifiedResponse] = Json.format[EmailUnverifiedResponse]
}

sealed trait EmailResponses

case object UnverifiedEmail extends EmailResponses

case class UndeliverableEmail(email: String) extends EmailResponses
