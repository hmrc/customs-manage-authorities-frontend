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

import base.SpecBase
import pages.add.{AccountsPage, AuthorisedUserPage}

class PageSpec extends SpecBase {

  "Page" must {
    "Pages defined toString converts page to string" in {
      val page = AccountsPage
      val res = Page.toString(page)
      res mustBe page.toString
    }
  }

  "AuthorisedUserPage" must {
    "AuthorisedUserPage defined toString converts page to string" in {
      val page = AuthorisedUserPage
      val res = page.toString
      res mustBe "authorisedUser"
    }
  }
}
