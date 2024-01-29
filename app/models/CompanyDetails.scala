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

import play.api.libs.json.{Format, Json, OFormat, Reads, Writes}

case class CompanyDetails(eori: String, name: Option[String])

object CompanyDetails {
  implicit val companyDetailsFormat: Format[CompanyDetails] = Json.format[CompanyDetails]
  implicit val companyDetailsOFormat: OFormat[CompanyDetails] = Json.format[CompanyDetails]
  implicit val companyDetailsReads: Reads[CompanyDetails] = Json.reads[CompanyDetails]
  implicit val companyDetailsWrites: Writes[CompanyDetails] = Json.writes[CompanyDetails]
}
