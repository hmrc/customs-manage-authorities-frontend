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

import models.requests.{AddAuthorityRequest, GrantAccountAuthorityRequest}

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

package object controllers {

  def grantAccountAuthRequestList(payload: AddAuthorityRequest,
                                  xiEori: String,
                                  gbEori: String): List[GrantAccountAuthorityRequest] = {
    val accounts = payload.accounts

    accounts match {
      case accn if accn.hasOnlyDutyDefermentsAccount => List(GrantAccountAuthorityRequest(payload, xiEori))
      case accn if accn.isDutyDefermentsAccountEmpty => List(GrantAccountAuthorityRequest(payload, gbEori))
      case _ =>
        val ddAccountAuthReq = payload.copy(accounts = accounts.copy(cash = None, guarantee = None))
        val cashAndGuaranteeAccountAuthRequest = payload.copy(accounts = accounts.copy(dutyDeferments = Seq()))

        List(GrantAccountAuthorityRequest(ddAccountAuthReq, xiEori),
          GrantAccountAuthorityRequest(cashAndGuaranteeAccountAuthRequest, gbEori))
    }
  }
}
