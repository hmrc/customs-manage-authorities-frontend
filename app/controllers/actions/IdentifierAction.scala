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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.InternalId
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Constants.{ENROLMENT_IDENTIFIER, ENROLMENT_KEY}

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
  extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(override val authConnector: AuthConnector,
                                              config: FrontendAppConfig,
                                              val parser: BodyParsers.Default)
                                             (implicit val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions {
  override def invokeBlock[A](request: Request[A],
                              block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(
      Retrievals.credentials and
        Retrievals.name and
        Retrievals.email and
        Retrievals.affinityGroup and
        Retrievals.internalId and
        Retrievals.allEnrolments) {

      case Some(credentials) ~ name ~ email ~ Some(affinityGroup) ~ Some(internalId) ~ allEnrolments =>

        allEnrolments.getEnrolment(ENROLMENT_KEY).flatMap(_.getIdentifier(ENROLMENT_IDENTIFIER)) match {
          case Some(eori) =>
            block(IdentifierRequest(request, InternalId(internalId), credentials, affinityGroup, name, email, eori.value))

          case None => Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
        }

      case _ => Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))

    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue_url" -> Seq(config.loginContinueUrl)))

      case _: InsufficientEnrolments =>
        Redirect(routes.UnauthorisedController.onPageLoad)

      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }
}
