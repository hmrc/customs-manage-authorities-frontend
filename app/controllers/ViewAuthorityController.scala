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

package controllers

import config.FrontendAppConfig
import connectors.CustomsDataStoreConnector
import controllers.actions._
import models.UserAnswers
import play.api.i18n._
import play.api.mvc._
import services._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.EditOrRemoveView

import javax.inject.Inject
import scala.concurrent._

class ViewAuthorityController @Inject()(view: EditOrRemoveView,
                                        mcc: MessagesControllerComponents,
                                        authoritiesCacheService: AuthoritiesCacheService,
                                        editSessionService: EditSessionService,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        dataStore: CustomsDataStoreConnector
                                       )(implicit executionContext: ExecutionContext, appConfig: FrontendAppConfig)
  extends FrontendController(mcc)
    with I18nSupport {
  def onPageLoad(accountId: String,
                 authorityId: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    authoritiesCacheService.getAccountAndAuthority(
      request.internalId,
      authorityId,
      accountId
    ).flatMap {
      case Left(_) => Future.successful(Redirect(routes.ManageAuthoritiesController.onPageLoad))
      case Right(AccountAndAuthority(account, authority)) =>
        val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.internalId.value))

        editSessionService.resetUserAnswers(accountId,
          authorityId,
          userAnswers,
          authority,
          account,
          dataStore).map { checkYourAnswersEditHelper =>
          Ok(view(checkYourAnswersEditHelper, accountId, authorityId))
        }
    }
  }
}
