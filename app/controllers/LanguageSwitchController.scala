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

package controllers

import com.google.inject.Inject
import config.FrontendAppConfig
import models.Language
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

class LanguageSwitchController @Inject()(
                                          appConfig: FrontendAppConfig,
                                          override implicit val messagesApi: MessagesApi,
                                          val controllerComponents: MessagesControllerComponents
                                        ) extends FrontendBaseController with I18nSupport {

  private def fallbackURL: String = routes.IndexController.onPageLoad().url

  def switchToLanguage(language: Language): Action[AnyContent] = Action {
    implicit request =>

      val languageToUse = if (appConfig.languageTranslationEnabled) {
        language
      } else {
        Language.English
      }

      val redirectURL = request.headers.get(REFERER).getOrElse(fallbackURL)
      Redirect(redirectURL).withLang(languageToUse.lang)
  }
}
