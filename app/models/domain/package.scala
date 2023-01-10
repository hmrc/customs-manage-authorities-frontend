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

package models

import play.api.mvc.PathBindable

package object domain {
  type EORI = String
  type AccountNumber = String
  type LinkId = String
  type GAN = String
  type CAN = String

  val lengthToReveal = 4

  implicit def optionBindable: PathBindable[Option[LinkId]] = new PathBindable[Option[LinkId]] {
    def bind(key: String, value: String): Either[String, Option[LinkId]] =
      implicitly[PathBindable[LinkId]].
        bind(key, value).
        fold(
          left => Left(left),
          right => Right(Some(right))
        )

    def unbind(key: String, value: Option[LinkId]): String = value map identity
  }
}
