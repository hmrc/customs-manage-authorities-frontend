<!-- 
# Customs Manage Authorities Frontend

A frontend component for the CDS Financials project which allows a client to manage access to their accounts.

This application lives in the "public" zone. It integrates with:

* [Customs Financials API](https://github.com/hmrc/customs-financials-api)

In dev/test environments, the upstream services are stubbed out using stub services (see below).

## Running the application locally

The service has the following dependencies:

* `AUTH`
* `AUTH_LOGIN_STUB`
* `AUTH_LOGIN_API`
* `USER_DETAILS`
* `CONTACT_FRONTEND`
* `CUSTOMS_FINANCIALS_API`
* `CUSTOMS_FINANCIALS_HODS_STUB`

You can use the CUSTOMS_MANAGE_AUTHORITIES profile in service manager to start these services.

Once these services are running, you should be able to do `sbt "run 9000"` to start in `DEV` mode.

## Running tests

There is just one test source tree in the `test` folder. Use `sbt test` to run them.

To get a unit test coverage report, you can run `sbt clean coverage test coverageReport`,
then open the resulting coverage report `target/scala-2.12/scoverage-report/index.html` in a web browser.

The test coverage threshold is currently set at 75%. Any significant commits of code without corresponding
tests may result in the build failing.

## All tests and checks

This is a sbt command alias specific to this project. It will run a scala style check, run unit tests, run integration
tests and produce a coverage report: 
> `sbt runAllChecks` -->


# Customs Manage Authorities Frontend

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Coverage](https://img.shields.io/badge/test_coverage-90-green.svg)](/target/scala-3.3.5/scoverage-report/index.html) [![Accessibility](https://img.shields.io/badge/WCAG2.2-AA-purple.svg)](https://www.gov.uk/service-manual/helping-people-to-use-your-service/understanding-wcag)

A micro-frontend service - This service provides an interface which allows a client to manage access to their accounts

This service is built following GDS standards to [WCAG 2.2 AA](https://www.gov.uk/service-manual/helping-people-to-use-your-service/understanding-wcag)

We use the [GOV.UK design system](https://design-system.service.gov.uk/) to ensure consistency and compliance through the project

This applications lives in the "public" zone. It integrates with:

Secure Payments Service (SPS) / Enterprise Tax Management Platform (ETMP) via the [Customs Financials API](https://github.com/hmrc/customs-financials-api)

Secure Document Exchange Service (SDES) bulk data API via the [SDES proxy](https://github.com/hmrc/secure-data-exchange-proxy)

## Running the service

*From the root directory*

`sbt run` - starts the service locally

`sbt runAllChecks` - Will run all checks required for a successful build

Default service port on local - 9000

### Required dependencies

There are a number of dependencies required to run the service.

The easiest way to get started with these is via the service manager CLI - you can find the installation guide [here](https://docs.tax.service.gov.uk/mdtp-handbook/documentation/developer-set-up/set-up-service-manager.html)

| Command                                          | Description |
| --------                                         | ------- |
| `sm2 --start CUSTOMS_FINANCIALS_ALL`             | Runs all dependencies |
| `sm2 -s`                                         | Shows running services |
| `sm2 --stop CUSTOMS_MANAGE_AUTHORITIES_FRONTEND` | Stop the micro service  |
| `sbt run`                                        | (from root dir) to compile the current service with your changes |


### Runtime Dependencies

* `AUTH`
* `AUTH_LOGIN_STUB`
* `AUTH_LOGIN_API`
* `BAS_GATEWAY`
* `CA_FRONTEND`
* `SSO`
* `USER_DETAILS`
* `CUSTOMS_FINANCIALS_API`
* `CUSTOMS_FINANCIALS_HODS_STUB`
* `CUSTOMS_FINANCIALS_SDES_STUB`
* `CONTACT_FRONTEND`

### Login enrolments

The service can be accessed by using below enrolments and with below sample EORI numbers, via http://localhost:9949/auth-login-stub/gg-sign-in (on local) or https://<host:port>/auth-login-stub/gg-sign-in on DEV/QA/STAGING

Redirect URL - `/customs/payment-records`

| Enrolment Key	| Identifier Name | Identifier Value |
| -------- | ------- | ------- |
| `HMRC-CUS-ORG` | `EORINumber`| `GB744638982000` |
| `HMRC-CUS-ORG` | `EORINumber`| `GB744638982001` |

## Testing

The minimum requirement for test coverage is 90%. Builds will fail when the project drops below this threshold.

### Unit Tests

| Command    | Description |
| -------- | ------- |
| `sbt test` | Runs unit tests locally |
| `sbt "test:testOnly *TEST_FILE_NAME*"` | runs tests for a single file |

### Coverage

| Command    | Description |
| -------- | ------- |
| `sbt clean coverage test coverageReport` | Generates a unit test coverage report that you can find here target/scala-3.3.5/scoverage-report/index.html  |

## Available Routes

You can find a list of microservice specific routes here - `/conf/app.routes`

Application entrypoint:  `/customs/payment-records` 

## Feature Switches

> ### Caution!
> There's a risk of WIP features being exposed in production! 
> **Don't** enable features in `application.conf`, as this will apply globally by default

### Enable features
| Command    | Description |
| -------- | ------- |
| `sbt "run -Dfeatures.some-feature-name=true"` | enables a feature locally without risking exposure |

### Available feature flags
| Flag     | Description |
| -------- | ------- |
| `fixed-system-time` | Fixes the system time for development and testing |
| `xi-eori-enabled` |  Enable the XI EORI related api calls               |
| `eu-eori-enabled` |  Enable the EU EORI feature                         |

Different features can be enabled / disabled per-environment via the `app-config-<env>` project by setting `features.some-feature: true`

## Helpful commands

| Command                                       | Description |
| --------                                      | ------- |
| `sbt runAllChecks`                            | Runs all standard code checks |
| `sbt clean`                                   | Cleans code |
| `sbt compile`                                 | Better to say 'Compiles the code' |
| `sbt coverage`                                | Prints code coverage |
| `sbt test`                                    | Runs unit tests |
| `sbt it/test`                                 | Runs integration tests |
| `sbt scalafmtCheckAll`                        | Runs code formatting checks based on .scalafmt.conf |
| `sbt scalastyle`                              | Runs code style checks based on /scalastyle-config.xml  |
| `sbt Test/scalastyle`                         | Runs code style checks for unit test code /test-scalastyle-config.xml |
| `sbt coverageReport`                          | Produces a code coverage report |
| `sbt "test:testOnly *TEST_FILE_NAME*"`        | runs tests for a single file |
| `sbt clean coverage test coverageReport`      | Generates a unit test coverage report that you can find here target/scala-3.3.5/scoverage-report/index.html  |
| `sbt "run -Dfeatures.some-feature-name=true"` | enables a feature locally without risking exposure |


