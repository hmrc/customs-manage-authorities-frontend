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

package generators

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages.add._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryAuthorisedUserUserAnswersEntry: Arbitrary[(AuthorisedUserPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AuthorisedUserPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryShowBalanceUserAnswersEntry: Arbitrary[(ShowBalancePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ShowBalancePage.type]
        value <- arbitrary[ShowBalance].map(Json.toJson(_)(ShowBalance.writes))
      } yield (page, value)
    }

  implicit lazy val arbitraryAuthorityStartDateUserAnswersEntry: Arbitrary[(AuthorityStartDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AuthorityStartDatePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAuthorityStartUserAnswersEntry: Arbitrary[(AuthorityStartPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AuthorityStartPage.type]
        value <- arbitrary[AuthorityStart].map(Json.toJson(_)(AuthorityStart.writes))
      } yield (page, value)
    }

  implicit lazy val arbitraryEoriNumberUserAnswersEntry: Arbitrary[(EoriNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EoriNumberPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }
}
