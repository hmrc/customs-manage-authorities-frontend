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

package controllers.remove

import connectors.CustomsFinancialsConnector
import controllers.actions._
import forms.RemoveFormProvider
import models.{AuthorityEnd, ErrorResponse, MissingAccountError, MissingAuthorityError, NormalMode, SubmissionError}
import models.domain.AuthoritiesWithId
import models.requests.RevokeAuthorityRequest
import pages.edit.EditAuthorityEndPage
import pages.remove.RemoveAuthorisedUserPage
import play.api.Logging
import play.api.i18n._
import play.api.mvc._
import repositories.SessionRepository
import services.AuthoritiesCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.RemoveViewModel
import views.html.remove.RemoveView

import javax.inject.Inject
import scala.concurrent._

class RemoveAuthorisedUserController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                service: AuthoritiesCacheService,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                connector: CustomsFinancialsConnector,
                                                sessionRepository: SessionRepository,
                                                identify: IdentifierAction,
                                                formProvider: RemoveFormProvider,
                                                implicit val controllerComponents: MessagesControllerComponents,
                                                view: RemoveView
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val form = formProvider()

  //TODO: clean up duplicate fields
  def onPageLoad(accountId: String, authorityId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      service.retrieveAuthorities(request.internalId).map { accountsWithAuthorities: AuthoritiesWithId =>
        accountsWithAuthorities.authorities.get(accountId).map { account =>
          account.authorities.get(authorityId).map { authority =>
            Right(Ok(view(form, RemoveViewModel(accountId, authorityId, account, authority), accountId, authorityId)))
          }.getOrElse(Left(MissingAuthorityError))
        }.getOrElse(Left(MissingAccountError))
      } map {
        case Right(result) => result
        case Left(failure) => errorPage(failure); Redirect(controllers.routes.TechnicalDifficulties.onPageLoad())
      }
  }

  def onSubmit(accountId: String, authorityId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      service.retrieveAuthorities(request.internalId).flatMap { accountsWithAuthorities: AuthoritiesWithId =>
        accountsWithAuthorities.authorities.get(accountId).map { account =>
          account.authorities.get(authorityId).map { authority =>
            form.bindFromRequest().fold(
              formWithErrors => {
                Future.successful(BadRequest(view(formWithErrors, RemoveViewModel(accountId, authorityId, account, authority), accountId, authorityId)))
              },
              authorisedUser => {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveAuthorisedUserPage(accountId, authorityId), authorisedUser))
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(routes.RemoveCheckYourAnswers.onPageLoad(accountId, authorityId))
              }
            )
          }.getOrElse(Future.successful(errorPage(MissingAuthorityError)))
        }.getOrElse(Future.successful(errorPage(MissingAccountError)))
      }
  }


  private def errorPage(error: ErrorResponse) = {
    logger.error(error.msg)
    Redirect(controllers.routes.TechnicalDifficulties.onPageLoad())
  }

}
