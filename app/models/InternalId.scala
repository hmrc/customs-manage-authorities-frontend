/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}

final case class InternalId(value: String)

object InternalId {
  private def stringFormat[A](fromString: String => A)(makeString: A => String): Format[A] = new Format[A] {
    def reads(json: JsValue): JsResult[A] = json match {
      case JsString(str) => JsSuccess(fromString(str))
      case _ => JsError(s"Expected JSON string type")
    }

    def writes(o: A): JsValue = JsString(makeString(o))
  }


  implicit val formal: Format[InternalId] = stringFormat(InternalId.apply)(_.value)
}
