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

import config.FrontendAppConfig
import javax.inject.Inject
import play.api.http.Status
import play.api.mvc.RequestHeader
import play.api.{Logger, Logging}
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.partials.{HeaderCarrierForPartialsConverter, HtmlPartial}

import scala.concurrent.{ExecutionContext, Future}

class SecureMessageConnector @Inject() (
  httpClient: HttpClientV2,
  appConfig: FrontendAppConfig,
  headerCarrierForPartialsConverter: HeaderCarrierForPartialsConverter
)(implicit ec: ExecutionContext)
    extends Logging
    with Status {

  private val log = Logger(this.getClass)

  def getMessageCountBanner(returnToUrl: String)(implicit request: RequestHeader): Future[Option[HtmlPartial]] = {
    implicit val hc: HeaderCarrier = headerCarrierForPartialsConverter.fromRequestWithEncryptedCookie(request)

    val url = s"${appConfig.customsSecureMessagingBannerEndpoint}?return_to=$returnToUrl"

    httpClient
      .get(url"$url")
      .execute[HtmlPartial]
      .map {
        case success @ HtmlPartial.Success(_, _) => Some(success)
        case HtmlPartial.Failure(_, _)           => None
      }
      .recover { case exc =>
        log.error(s"Problem loading message banner partial: ${exc.getMessage}")
        None
      }
  }
}
