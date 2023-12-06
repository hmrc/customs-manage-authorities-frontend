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

package controllers

import config.FrontendAppConfig
import connectors.CustomsFinancialsConnector
import controllers.actions.IdentifierAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Logger, LoggerLike}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.email.verify_your_email

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EmailController @Inject()(authenticate: IdentifierAction,
                                verifyEmailView: verify_your_email,
                                customsFinancialsApiConnector: CustomsFinancialsConnector,
                                implicit val mcc: MessagesControllerComponents)
                               (implicit val appConfig: FrontendAppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  val log: LoggerLike = Logger(this.getClass)

  def showUnverified():Action[AnyContent] = authenticate async { implicit request =>
    customsFinancialsApiConnector.isEmailUnverified.map {
      case email => Ok(verifyEmailView(appConfig.emailFrontendUrl, email))
    }
  }
}
