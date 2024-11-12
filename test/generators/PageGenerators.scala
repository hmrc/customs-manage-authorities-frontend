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

package generators

import org.scalacheck.Arbitrary
import pages.add._

trait PageGenerators {

  implicit lazy val arbitraryAuthorisedUserPage: Arbitrary[AuthorisedUserPage.type] =
    Arbitrary(AuthorisedUserPage)

  implicit lazy val arbitraryAuthorityEndDatePage: Arbitrary[AuthorityEndDatePage.type] =
    Arbitrary(AuthorityEndDatePage)

  implicit lazy val arbitraryAuthorityEndPage: Arbitrary[AuthorityEndPage.type] =
    Arbitrary(AuthorityEndPage)

  implicit lazy val arbitraryShowBalancePage: Arbitrary[ShowBalancePage.type] =
    Arbitrary(ShowBalancePage)

  implicit lazy val arbitraryAuthorityStartDatePage: Arbitrary[AuthorityStartDatePage.type] =
    Arbitrary(AuthorityStartDatePage)

  implicit lazy val arbitraryAuthorityStartPage: Arbitrary[AuthorityStartPage.type] =
    Arbitrary(AuthorityStartPage)

  implicit lazy val arbitraryEoriNumberPage: Arbitrary[EoriNumberPage.type] =
    Arbitrary(EoriNumberPage)

  implicit lazy val arbitraryAccountsPage: Arbitrary[AccountsPage.type] =
    Arbitrary(AccountsPage)
}
