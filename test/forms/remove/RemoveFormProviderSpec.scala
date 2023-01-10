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

package forms.remove

import forms.AuthorisedUserFormProvider
import forms.behaviours.StringFieldBehaviours
import models.domain.AuthorisedUser
import play.api.data.FormError

class RemoveFormProviderSpec extends StringFieldBehaviours {

  val nameRequiredKey = "remove.error.fullName.required"
  val roleRequiredKey = "remove.error.jobRole.required"
  val confirmationRequiredKey = "remove.error.confirmation.required"
  val nameLengthKey = "remove.error.fullName.length"
  val roleLengthKey = "remove.error.jobRole.length"
  val lengthKey = "remove.error.length"
  val maxLength = 255
  val nameInvalidKey = "remove.error.fullName.malicious"
  val roleInvalidKey = "remove.error.jobRole.malicious"
  val textFieldRegex: String = """^[^(){}$<>\[\]\\\/]*$"""

  val user = AuthorisedUser("name", "role")

  val form = new AuthorisedUserFormProvider()()

  "RemoveFormProvider" must {

    "bind when all fields are present" in {
      val result = form.bind(Map(
        "fullName" -> user.userName,
        "jobRole" -> user.userRole,
        "confirmation" -> "true"))

      result.value.value shouldBe user
    }

    "not bind when fullName is missing" in {
      val result = form.bind(Map(
        "jobRole" -> user.userRole,
        "confirmation" -> "true"))

      result.errors shouldEqual Seq(FormError("fullName", nameRequiredKey))
    }

    "not bind when jobRole is missing" in {
      val result = form.bind(Map(
        "fullName" -> user.userName,
        "confirmation" -> "true"))

      result.errors shouldEqual Seq(FormError("jobRole", roleRequiredKey))
    }


    "not bind empty values" in {
      val result = form.bind(Map(
        "fullName" -> "",
        "jobRole" -> ""))

      result.errors shouldEqual Seq(
        FormError("fullName", nameRequiredKey),
        FormError("jobRole", roleRequiredKey)
      )
    }

    "not bind when fullName is over maximum length" in {
      val result = form.bind(Map(
        "fullName" -> "a" * (maxLength + 1),
        "jobRole" -> user.userRole,
        "confirmation" -> "true"))

      result.errors shouldEqual Seq(FormError("fullName", nameLengthKey, Seq(maxLength)))
    }

    "not bind when jobRole is over maximum length" in {
      val result = form.bind(Map(
        "fullName" -> user.userName,
        "jobRole" -> "a" * (maxLength + 1),
        "confirmation" -> "true"))

      result.errors shouldEqual Seq(FormError("jobRole", roleLengthKey, Seq(maxLength)))
    }

    "not bind when fullName and jobRole contains angle brackets" in {
      val result = form.bind(Map(
        "fullName" -> "<script>",
        "jobRole" -> "<script>",
        "confirmation" -> "true"))

      result.errors shouldEqual Seq(FormError("fullName", nameInvalidKey,Seq(textFieldRegex)),FormError("jobRole", roleInvalidKey,Seq(textFieldRegex)))
    }

    "not bind when fullName and jobRole contains square brackets" in {
      val result = form.bind(Map(
        "fullName" -> "[1]",
        "jobRole" -> "[1]",
        "confirmation" -> "true"))

      result.errors shouldEqual Seq(FormError("fullName", nameInvalidKey,Seq(textFieldRegex)),FormError("jobRole", roleInvalidKey,Seq(textFieldRegex)))
    }

    "not bind when fullName and jobRole contains braces" in {
      val result = form.bind(Map(
        "fullName" -> "alert(1)",
        "jobRole" -> "alert(1)",
        "confirmation" -> "true"))

      result.errors shouldEqual Seq(FormError("fullName", nameInvalidKey,Seq(textFieldRegex)),FormError("jobRole", roleInvalidKey,Seq(textFieldRegex)))
    }

    "not bind when fullName and jobRole contains parentheses" in {
      val result = form.bind(Map(
        "fullName" -> "{expression}",
        "jobRole" -> "{expression}",
        "confirmation" -> "true"))

      result.errors shouldEqual Seq(FormError("fullName", nameInvalidKey,Seq(textFieldRegex)),FormError("jobRole", roleInvalidKey,Seq(textFieldRegex)))
    }

    "not bind when fullName and jobRole contains dollar sign" in {
      val result = form.bind(Map(
        "fullName" -> "alert$",
        "jobRole" -> "alert$",
        "confirmation" -> "true"))

      result.errors shouldEqual Seq(FormError("fullName", nameInvalidKey,Seq(textFieldRegex)),FormError("jobRole", roleInvalidKey,Seq(textFieldRegex)))
    }

    "not bind when fullName and jobRole contains forward slash" in {
      val result = form.bind(Map(
        "fullName" -> "/test",
        "jobRole" -> "/test",
        "confirmation" -> "true"))

      result.errors shouldEqual Seq(FormError("fullName", nameInvalidKey,Seq(textFieldRegex)),FormError("jobRole", roleInvalidKey,Seq(textFieldRegex)))
    }

    "not bind when fullName and jobRole contains backward slash" in {
      val result = form.bind(Map(
        "fullName" -> "\\test",
        "jobRole" -> "\\test",
        "confirmation" -> "true"))

      result.errors shouldEqual Seq(FormError("fullName", nameInvalidKey,Seq(textFieldRegex)),FormError("jobRole", roleInvalidKey,Seq(textFieldRegex)))
    }
  }
}
