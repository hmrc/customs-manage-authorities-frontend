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

package connectors

import base.SpecBase
import config.FrontendAppConfig
import models.domain.FileFormat.Csv
import models.domain.FileRole.StandingAuthority
import models.domain.{FileInformation, Metadata, MetadataItem, StandingAuthorityFile, StandingAuthorityMetadata}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{spy, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import play.api.libs.json.{Json, Writes}
import play.api.test.Helpers.*
import play.api.{Application, inject}
import services.{AuditingService, MetricsReporterService, SdesGatekeeperService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import utils.StringUtils.emptyString
import utils.TestData._
import java.net.URL

import scala.concurrent.Future

class SdesConnectorSpec extends SpecBase {

  "getAuthoritiesCsvFiles" should {
    "make a GET request to sdesStandingAuthorityFilesUrl" in new Setup {
      val url: String = sdesStandingAuthorityFileUrl

      when(mockRequestBuilder.setHeader(any[(String, String)].asInstanceOf[Seq[(String, String)]]: _*))
        .thenReturn(mockRequestBuilder)

      when(mockRequestBuilder.execute[Seq[FileInformation]](any, any))
        .thenReturn(Future.successful(Seq.empty))

      when(mockHttpClientV2.get(eqTo(new URL(url)))(any)).thenReturn(mockRequestBuilder)

      await(sdesService.getAuthoritiesCsvFiles(someEori)(hc))

      verify(mockHttpClientV2).get(eqTo(new URL(url)))(any)
      verify(mockRequestBuilder).setHeader(any[(String, String)].asInstanceOf[Seq[(String, String)]]: _*)
      verify(mockRequestBuilder).execute[Seq[FileInformation]](any, any)
    }

    "converts Sdes response to List[StandingAuthorityFile]" in new Setup {
      val url: String             = sdesStandingAuthorityFileUrl
      val numberOfStatements: Int = standingAuthoritiesFilesSdesResponse.length

      when(mockRequestBuilder.setHeader(any[(String, String)].asInstanceOf[Seq[(String, String)]]: _*))
        .thenReturn(mockRequestBuilder)

      when(mockRequestBuilder.execute[Seq[FileInformation]](any, any))
        .thenReturn(Future.successful(standingAuthoritiesFilesSdesResponse))

      when(mockHttpClientV2.get(eqTo(new URL(url)))(any)).thenReturn(mockRequestBuilder)

      when(sdesGatekeeperServiceSpy.convertTo(any())).thenCallRealMethod()

      val appWithGateKeeper: Application = applicationBuilder()
        .overrides(
          inject.bind[HttpClientV2].toInstance(mockHttpClientV2),
          inject.bind[SdesGatekeeperService].toInstance(sdesGatekeeperServiceSpy)
        )
        .build()

      val service: SdesConnector = appWithGateKeeper.injector.instanceOf[SdesConnector]

      await(service.getAuthoritiesCsvFiles(someEori)(hc))
      verify(mockHttpClientV2).get(eqTo(new URL(url)))(any)
      verify(mockRequestBuilder).setHeader(any[(String, String)].asInstanceOf[Seq[(String, String)]]: _*)
      verify(mockRequestBuilder).execute[Seq[FileInformation]](any, any)
      verify(sdesGatekeeperServiceSpy, times(numberOfStatements)).convertToStandingAuthoritiesFile(any)
    }

    "filter out unknown file types" in new Setup {
      val url: String = sdesStandingAuthorityFileUrl

      when(mockRequestBuilder.setHeader(any[(String, String)].asInstanceOf[Seq[(String, String)]]: _*))
        .thenReturn(mockRequestBuilder)

      when(mockRequestBuilder.execute[Seq[FileInformation]](any, any))
        .thenReturn(Future.successful(standingAuthoritiesFilesWithUnknownFiletypesSdesResponse))

      when(mockHttpClientV2.get(eqTo(new URL(url)))(any)).thenReturn(mockRequestBuilder)

      val result: Seq[StandingAuthorityFile] =
        await(sdesService.getAuthoritiesCsvFiles(someEoriWithUnknownFileTypes)(hc))

      result mustBe standingAuthorityFiles
      verify(mockHttpClientV2).get(eqTo(new URL(url)))(any)
      verify(mockRequestBuilder).setHeader(any[(String, String)].asInstanceOf[Seq[(String, String)]]: _*)
      verify(mockRequestBuilder).execute[Seq[FileInformation]](any, any)
    }
  }

  trait Setup {
    val hc: HeaderCarrier                                                 = HeaderCarrier()
    implicit val messages: Messages                                       = stubMessages()
    implicit val fileInformationListWrites: Writes[List[FileInformation]] = Writes.list(Json.writes[FileInformation])
    val someEori                                                          = "12345678"
    val someEoriWithUnknownFileTypes                                      = "EoriFooBar"
    val xClientId                                                         = "TheClientId"
    val xClientIdHeader                                                   = "x-client-id"
    val xSDESKey                                                          = "X-SDES-Key"

    val sdesStandingAuthorityFileUrl =
      "http://localhost:9754/customs-financials-sdes-stub/files-available/list/StandingAuthority"

    val standingAuthorityFiles: List[StandingAuthorityFile] = List(
      StandingAuthorityFile(
        "name_01",
        "download_url_01",
        FILE_SIZE_111,
        StandingAuthorityMetadata(YEAR_2022, MONTH_6, DAY_1, Csv, StandingAuthority),
        emptyString
      ),
      StandingAuthorityFile(
        "name_02",
        "download_url_02",
        FILE_SIZE_115,
        StandingAuthorityMetadata(YEAR_2022, MONTH_5, DAY_25, Csv, StandingAuthority),
        emptyString
      )
    )

    val standingAuthoritiesFilesSdesResponse: Seq[FileInformation] = Seq(
      FileInformation(
        "name_01",
        "download_url_01",
        FILE_SIZE_111,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2022"),
            MetadataItem("PeriodStartMonth", "6"),
            MetadataItem("PeriodStartDay", "1"),
            MetadataItem("FileType", "csv"),
            MetadataItem("FileRole", "StandingAuthority")
          )
        )
      ),
      FileInformation(
        "name_02",
        "download_url_02",
        FILE_SIZE_115,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2022"),
            MetadataItem("PeriodStartMonth", "5"),
            MetadataItem("PeriodStartDay", "25"),
            MetadataItem("FileType", "csv"),
            MetadataItem("FileRole", "StandingAuthority")
          )
        )
      )
    )

    val standingAuthoritiesFilesWithUnknownFiletypesSdesResponse: Seq[FileInformation] = Seq(
      FileInformation(
        "name_01",
        "download_url_01",
        FILE_SIZE_111,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2022"),
            MetadataItem("PeriodStartMonth", "6"),
            MetadataItem("PeriodStartDay", "1"),
            MetadataItem("FileType", "csv"),
            MetadataItem("FileRole", "StandingAuthority")
          )
        )
      ),
      FileInformation(
        "name_02",
        "download_url_02",
        FILE_SIZE_115,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2022"),
            MetadataItem("PeriodStartMonth", "5"),
            MetadataItem("PeriodStartDay", "25"),
            MetadataItem("FileType", "csv"),
            MetadataItem("FileRole", "StandingAuthority")
          )
        )
      ),
      FileInformation(
        "name_03",
        "download_url_03",
        FILE_SIZE_115,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2022"),
            MetadataItem("PeriodStartMonth", "4"),
            MetadataItem("PeriodStartDay", "25"),
            MetadataItem("FileType", "pdf"),
            MetadataItem("FileRole", "StandingAuthority")
          )
        )
      )
    )

    val sdesGatekeeperServiceSpy: SdesGatekeeperService    = spy(new SdesGatekeeperService())
    val mockHttpClientV2: HttpClientV2                     = mock[HttpClientV2]
    val mockRequestBuilder: RequestBuilder                 = mock[RequestBuilder]
    val mockAppConfig: FrontendAppConfig                   = mock[FrontendAppConfig]
    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]
    val mockAuditingService: AuditingService               = mock[AuditingService]

    val app: Application = applicationBuilder()
      .overrides(
        inject.bind[HttpClientV2].toInstance(mockHttpClientV2)
      )
      .build()

    val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]
  }
}
