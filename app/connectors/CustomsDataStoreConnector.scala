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
import javax.inject.Inject
import models.CompanyInformation
import play.api.Logger
import play.api.libs.json.{Json, OFormat}
import models.domain.XiEoriAddressInformation
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions}
import scala.concurrent.{ExecutionContext, Future}

class CustomsDataStoreConnector @Inject()(appConfig: FrontendAppConfig,
                                          httpClient: HttpClient
                                          )(implicit ec: ExecutionContext) extends HttpErrorFunctions {

  val log = Logger(this.getClass)

  def getCompanyName(eori: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val dataStoreEndpoint = appConfig.customsDataStore + s"/eori/$eori/company-information"
    httpClient.GET[CompanyInformation](dataStoreEndpoint).map(response => {
      response.consent match {
        case "1" => Some(response.name)
        case _ => None
      }}).recover { case e =>
      log.error(s"Call to data stored failed for getCompanyName exception=$e")
      None
    }
  }

  def getXiEori(eori: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val dataStoreEndpoint = appConfig.customsDataStore + s"/eori/$eori/xieori-information"
    httpClient.GET[XiEoriInformationReponse](dataStoreEndpoint).map(
      response => Some(response.xiEori))
    }.recover { case e =>
    log.error(s"Call to data stored failed for getXiEori. exception=$e")
    None
  }
}

case class XiEoriInformationReponse(xiEori: String, consent: String, address: XiEoriAddressInformation)

object XiEoriInformationReponse {
  implicit val format: OFormat[XiEoriInformationReponse] = Json.format[XiEoriInformationReponse]
}



