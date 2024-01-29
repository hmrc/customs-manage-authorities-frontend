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

package models.requests

import models.{InternalId, UserAnswers}
import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name}

trait RequestWithUserAnswers[A] extends Request[A] {
  def internalId: InternalId

  def affinityGroup: AffinityGroup

  def credentials: Credentials

  def userAnswers: UserAnswers
}

final case class OptionalDataRequest[A](request: Request[A],
                                        internalId: InternalId,
                                        credentials: Credentials,
                                        affinityGroup: AffinityGroup,
                                        name: Option[Name],
                                        email: Option[String],
                                        eoriNumber: String,
                                        userAnswers: Option[UserAnswers]) extends WrappedRequest[A](request)

final case class DataRequest[A](request: Request[A],
                                internalId: InternalId,
                                credentials: Credentials,
                                affinityGroup: AffinityGroup,
                                name: Option[Name],
                                email: Option[String],
                                eoriNumber: String,
                                userAnswers: UserAnswers)
  extends WrappedRequest[A](request) with RequestWithUserAnswers[A]
