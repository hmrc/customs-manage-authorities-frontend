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

import connectors.CustomsDataStoreConnector
import models.InternalId
import models.domain.EORI
import repositories.AuthorisedEoriAndCompanyInfoRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.StringUtils.emptyString

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedEoriAndCompanyInfoService @Inject() (
  repository: AuthorisedEoriAndCompanyInfoRepository,
  dataStoreConnector: CustomsDataStoreConnector
)(implicit executionContext: ExecutionContext) {

  def retrieveAuthEorisAndCompanyInfoForId(internalId: InternalId): Future[Option[Map[String, String]]] =
    repository.get(internalId.value)

  def retrieveAuthorisedEoriAndCompanyInfo(
    internalId: InternalId,
    eoris: Set[EORI]
  )(implicit hc: HeaderCarrier): Future[Option[Map[String, String]]] = {

    lazy val eoriAndCompanyMap = for {
      eoriSeq: Seq[Option[EORI]] <-
        Future.sequence(eoris.toSeq.map(dataStoreConnector.retrieveCompanyInformationThirdParty(_)))
    } yield eoris.zip(eoriSeq).toMap.map(keyValue => (keyValue._1, keyValue._2.getOrElse(emptyString)))

    repository.get(internalId.value).flatMap {
      case Some(data) => Future.successful(Some(data))
      case _          =>
        for {
          dataMap <- eoriAndCompanyMap
          _       <- repository.set(internalId.value, dataMap)
        } yield Some(dataMap)
    }
  }

  def storeAuthEorisAndCompanyInfo(internalId: InternalId, data: Map[String, String]): Future[Boolean] =
    repository.set(internalId.value, data)
}
