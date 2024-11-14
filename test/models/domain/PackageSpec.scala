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

package models.domain

import base.SpecBase
import play.api.mvc.PathBindable

class PackageSpec extends SpecBase {
  "LinkId" should {
    val linkPathBindable = implicitly[PathBindable[Option[LinkId]]]

    "bind the link to the value" in {
      val result: Either[String, Option[LinkId]] = linkPathBindable.bind("test_key", "test_value")

      result.leftSideValue.getOrElse(None) mustBe Option("test_value")
    }

    "unbind the key" in {
      val result: String = linkPathBindable.unbind("test_key", Some("test_value"))
      result mustBe "Some(test_value)"
    }
  }
}
