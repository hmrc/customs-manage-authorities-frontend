
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

The test coverage threshold is currently set at 75%. Any significant commits of code without corresponding tests may result in the build failing.
