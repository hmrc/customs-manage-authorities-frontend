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

package base

import com.codahale.metrics.MetricRegistry
import config.FrontendAppConfig
import controllers.actions._
import models.{InternalId, UserAnswers}
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.i18n.{DefaultMessagesApi, Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.metrics.Metrics
import utils.StringUtils.emptyString

class FakeMetrics extends Metrics {
  val defaultRegistry: MetricRegistry = new MetricRegistry
}

trait SpecBase extends PlaySpec with TryValues with ScalaFutures with IntegrationPatience {

  val userAnswersId: InternalId = InternalId("id")

  def frontendAppConfig(app: Application): FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId.value)

  def messages(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(fakeRequest(emptyString, emptyString))

  def fakeRequest(method: String = emptyString,
                  path: String = emptyString): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, path).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None,
                                   requestEoriNUmber: String = emptyString): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers, requestEoriNUmber)),
        bind[Metrics].toInstance(new FakeMetrics)
      ).configure(
      "play.filters.csp.nonce.enabled" -> false
    )

  val messagesApi = new DefaultMessagesApi(
    Map("en" ->
      Map(
        "month.abbr.1 " -> "Jan",
        "month.abbr.2 " -> "Feb",
        "month.abbr.3 " -> "Mar",
        "month.abbr.4 " -> "Apr",
        "month.abbr.5 " -> "May",
        "month.abbr.6 " -> "Jun",
        "month.abbr.7 " -> "Jul",
        "month.abbr.8 " -> "Aug",
        "month.abbr.9 " -> "Sep",
        "month.abbr.10" -> "Oct",
        "month.abbr.11" -> "Nov",
        "month.abbr.12" -> "Dec"
      )
    )
  )

}
