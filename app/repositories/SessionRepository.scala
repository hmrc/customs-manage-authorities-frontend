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
import models.UserAnswers
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions}
import org.mongodb.scala.{SingleObservableFuture, ToSingleObservablePublisher}
import play.api.Configuration
import uk.gov.hmrc.mongo.play.PlayMongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefaultSessionRepository @Inject()(
                                          mongoComponent: PlayMongoComponent,
                                          config: Configuration
                                        )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[UserAnswers](
    collectionName = "user-answers-cache",
    mongoComponent = mongoComponent,
    domainFormat = UserAnswers.format,
    indexes = Seq(
      IndexModel(
        ascending("lastUpdated"),
        IndexOptions().name("user-answers-last-updated-index")
          .unique(true)
          .expireAfter(config.get[Long]("mongodb.timeToLiveInSeconds"), TimeUnit.SECONDS)
      )
    )
  ) with SessionRepository {

  override def get(id: String): Future[Option[UserAnswers]] =
    collection.find(equal("_id", id))
      .toSingle()
      .toFutureOption()

  override def set(userAnswers: UserAnswers): Future[Boolean] =
    collection.replaceOne(
      equal("_id", userAnswers.id),
      userAnswers.copy(lastUpdated = LocalDateTime.now()),
      ReplaceOptions().upsert(true))
      .toFuture()
      .map(_.wasAcknowledged())

  override def clear(id: String): Future[Boolean] = {
    collection.deleteOne(equal("_id", id))
      .toFuture()
      .map(_.wasAcknowledged())
  }
}

trait SessionRepository {
  def get(id: String): Future[Option[UserAnswers]]
  def set(userAnswers: UserAnswers): Future[Boolean]
  def clear(id: String): Future[Boolean]
}
