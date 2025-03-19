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

package utils

import connectors.CustomsDataStoreConnector
import utils.Constants.NON_EU_EORI_PREFIXES
import models.requests.IdentifierRequest
import play.api.mvc.AnyContent
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

  object Utils {

    def getXiEori(dataStoreConnector: CustomsDataStoreConnector)
                 (implicit request: IdentifierRequest[AnyContent],
                  ec: ExecutionContext,
                  hc: HeaderCarrier): Future[Option[String]] = {

      if (NON_EU_EORI_PREFIXES.exists(prefix => request.eoriNumber.startsWith(prefix))) {
        dataStoreConnector.getXiEori
      } else {
        Future.successful(None)
      }
    }
}
