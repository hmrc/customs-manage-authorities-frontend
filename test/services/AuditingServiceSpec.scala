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

import base.SpecBase
import config.FrontendAppConfig
import models.domain.FileFormat.Csv
import models.domain.{AuditModel, DownloadStatementAuditData, StandingAuthorityFile, StandingAuthorityMetadata}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import utils.TestData.{EORI_NUMBER, FILE_SIZE_500, START_DATE_1}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditingServiceSpec extends SpecBase {

  "AuditingService" should {

    "create the correct audit event for SDES file retrieval" in new Setup {
      val auditData = DownloadStatementAuditData.apply(gbStandingAuth1.metadata, eori)
      val auditModel = AuditModel(
        AUDIT_STANDING_AUTHORITIES, gbStandingAuth1.metadata.fileRole.transactionName, Json.toJson(auditData))

      await(auditingService.audit(auditModel))

      val dataEventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
      verify(mockAuditConnector).sendExtendedEvent(dataEventCaptor.capture)(any, any)

      val dataEvent: ExtendedDataEvent = dataEventCaptor.getValue

      dataEvent.auditSource must be(expectedAuditSource)
      dataEvent.auditType must be(AUDIT_STANDING_AUTHORITIES)
      dataEvent.detail must be(Json.toJson(auditData))
      dataEvent.tags.toString() must include(AUDIT_STANDING_AUTHORITIES_TRANSACTION_NAME)
    }
  }

  trait Setup {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val expectedAuditSource = "customs-manage-authorities-frontend"
    val eori = "EORI"
    val AUDIT_STANDING_AUTHORITIES = "DisplayStandingAuthoritiesCSV"
    val AUDIT_STANDING_AUTHORITIES_TRANSACTION_NAME = "Display standing authorities csv"

    private val gbStanAuthFile153Url = "https://test.co.uk/GB123456789012/SA_000000000153_csv.csv"
    private val standAuthMetadata: StandingAuthorityMetadata = StandingAuthorityMetadata(
      START_DATE_1.getYear,
      START_DATE_1.getMonthValue,
      START_DATE_1.getDayOfMonth,
      Csv,
      models.domain.FileRole.StandingAuthority
    )

    protected val gbStandingAuth1: StandingAuthorityFile = StandingAuthorityFile(
      "SA_000000000153_csv.csv", gbStanAuthFile153Url, FILE_SIZE_500, standAuthMetadata, EORI_NUMBER)

    val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
    when(mockConfig.appName).thenReturn("customs-manage-authorities-frontend")

    val mockAuditConnector: AuditConnector = mock[AuditConnector]
    when(mockAuditConnector.sendExtendedEvent(any)(any, any)).thenReturn(Future.successful(AuditResult.Success))

    val auditingService = new AuditingService(mockConfig, mockAuditConnector)
  }

}
