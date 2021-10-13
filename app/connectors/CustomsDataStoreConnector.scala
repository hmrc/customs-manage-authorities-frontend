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

package connectors

import config.FrontendAppConfig
import models.CompanyInformation
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsDataStoreConnector @Inject()(httpClient: HttpClient,
                                          frontendAppConfig: FrontendAppConfig)
                                         (implicit executionContext: ExecutionContext) {

  //TODO ensure datastore checks whether user has authorised to display information
  def getCompanyInformation(eori: String)(implicit hc: HeaderCarrier): Future[Option[CompanyInformation]] = {
    val dataStoreEndpoint = frontendAppConfig.customsDataStore + s"/eori/$eori/company-information"
    httpClient.GET[CompanyInformation](dataStoreEndpoint).map(Some(_)).recover {
      case _ => None
    }
  }
}
