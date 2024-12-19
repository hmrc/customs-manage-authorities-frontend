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

package controllers.actions

import models.{NormalMode, withNameToString}
import models.requests.DataRequest
import pages.add.{AccountsPage, EoriNumberPage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import services.AuthorisedAccountsService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VerifyAccountNumbersActionImpl @Inject() (authorisedAccountsService: AuthorisedAccountsService)(implicit
  val executionContext: ExecutionContext
) extends VerifyAccountNumbersAction {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    (request.userAnswers.get(EoriNumberPage), request.userAnswers.get(AccountsPage)) match {
      case (None, _)                           =>
        Future.successful(Left(Redirect(controllers.add.routes.EoriNumberController.onPageLoad(NormalMode))))
      case (_, None)                           =>
        Future.successful(Left(Redirect(controllers.add.routes.AccountsController.onPageLoad(NormalMode))))
      case (Some(enteredEori), Some(accounts)) =>
        authorisedAccountsService.getAuthorisedAccounts(enteredEori)(request, hc).map { authorisedAccounts =>
          if (authorisedAccounts.alreadyAuthorisedAccounts.exists(accounts.contains)) {
            Left(Redirect(controllers.add.routes.AccountsController.onPageLoad(NormalMode)))
          } else {
            Right(request)
          }
        }
    }
  }
}

trait VerifyAccountNumbersAction extends ActionRefiner[DataRequest, DataRequest]
