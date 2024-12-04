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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import connectors.{CustomsDataStoreConnector, CustomsFinancialsConnector, SdesConnector, SecureMessageConnector}
import models.domain.FileFormat.Csv
import models.domain._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.Helpers._
import repositories.{AccountsRepository, AuthorisedEoriAndCompanyInfoRepository, AuthoritiesRepository}
import services.{AccountsCacheService, AuthorisedEoriAndCompanyInfoService, AuthoritiesCacheService}
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.DateUtils
import utils.TestData._
import viewmodels.{AuthoritiesFilesNotificationViewModel, ManageAuthoritiesViewModel}
import views.html.{ManageAuthoritiesApiFailureView, ManageAuthoritiesView, NoAccountsView}

import java.time.LocalDate
import scala.concurrent.Future

class ManageAuthoritiesControllerSpec extends SpecBase with MockitoSugar with DateUtils {

  "ManageAuthorities Controller" when {

    "onPageLoad" should {

      "call deleteNotification on the customsFinancialsConnector" in new Setup {
        private val mockCustomsFinancialsConnector = mock[CustomsFinancialsConnector]

        private val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[CustomsFinancialsConnector].toInstance(mockCustomsFinancialsConnector)
          )
          .build()

        running(application) {
          val request = fakeRequest(GET, manageAuthoritiesRoute)
          await(route(application, request).value)

          verify(mockCustomsFinancialsConnector).deleteNotification(any, any)(any)
        }
      }
    }

    "API call succeeds" must {

      "return OK and the correct view if no accounts associated with a EORI" in new Setup {
        val accounts: CDSAccounts = CDSAccounts("GB123456789012", List())

        val mockRepository: AuthoritiesRepository = mock[AuthoritiesRepository]
        val mockAccountsCacheService: AccountsCacheService = mock[AccountsCacheService]
        val mockAuthCacheService: AuthoritiesCacheService = mock[AuthoritiesCacheService]
        val mockSecureMessageConnector: SecureMessageConnector = mock[SecureMessageConnector]
        val mockSdesConnector: SdesConnector = mock[SdesConnector]

        when(mockAuthCacheService.retrieveAuthoritiesForId(any)).thenReturn(Future.successful(None))
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockAccountsCacheService.retrieveAccountsForId(any)).thenReturn(Future.successful(None))
        when(mockAccountsCacheService.retrieveAccounts(any(), any())(any())).thenReturn(Future.successful(accounts))
        when(mockSecureMessageConnector.getMessageCountBanner(any)(any)).thenReturn(Future.successful(None))
        when(mockSdesConnector.getAuthoritiesCsvFiles(any())(any())).thenReturn(Future.successful(authCsvFiles))

        private val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockRepository),
            bind[AccountsCacheService].toInstance(mockAccountsCacheService),
            bind[AuthoritiesCacheService].toInstance(mockAuthCacheService),
            bind[SecureMessageConnector].toInstance(mockSecureMessageConnector),
            bind[SdesConnector].toInstance(mockSdesConnector)
          ).configure("features.edit-journey" -> true)
          .build()

        running(application) {

          val request = fakeRequest(GET, manageAuthoritiesRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[NoAccountsView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(filesNotificationViewModel(application))(request, messages(application), appConfig).toString

          verify(mockSecureMessageConnector).getMessageCountBanner(any)(any)
        }
      }

      "return OK and the correct view" in new Setup {
        val accounts: CDSAccounts = CDSAccounts("GB123456789012", List(
          CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))),
          CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
        ))

        val eoriAndCompanyInfoMap: Map[String, String] = Map(eori1 -> COMPANY_NAME)

        val mockRepository: AuthoritiesRepository = mock[AuthoritiesRepository]
        val mockAccountsCacheService: AccountsCacheService = mock[AccountsCacheService]
        val mockAuthCacheService: AuthoritiesCacheService = mock[AuthoritiesCacheService]
        val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]
        val mockAuthEoriAndCompanyInfoService: AuthorisedEoriAndCompanyInfoService =
          mock[AuthorisedEoriAndCompanyInfoService]
        val mockSecureMessageConnector: SecureMessageConnector = mock[SecureMessageConnector]
        val mockSdesConnector: SdesConnector = mock[SdesConnector]

        when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(XI_EORI)))
        when(mockDataStoreConnector.getEmail(any)(any)).thenReturn(Future.successful(Right(testEmail)))
        when(mockDataStoreConnector.getCompanyName(any)(any)).thenReturn(Future.successful(Some(COMPANY_NAME)))

        when(mockAuthCacheService.retrieveAuthoritiesForId(any)).thenReturn(Future.successful(None))
        when(mockAccountsCacheService.retrieveAccountsForId(any)).thenReturn(Future.successful(None))
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(Future.successful(authoritiesWithId))
        when(mockAccountsCacheService.retrieveAccounts(any(), any())(any())).thenReturn(Future.successful(accounts))

        when(mockAuthEoriAndCompanyInfoService.retrieveAuthorisedEoriAndCompanyInfo(any, any)(any))
          .thenReturn(Future.successful(Some(eoriAndCompanyInfoMap)))
        when(mockSecureMessageConnector.getMessageCountBanner(any)(any)).thenReturn(Future.successful(None))
        when(mockSdesConnector.getAuthoritiesCsvFiles(any())(any())).thenReturn(Future.successful(authCsvFiles))

        private val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockRepository),
            bind[AccountsCacheService].toInstance(mockAccountsCacheService),
            bind[AuthoritiesCacheService].toInstance(mockAuthCacheService),
            bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            bind[AuthorisedEoriAndCompanyInfoService].toInstance(mockAuthEoriAndCompanyInfoService),
            bind[SecureMessageConnector].toInstance(mockSecureMessageConnector),
            bind[SdesConnector].toInstance(mockSdesConnector)
          ).configure("features.edit-journey" -> true)
          .build()

        running(application) {

          val request = fakeRequest(GET, manageAuthoritiesRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[ManageAuthoritiesView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(ManageAuthoritiesViewModel(
              authoritiesWithId, accounts, eoriAndCompanyInfoMap, filesNotificationViewModel(application)),
              maybeMessageBannerPartial = None)(request, messages(application), appConfig).toString

          verify(mockSecureMessageConnector).getMessageCountBanner(any)(any)
        }
      }

      "return OK and the correct view when authorities and account data  is found in cache" in new Setup {
        val accounts: CDSAccounts = CDSAccounts("GB123456789012", List(
          CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))),
          CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
        ))

        val eoriAndCompanyInfoMap: Map[String, String] = Map(eori1 -> COMPANY_NAME)

        val mockAuthRepository: AuthoritiesRepository = mock[AuthoritiesRepository]
        val mockAccountsRepository: AccountsRepository = mock[AccountsRepository]
        val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]
        val mockAuthEoriAndCompanyService: AuthorisedEoriAndCompanyInfoService = mock[AuthorisedEoriAndCompanyInfoService]
        val mockSdesConnector: SdesConnector = mock[SdesConnector]

        when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(XI_EORI)))
        when(mockDataStoreConnector.getEmail(any)(any)).thenReturn(Future.successful(Right(testEmail)))
        when(mockDataStoreConnector.getCompanyName(any)(any)).thenReturn(Future.successful(Some(COMPANY_NAME)))
        when(mockAccountsRepository.get(any())).thenReturn(Future.successful(Some(accounts)))
        when(mockAuthRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))

        when(mockAuthEoriAndCompanyService.retrieveAuthorisedEoriAndCompanyInfo(any, any)(any))
          .thenReturn(Future.successful(Some(eoriAndCompanyInfoMap)))
        when(mockSdesConnector.getAuthoritiesCsvFiles(any())(any())).thenReturn(Future.successful(authCsvFiles))

        private val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockAuthRepository),
            bind[AccountsRepository].toInstance(mockAccountsRepository),
            bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            bind[AuthorisedEoriAndCompanyInfoService].toInstance(mockAuthEoriAndCompanyService),
            bind[SdesConnector].toInstance(mockSdesConnector)
          ).configure("features.edit-journey" -> true)
          .build()

        running(application) {

          val request = fakeRequest(GET, manageAuthoritiesRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[ManageAuthoritiesView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(ManageAuthoritiesViewModel(
              authoritiesWithId, accounts, eoriAndCompanyInfoMap, filesNotificationViewModel(application)),
              maybeMessageBannerPartial = None)(request, messages(application), appConfig).toString
        }
      }

      "return OK and the correct view when no authority accounts are found" in new Setup {
        val accounts = CDSAccounts("GB123456789012", List(
          CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))),
          CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
        ))

        val mockRepository = mock[AuthoritiesRepository]
        val mockAccountsCacheService = mock[AccountsCacheService]
        val emptyMap: Map[String, AccountWithAuthoritiesWithId] = Map()
        val emptyAuthoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(emptyMap)
        val mockAuthCacheService = mock[AuthoritiesCacheService]
        val mockSdesConnector: SdesConnector = mock[SdesConnector]

        when(mockAuthCacheService.retrieveAuthoritiesForId(any)).thenReturn(Future.successful(None))
        when(mockAccountsCacheService.retrieveAccountsForId(any)).thenReturn(Future.successful(None))

        when(mockAuthCacheService.retrieveAuthorities(any, any)(any))
          .thenReturn(Future.successful(emptyAuthoritiesWithId))

        when(mockRepository.get(any())).thenReturn(Future.successful(Some(emptyAuthoritiesWithId)))
        when(mockAccountsCacheService.retrieveAccounts(any(), any())(any())).thenReturn(Future.successful(accounts))
        when(mockSdesConnector.getAuthoritiesCsvFiles(any())(any())).thenReturn(Future.successful(authCsvFiles))

        private val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AuthoritiesRepository].toInstance(mockRepository),
            bind[AccountsCacheService].toInstance(mockAccountsCacheService),
            bind[AuthoritiesCacheService].toInstance(mockAuthCacheService),
            bind[SdesConnector].toInstance(mockSdesConnector)
          ).configure("features.edit-journey" -> true)
          .build()

        running(application) {

          val request = fakeRequest(GET, manageAuthoritiesRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[ManageAuthoritiesView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(ManageAuthoritiesViewModel(
              authorities = emptyAuthoritiesWithId,
              accounts = accounts,
              filesNotificationViewModel = filesNotificationViewModel(application)),
              maybeMessageBannerPartial = None)(
              request, messages(application), appConfig).toString
        }
      }
    }

    "API call fails" must {

      "redirect to 'unavailable' page" in new Setup {
        val mockRepository = mock[AuthoritiesRepository]
        when(mockRepository.get(any())).thenReturn(Future.successful(None))

        val failingConnector = mock[CustomsFinancialsConnector]

        private val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[CustomsFinancialsConnector].toInstance(failingConnector),
            bind[AuthoritiesRepository].toInstance(mockRepository)

          ).build()

        running(application) {

          val request = fakeRequest(GET, manageAuthoritiesRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual manageAuthoritiesUnavailableRoute
        }
      }

      "serve unavailable page on a separate route" in new Setup {
        val mockSdesConnector: SdesConnector = mock[SdesConnector]

        when(mockSdesConnector.getAuthoritiesCsvFiles(any())(any())).thenReturn(Future.successful(authCsvFiles))

        private val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
          bind[SdesConnector].toInstance(mockSdesConnector)
        ).build()

        running(application) {

          val request = fakeRequest(GET, manageAuthoritiesUnavailableRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[ManageAuthoritiesApiFailureView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(filesNotificationViewModel(application))(request, messages(application), appConfig).toString
        }
      }
    }

    "API call fails due to GBN EORI Json Validation" must {

      "redirect to 'account unavailable' page" in new Setup {
        val statusCode = 500

        val mockAccountsCacheService: AccountsCacheService = mock[AccountsCacheService]
        val mockAuthCacheService: AuthoritiesCacheService = mock[AuthoritiesCacheService]

        when(mockAuthCacheService.retrieveAuthoritiesForId(any)).thenReturn(Future.successful(None))
        when(mockAccountsCacheService.retrieveAccountsForId(any)).thenReturn(Future.successful(None))

        when(mockAccountsCacheService.retrieveAccounts(any(), any())(any()))
          .thenReturn(Future.failed(UpstreamErrorResponse("JSON Validation Error", statusCode)))

        private val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AccountsCacheService].toInstance(mockAccountsCacheService)
          ).build()

        running(application) {
          val request = fakeRequest(GET, manageAuthoritiesRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual manageAuthoritiesGBNValidationRoute
        }
      }
    }
  }

  "fetchAuthoritiesOnMIDVAHomePageLoad" should {

    "return OK when authorities are retrieved successfully" in new Setup {
      val accounts: CDSAccounts = CDSAccounts("GB123456789012", List(
        CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))),
        CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
      ))

      val mockRepository: AuthoritiesRepository = mock[AuthoritiesRepository]
      val mockAccountsCacheService: AccountsCacheService = mock[AccountsCacheService]
      val mockAuthoritiesCacheService: AuthoritiesCacheService = mock[AuthoritiesCacheService]
      val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]
      val mockAuthAndCompanyInfoService: AuthorisedEoriAndCompanyInfoService = mock[AuthorisedEoriAndCompanyInfoService]
      val mockAuthAndCompanyRepo: AuthorisedEoriAndCompanyInfoRepository = mock[AuthorisedEoriAndCompanyInfoRepository]

      when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId02)))

      when(mockAccountsCacheService.retrieveAccounts(any(), any())(any()))
        .thenReturn(Future.successful(accounts))

      when(mockAuthoritiesCacheService.retrieveAuthorities(any(), any())(any()))
        .thenReturn(Future.successful(authoritiesWithId))

      when(mockDataStoreConnector.getEmail(any())(any())).thenReturn(Future.successful(Right(testEmail)))
      when(mockDataStoreConnector.getXiEori(any())(any())).thenReturn(Future.successful(Some(XI_EORI)))
      when(mockAuthAndCompanyRepo.get(any)).thenReturn(Future.successful(None))
      when(mockAuthAndCompanyInfoService.retrieveAuthorisedEoriAndCompanyInfo(any, any)(any))
        .thenReturn(Future.successful(Some(eoriAndCompanyMap)))

      private val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AuthoritiesRepository].toInstance(mockRepository),
          bind[AccountsCacheService].toInstance(mockAccountsCacheService),
          bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
          bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
          bind[AuthorisedEoriAndCompanyInfoRepository].toInstance(mockAuthAndCompanyRepo)
        ).configure("features.edit-journey" -> true)
        .build()

      running(application) {

        val request = fakeRequest(GET, fetchAllAuthoritiesRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        verify(mockDataStoreConnector, times(1)).getCompanyName(any())(any())
      }
    }

    "return OK when authorities are retrieved successfully and save company info in the cache" in new Setup {
      val accounts: CDSAccounts = CDSAccounts("GB123456789012", List(
        CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))),
        CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
      ))

      val mockRepository: AuthoritiesRepository = mock[AuthoritiesRepository]
      val mockAccountsCacheService: AccountsCacheService = mock[AccountsCacheService]
      val mockAuthoritiesCacheService: AuthoritiesCacheService = mock[AuthoritiesCacheService]
      val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]
      val mockAuthAndCompanyRepo: AuthorisedEoriAndCompanyInfoRepository = mock[AuthorisedEoriAndCompanyInfoRepository]

      when(mockAccountsCacheService.retrieveAccounts(any(), any())(any()))
        .thenReturn(Future.successful(accounts))

      when(mockAuthoritiesCacheService.retrieveAuthorities(any(), any())(any()))
        .thenReturn(Future.successful(authoritiesWithId))
      when(mockDataStoreConnector.getCompanyName(any)(any)).thenReturn(Future.successful(Some(COMPANY_NAME)))
      when(mockAuthAndCompanyRepo.get(any)).thenReturn(Future.successful(None))

      when(mockDataStoreConnector.getEmail(any())(any())).thenReturn(Future.successful(Right(testEmail)))
      when(mockDataStoreConnector.getXiEori(any())(any())).thenReturn(Future.successful(Some(XI_EORI)))

      private val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AuthoritiesRepository].toInstance(mockRepository),
          bind[AccountsCacheService].toInstance(mockAccountsCacheService),
          bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
          bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
          bind[AuthorisedEoriAndCompanyInfoRepository].toInstance(mockAuthAndCompanyRepo)
        ).configure("features.edit-journey" -> true)
        .build()

      running(application) {

        val request = fakeRequest(GET, fetchAllAuthoritiesRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        verify(mockDataStoreConnector, times(1)).getCompanyName(any())(any())
      }
    }

    "return NO_CONTENT when there are no authorities found due to Pending accounts" in new Setup {
      val accounts: CDSAccounts = CDSAccounts("GB123456789012", List(
        CashAccount("12345", "GB123456789012", AccountStatusPending, CDSCashBalance(Some(100.00))),
        CashAccount("23456", "GB123456789012", AccountStatusPending, CDSCashBalance(Some(100.00)))
      ))

      val mockRepository: AuthoritiesRepository = mock[AuthoritiesRepository]
      val mockAccountsCacheService: AccountsCacheService = mock[AccountsCacheService]
      val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]

      val emptyMap: Map[String, AccountWithAuthoritiesWithId] = Map()
      val emptyAuthoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(emptyMap)

      when(mockRepository.get(any())).thenReturn(Future.successful(Some(emptyAuthoritiesWithId)))

      when(mockAccountsCacheService.retrieveAccounts(any(), any())(any())).thenReturn(Future.successful(accounts))

      when(mockDataStoreConnector.getEmail(any())(any())).thenReturn(Future.successful(Right(testEmail)))
      when(mockDataStoreConnector.getXiEori(any())(any())).thenReturn(Future.successful(Some(XI_EORI)))

      private val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AuthoritiesRepository].toInstance(mockRepository),
          bind[AccountsCacheService].toInstance(mockAccountsCacheService),
          bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
        ).configure("features.edit-journey" -> true)
        .build()

      running(application) {

        val request = fakeRequest(GET, fetchAllAuthoritiesRoute)
        val result = route(application, request).value

        status(result) mustEqual NO_CONTENT
      }
    }

    "return OK even if getCompanyName api throws exception" in new Setup {
      val accounts: CDSAccounts = CDSAccounts("GB123456789012", List(
        CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))),
        CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
      ))

      val mockRepository: AuthoritiesRepository = mock[AuthoritiesRepository]
      val mockAccountsCacheService: AccountsCacheService = mock[AccountsCacheService]
      val mockAuthoritiesCacheService: AuthoritiesCacheService = mock[AuthoritiesCacheService]
      val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]
      val mockAuthAndCompanyRepo: AuthorisedEoriAndCompanyInfoRepository = mock[AuthorisedEoriAndCompanyInfoRepository]

      val emptyMap: Map[String, AccountWithAuthoritiesWithId] = Map()
      val emptyAuthoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(emptyMap)

      when(mockRepository.get(any())).thenReturn(Future.successful(Some(emptyAuthoritiesWithId)))

      when(mockAccountsCacheService.retrieveAccounts(any(), any())(any()))
        .thenReturn(Future.successful(accounts))

      when(mockAuthoritiesCacheService.retrieveAuthorities(any(), any())(any()))
        .thenReturn(Future.successful(authoritiesWithId02))

      when(mockDataStoreConnector.getEmail(any())(any())).thenReturn(Future.successful(Right(testEmail)))
      when(mockDataStoreConnector.getXiEori(any())(any())).thenReturn(Future.successful(Some(XI_EORI)))
      when(mockDataStoreConnector.getCompanyName(any())(any())).thenReturn(Future.failed(new RuntimeException("Failed")))
      when(mockAuthAndCompanyRepo.get(any)).thenReturn(Future.successful(None))

      private val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AuthoritiesRepository].toInstance(mockRepository),
          bind[AccountsCacheService].toInstance(mockAccountsCacheService),
          bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
          bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
          bind[AuthorisedEoriAndCompanyInfoRepository].toInstance(mockAuthAndCompanyRepo)
        ).configure("features.edit-journey" -> true)
        .build()

      running(application) {

        val request = fakeRequest(GET, fetchAllAuthoritiesRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        verify(mockDataStoreConnector, times(2)).getCompanyName(any())(any())
      }
    }

    "return INTERNAL_SERVER_ERROR if there is an error during the processing" in new Setup {
      val mockAccountsCacheService: AccountsCacheService = mock[AccountsCacheService]

      when(mockAccountsCacheService.retrieveAccounts(any(), any())(any()))
        .thenReturn(Future.failed(UpstreamErrorResponse("JSON Validation Error", INTERNAL_SERVER_ERROR)))

      private val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AccountsCacheService].toInstance(mockAccountsCacheService)
        ).build()

      running(application) {
        val request = fakeRequest(GET, fetchAllAuthoritiesRoute)
        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }
  }

  trait Setup {
    lazy val manageAuthoritiesRoute: String = routes.ManageAuthoritiesController.onPageLoad().url
    lazy val manageAuthoritiesUnavailableRoute: String = routes.ManageAuthoritiesController.unavailable().url
    lazy val manageAuthoritiesGBNValidationRoute: String = routes.ManageAuthoritiesController.validationFailure().url

    val eori1 = "EORI"
    val eori2 = "EORI2"
    val eoriAndCompanyMap: Map[String, String] = Map(eori1 -> COMPANY_NAME, eori2 -> COMPANY_NAME)

    val fetchAllAuthoritiesRoute: String =
      routes.ManageAuthoritiesController.fetchAuthoritiesOnMIDVAHomePageLoad(EORI_NUMBER).url

    val startDate: LocalDate = LocalDate.parse("2020-03-01")
    val endDate: LocalDate = LocalDate.parse("2020-04-01")

    val standingAuthority01: StandingAuthority = models.domain.StandingAuthority(
      "EORI", startDate, Some(endDate), viewBalance = false)

    val standingAuthority02: StandingAuthority = models.domain.StandingAuthority(
      "EORI2", startDate, Some(endDate), viewBalance = false)

    val authoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
      "a" -> AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(
        AccountStatusOpen), Map("b" -> standingAuthority01))
    ))

    val authoritiesWithId02: AuthoritiesWithId = AuthoritiesWithId(Map(
      "a" -> AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(
        AccountStatusOpen), Map("b" -> standingAuthority01)),

      "c" -> AccountWithAuthoritiesWithId(CdsCashAccount, "123456", Some(
        AccountStatusClosed), Map("d" -> standingAuthority02))
    ))

    private val gbStanAuthFile153Url = "https://test.co.uk/GB123456789012/SA_000000000153_csv.csv"
    private val gbStanAuthFile154Url = "https://test.co.uk/GB123456789012/SA_000000000154_csv.csv"
    private val xiStanAuthFile153Url = "https://test.co.uk/XI123456789012/SA_000000000153_XI_csv.csv"
    private val xiStanAuthFile154Url = "https://test.co.uk/XI123456789012/SA_000000000154_XI_csv.csv"

    private val standAuthMetadata: StandingAuthorityMetadata =
      StandingAuthorityMetadata(START_DATE_1.getYear, START_DATE_1.getMonthValue, START_DATE_1.getDayOfMonth, Csv,
        models.domain.FileRole.StandingAuthority)

    private val gbStandingAuth1: StandingAuthorityFile = StandingAuthorityFile(
      "SA_000000000153_csv.csv", gbStanAuthFile153Url, FILE_SIZE_500, standAuthMetadata, EORI_NUMBER)
    private val gbStandingAuth2: StandingAuthorityFile = StandingAuthorityFile(
      "SA_000000000154_csv.csv", gbStanAuthFile154Url, FILE_SIZE_500, standAuthMetadata, EORI_NUMBER)

    private val xiStandingAuth1: StandingAuthorityFile = StandingAuthorityFile(
      "SA_XI_000000000153_csv.csv", xiStanAuthFile153Url, FILE_SIZE_500, standAuthMetadata, XI_EORI)
    private val xiStandingAuth2: StandingAuthorityFile = StandingAuthorityFile(
      "SA_XI_000000000154_XI_csv.csv", xiStanAuthFile154Url, FILE_SIZE_500, standAuthMetadata, XI_EORI)

    protected val authCsvFiles: Seq[StandingAuthorityFile] =
      Seq(gbStandingAuth1, gbStandingAuth2, xiStandingAuth1, xiStandingAuth2)

    protected def filesNotificationViewModel(app: Application): AuthoritiesFilesNotificationViewModel =
      AuthoritiesFilesNotificationViewModel(
        Some(gbStanAuthFile154Url), Some(xiStanAuthFile154Url), dateAsDayMonthAndYear(START_DATE_1)(messages(app))
      )
  }
}
