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

package repositories

import com.mongodb.client.model.Indexes.ascending
import models.domain.CDSAccounts
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions}
import play.api.Configuration
import play.api.libs.json.{Format, Json, OFormat, Reads, Writes, __}
import uk.gov.hmrc.mongo.play.PlayMongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import org.mongodb.scala.{SingleObservableFuture, ToSingleObservablePublisher}

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
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
          .expireAfter(config.get[Long]("mongodb.timeToLiveInSeconds"), TimeUnit.SECONDS)
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

case class AccountsRepositoryCacheEntry(_id: String,
                                        data: CDSAccounts,
                                        lastUpdated: LocalDateTime)

trait MongoJavatimeFormats {
  outer =>

  final val localDateTimeReads: Reads[LocalDateTime] =
    Reads.at[String](__ \ "$date" \ "$numberLong")
      .map(dateTime => Instant.ofEpochMilli(dateTime.toLong).atZone(ZoneOffset.UTC).toLocalDateTime)

  final val localDateTimeWrites: Writes[LocalDateTime] =
    Writes.at[String](__ \ "$date" \ "$numberLong")
      .contramap(_.toInstant(ZoneOffset.UTC).toEpochMilli.toString)

  final val localDateTimeFormat: Format[LocalDateTime] =
    Format(localDateTimeReads, localDateTimeWrites)

  trait Implicits {
    implicit val jatLocalDateTimeFormat: Format[LocalDateTime] = outer.localDateTimeFormat
  }

  object Implicits extends Implicits
}

object MongoJavatimeFormats extends MongoJavatimeFormats

object AccountsRepositoryCacheEntry {
  implicit val lastUpdatedReads: Reads[LocalDateTime] = MongoJavatimeFormats.localDateTimeReads
  implicit val lastUpdatedWrites: Writes[LocalDateTime] = MongoJavatimeFormats.localDateTimeWrites
  implicit val format: OFormat[AccountsRepositoryCacheEntry] = Json.format[AccountsRepositoryCacheEntry]
}
