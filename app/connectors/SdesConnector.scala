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

package connectors

import config.FrontendAppConfig
import config.Headers.{X_CLIENT_ID, X_SDES_KEY}
import models.domain.FileFormat.{authorityFileFormats, filterFileFormats}
import models.domain.FileRole.StandingAuthority
import models.domain.{EORI, FileInformation, SdesFile, StandingAuthorityFile}
import services.{AuditingService, MetricsReporterService, SdesGatekeeperService}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SdesConnector @Inject()(httpClient: HttpClient,
                              appConfig: FrontendAppConfig,
                              metricsReporterService: MetricsReporterService,
                              sdesGatekeeperService: SdesGatekeeperService,
                              auditingService: AuditingService
                             )(implicit executionContext: ExecutionContext) {

  import sdesGatekeeperService._

  def getAuthoritiesCsvFiles(eori: EORI)(implicit hc: HeaderCarrier): Future[Seq[StandingAuthorityFile]] = {
    val transform = convertTo[StandingAuthorityFile] andThen filterFileFormats(authorityFileFormats)

    getSdesFiles[FileInformation, StandingAuthorityFile](
      appConfig.filesUrl(StandingAuthority),
      eori,
      "sdes.get.csv-statement",
      transform
    )
  }

  private def getSdesFiles[A, B <: SdesFile](url: String,
                                             eori: EORI,
                                             metricsName: String,
                                             transform: Seq[A] => Seq[B])
                                    (implicit reads: HttpReads[HttpResponse],
                                     readSeq: HttpReads[Seq[A]],
                                     hc: HeaderCarrier): Future[Seq[B]] = {

    metricsReporterService.withResponseTimeLogging(metricsName) {
      httpClient.GET[HttpResponse](url, headers = Seq(X_CLIENT_ID -> appConfig.xClientIdHeader, X_SDES_KEY -> eori))(reads, HeaderCarrier(), implicitly)
        .map(readSeq.read("GET", url, _))
        .map(transform)
        .map { files =>
          auditingService.auditFiles(files, eori)
          files
        }
    }
  }
}