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

import models.requests.AddAuthorityRequest

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

  def addAuthRequestList(payload: AddAuthorityRequest): List[AddAuthorityRequest] = {
    val accounts = payload.accounts

    if (accounts.hasOnlyDutyDefermentsAccount) {
      List(payload)
    } else {
      val ddAccountAuthReq = payload.copy(accounts = accounts.copy(cash = None, guarantee = None))
      val cashAndGuaranteeAccountAuthRequest = payload.copy(accounts = accounts.copy(dutyDeferments = Seq()))

      List(ddAccountAuthReq, cashAndGuaranteeAccountAuthRequest)
    }
  }
}
