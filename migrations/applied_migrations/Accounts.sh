#!/bin/bash

echo ""
echo "Applying migration Accounts"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /accounts                        controllers.AccountsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /accounts                        controllers.AccountsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAccounts                  controllers.AccountsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAccounts                  controllers.AccountsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "accounts.title = Which accounts do you want to add an authority to?" >> ../conf/messages.en
echo "accounts.heading = Which accounts do you want to add an authority to?" >> ../conf/messages.en
echo "accounts.one = Duty deferment: 12345678" >> ../conf/messages.en
echo "accounts.two = General guarantee: 8475938" >> ../conf/messages.en
echo "accounts.checkYourAnswersLabel = Which accounts do you want to add an authority to?" >> ../conf/messages.en
echo "accounts.error.required = Select accounts" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/  self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAccountsUserAnswersEntry: Arbitrary[(AccountsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AccountsPage.type]";\
    print "        value <- arbitrary[Accounts].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAccountsPage: Arbitrary[AccountsPage.type] =";\
    print "    Arbitrary(AccountsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/  self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAccounts: Arbitrary[Accounts] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(Accounts.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AccountsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration Accounts completed"
