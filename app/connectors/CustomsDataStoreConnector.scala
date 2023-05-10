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
import models.{CompanyInformation, XiEoriInformationResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions}
import scala.concurrent.{ExecutionContext, Future}

class CustomsDataStoreConnector @Inject()(appConfig: FrontendAppConfig,
                                          httpClient: HttpClient
                                          )(implicit ec: ExecutionContext) extends HttpErrorFunctions {


  def getCompanyName(eori: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val dataStoreEndpoint = appConfig.customsDataStore + s"/eori/$eori/company-information"
    httpClient.GET[CompanyInformation](dataStoreEndpoint).map(response => {
      response.consent match {
        case "1" => Some(response.name)
        case _ => None
      }
    }).recover { case e =>
      None
    }
  }

  def getXiEori(eori: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val dataStoreEndpoint = appConfig.customsDataStore + s"/eori/$eori/xieori-information"
    httpClient.GET[XiEoriInformationResponse](dataStoreEndpoint).map(
      response => Some(response.xiEori)
    ).recover { case e =>
      None
    }
  }
}
