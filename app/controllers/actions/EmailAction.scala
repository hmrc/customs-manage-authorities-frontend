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

import connectors.CustomsDataStoreConnector
import models.requests.IdentifierRequest
import models.{EmailResponses, UndeliverableEmail, UnverifiedEmail}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailAction @Inject()(dataStoreConnector: CustomsDataStoreConnector)(
  implicit val executionContext: ExecutionContext, val messagesApi: MessagesApi)
  extends ActionFilter[IdentifierRequest]
    with I18nSupport {

  def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    dataStoreConnector.getEmail(request.eoriNumber).map {
      case Left(value) => checkEmailResponseAndRedirect(value)
      case Right(_) => None
    }.recover { case _ => None }
  }

  private def checkEmailResponseAndRedirect(value: EmailResponses): Option[Result] = {
    value match {
      case UnverifiedEmail => Some(Redirect(controllers.routes.EmailController.showUnverified()))
      case UndeliverableEmail(_) => Some(Redirect(controllers.routes.EmailController.showUndeliverable()))
    }
  }
}
