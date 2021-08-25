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

package repositories

import com.mongodb.client.model.Indexes.ascending
import models.domain.CDSAccounts
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions}
import play.api.Configuration
import play.api.libs.json.{Json, OFormat, Reads, Writes}
import uk.gov.hmrc.mongo.play.PlayMongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountsRepository @Inject()(
                                    val mongoComponent: PlayMongoComponent,
                                    val config: Configuration
                                  )(implicit val ec: ExecutionContext)
  extends PlayMongoRepository[AccountsRepositoryCacheEntry](
    collectionName = "accounts-cache",
    mongoComponent = mongoComponent,
    domainFormat = AccountsRepositoryCacheEntry.format,
    indexes = Seq(
      IndexModel(
        ascending("lastUpdated"),
        IndexOptions()
          .name("accounts-last-updated-index")
          .unique(true)
          .expireAfter(config.get[Int]("mongodb.timeToLiveInSeconds"), TimeUnit.SECONDS)
      )
    )
  ) {
  def get(id: String): Future[Option[CDSAccounts]] =
    collection
      .find(equal("_id", id))
      .toSingle()
      .toFutureOption()
      .map(_.map(_.data))

  def set(id: String, accounts: CDSAccounts): Future[Boolean] =
    collection
      .replaceOne(
        equal("_id", id),
        AccountsRepositoryCacheEntry(id, accounts, LocalDateTime.now()),
        ReplaceOptions().upsert(true))
      .toFuture()
      .map(_.wasAcknowledged())

  def clear(id: String): Future[Boolean] =
    collection
      .deleteOne(equal("_id", id))
      .toFuture()
      .map(_.wasAcknowledged())
}

case class AccountsRepositoryCacheEntry(_id: String, data: CDSAccounts, lastUpdated: LocalDateTime)

object AccountsRepositoryCacheEntry {
  implicit val lastUpdatedReads: Reads[LocalDateTime] = MongoJavatimeFormats.localDateTimeReads
  implicit val lastUpdatedWrites: Writes[LocalDateTime] = MongoJavatimeFormats.localDateTimeWrites
  implicit val format: OFormat[AccountsRepositoryCacheEntry] = Json.format[AccountsRepositoryCacheEntry]
}
