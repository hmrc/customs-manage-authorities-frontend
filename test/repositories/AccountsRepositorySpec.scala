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

package repositories

import base.SpecBase
import models.domain.{AccountStatusOpen, CDSAccounts, CDSCashBalance}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mongodb.scala.MongoCollection
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration, inject}
import uk.gov.hmrc.mongo.play.PlayMongoComponent
import uk.gov.hmrc.mongo.play.json.{CollectionFactory, PlayMongoRepository}

import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.concurrent.{ExecutionContext, Future}

class AccountsRepositorySpec extends SpecBase {
"set" must {
  "replace the document in the collection as per provided input" in new SetUp {

    val service = new AccountsRepository(mockPlayMongoComponent, appConfig)

    service.set("1", cdsAccounts) shouldBe Future.successful(true)
  }
}

  trait SetUp {
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
    val cdsAccounts: CDSAccounts = CDSAccounts("GB123456789012",
      List(models.domain.CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))))

    val app: Application = new GuiceApplicationBuilder().overrides(
      //inject.bind[PlayMongoComponent].toInstance(mockPlayMongoComponent),
      inject.bind[MongoCollection[AccountsRepositoryCacheEntry]].toInstance(mockCollection),
      inject.bind[PlayMongoRepository[AccountsRepositoryCacheEntry]].toInstance(mockPlayMongoRepo)
    ).build()

    val appConfig: Configuration = app.injector.instanceOf[Configuration]
    val mockPlayMongoComponent: PlayMongoComponent = mock[PlayMongoComponent]
    val mockPlayMongoRepo = mock[PlayMongoRepository[AccountsRepositoryCacheEntry]]
    val mockCollection = mock[MongoCollection[AccountsRepositoryCacheEntry]]
    val mockCollectionFactory = mock[CollectionFactory]

    when(mockPlayMongoComponent.initTimeout).thenReturn(FiniteDuration(12L,SECONDS))
    when(mockCollectionFactory.collection(any(),any(),any(),any())).thenReturn(mockCollection)
    when(mockPlayMongoRepo.collection).thenReturn(mockCollection)
  }
}
