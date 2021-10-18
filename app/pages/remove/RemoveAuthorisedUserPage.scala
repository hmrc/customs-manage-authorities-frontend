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

package pages.remove

import models.domain.AuthorisedUser
import pages.QuestionPage
import play.api.libs.json.JsPath

case class RemoveAuthorisedUserPage(accountId: String, authorityId: String) extends QuestionPage[AuthorisedUser] {
  override def path: JsPath = JsPath \ "remove" \ accountId \ authorityId \ toString
  override def toString: String = "removeAuthorisedUser"
}