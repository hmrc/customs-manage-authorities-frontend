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

import base.SpecBase
import com.google.inject.Inject
import controllers.routes
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier
import utils.StringUtils.emptyString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  "Auth Action" when {

    "the user hasn't logged in" must {

      "redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()
        val config      = frontendAppConfig(application)

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

        val authAction =
          new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), config, bodyParsers)

        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest())

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(config.loginUrl)
      }
    }

    "the user's session has expired" must {

      "redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()
        val config      = frontendAppConfig(application)

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

        val authAction =
          new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new BearerTokenExpired),
            config,
            bodyParsers
          )

        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest())

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(config.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" must {

      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()
        val config      = frontendAppConfig(application)

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

        val authAction =
          new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientEnrolments),
            config,
            bodyParsers
          )

        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest())

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user doesn't have sufficient confidence level" must {

      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()
        val config      = frontendAppConfig(application)

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

        val authAction =
          new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientConfidenceLevel),
            config,
            bodyParsers
          )

        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest())

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user used an unaccepted auth provider" must {

      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()
        val config      = frontendAppConfig(application)

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

        val authAction =
          new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAuthProvider),
            config,
            bodyParsers
          )

        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest())

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user has an unsupported affinity group" must {

      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()
        val config      = frontendAppConfig(application)

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

        val authAction =
          new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
            config,
            bodyParsers
          )

        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest())

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user has an unsupported credential role" must {

      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()
        val config      = frontendAppConfig(application)

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

        val authAction =
          new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedCredentialRole),
            config,
            bodyParsers
          )

        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest())

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }
  }

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
  }
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = emptyString

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}
