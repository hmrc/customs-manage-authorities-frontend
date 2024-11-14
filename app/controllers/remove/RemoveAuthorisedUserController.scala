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

package controllers.remove

import config.FrontendAppConfig
import controllers.actions._
import forms.AuthorisedUserFormProvider
import models.{ErrorResponse, MissingAccountError, MissingAuthorityError}
import pages.remove.RemoveAuthorisedUserPage
import play.api.Logging
import play.api.i18n._
import play.api.mvc._
import repositories.SessionRepository
import services.{AccountAndAuthority, AuthoritiesCacheService, NoAccount, NoAuthority}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.RemoveViewModel
import views.html.remove.RemoveAuthorisedUserView

import javax.inject.Inject
import scala.concurrent._

class RemoveAuthorisedUserController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                service: AuthoritiesCacheService,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                sessionRepository: SessionRepository,
                                                identify: IdentifierAction,
                                                formProvider: AuthorisedUserFormProvider,
                                                implicit val controllerComponents: MessagesControllerComponents,
                                                view: RemoveAuthorisedUserView
                                              )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
  extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()

  def onPageLoad(accountId: String,
                 authorityId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>

        service.getAccountAndAuthority(request.internalId, authorityId, accountId).map {
          case Left(NoAuthority) => errorPage(MissingAuthorityError)
          case Left(NoAccount) => errorPage(MissingAccountError)
          case Right(AccountAndAuthority(account, authority)) =>
            val populatedForm = request.userAnswers.get(RemoveAuthorisedUserPage(accountId, authorityId)) match {
              case Some(value) => form.fill(value)
              case None => form
            }

            Ok(view(populatedForm, RemoveViewModel(accountId, authorityId, account, authority)))
        }
    }

  def onSubmit(accountId: String,
               authorityId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      service.getAccountAndAuthority(request.internalId, authorityId, accountId).flatMap {
        case Left(NoAuthority) => Future.successful(errorPage(MissingAuthorityError))
        case Left(NoAccount) => Future.successful(errorPage(MissingAccountError))
        case Right(AccountAndAuthority(account, authority)) =>
          form.bindFromRequest().fold(
            formWithErrors => {
              Future.successful(BadRequest(view(
                formWithErrors,
                RemoveViewModel(accountId, authorityId, account, authority))
              ))
            },
            authorisedUser => {
              for {
                updatedAnswers <- Future.fromTry(
                  request.userAnswers.set(RemoveAuthorisedUserPage(accountId, authorityId), authorisedUser))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(routes.RemoveCheckYourAnswers.onPageLoad(accountId, authorityId))
            }
          )
      }
  }

  private def errorPage(error: ErrorResponse): Result = {
    logger.error(error.msg)
    Redirect(controllers.routes.TechnicalDifficulties.onPageLoad)
  }
}
