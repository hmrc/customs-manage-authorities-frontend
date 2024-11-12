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

package services

import config.FrontendAppConfig
import models.domain.{AuditModel, EORI, SdesFile}
import play.api.http.HeaderNames
import play.api.{Logger, LoggerLike}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import utils.StringUtils.hyphen

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditingService @Inject()(appConfig: FrontendAppConfig, auditConnector: AuditConnector) {

  val log: LoggerLike = Logger(this.getClass)

  private val referrer: HeaderCarrier => String = _.headers(Seq(HeaderNames.REFERER)).headOption.fold(hyphen)(_._2)

  def auditFiles[T <: SdesFile](files: Seq[T], eori: EORI)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[AuditResult]] = {
    Future.sequence(files.map { file => audit(file.auditModelFor(eori)) })
  }

  def audit(auditModel: AuditModel)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val dataEvent = toExtendedDataEvent(appConfig.appName, auditModel, referrer(hc))

    auditConnector.sendExtendedEvent(dataEvent)
  }

  private def toExtendedDataEvent(appName: String,
                                  auditModel: AuditModel,
                                  path: String)(implicit hc: HeaderCarrier): ExtendedDataEvent =
    ExtendedDataEvent(
      auditSource = appName,
      auditType = auditModel.auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(auditModel.transactionName, path),
      detail = auditModel.detail)
}
