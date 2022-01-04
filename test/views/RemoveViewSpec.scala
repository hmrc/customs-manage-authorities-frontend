/*
 * Copyright 2022 HM Revenue & Customs
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

package views

import base.SpecBase
import config.FrontendAppConfig
import forms.AuthorisedUserFormProvider
import models.domain.{AccountStatusOpen, AccountWithAuthoritiesWithId, CdsCashAccount, StandingAuthority}
import org.jsoup.Jsoup
import org.scalatest.Matchers._
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import viewmodels.RemoveViewModel
import views.html.remove.RemoveAuthorisedUserView

import java.time.LocalDate

class RemoveViewSpec extends SpecBase {


  "Remove view" should {
      "have back link" in new Setup {
        val viewModel = RemoveViewModel("a", "b", accountsWithAuthoritiesWithId, standingAuthority)
        view(viewModel).getElementsByClass("govuk-back-link").attr("href") mustBe s"/customs/manage-authorities/view-authority/a/b"
      }
    }


  trait Setup  {
    val startDate = LocalDate.parse("2020-03-01")
    val endDate = LocalDate.parse("2020-04-01")
    val standingAuthority = StandingAuthority("EORI", startDate, viewBalance = false)
    val accountsWithAuthoritiesWithId = AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority))

    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")
    val app = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
    implicit val appConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages = Helpers.stubMessages()

    private val formProvider = new AuthorisedUserFormProvider()
    private val form = formProvider()

    def view(viewModel: RemoveViewModel) = Jsoup.parse(app.injector.instanceOf[RemoveAuthorisedUserView].apply(form,viewModel).body)
  }
}
