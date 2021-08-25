#!/bin/bash

echo ""
echo "Applying migration ShowBalance"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /showBalance                        controllers.ShowBalanceController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /showBalance                        controllers.ShowBalanceController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeShowBalance                  controllers.ShowBalanceController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeShowBalance                  controllers.ShowBalanceController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "showBalance.title = Allow this user to see the available balance?" >> ../conf/messages.en
echo "showBalance.heading = Allow this user to see the available balance?" >> ../conf/messages.en
echo "showBalance.yes = Yes" >> ../conf/messages.en
echo "showBalance.no = No, just allow them to use the account" >> ../conf/messages.en
echo "showBalance.checkYourAnswersLabel = Allow this user to see the available balance?" >> ../conf/messages.en
echo "showBalance.error.required = Select showBalance" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/  self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryShowBalanceUserAnswersEntry: Arbitrary[(ShowBalancePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ShowBalancePage.type]";\
    print "        value <- arbitrary[ShowBalance].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryShowBalancePage: Arbitrary[ShowBalancePage.type] =";\
    print "    Arbitrary(ShowBalancePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/  self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryShowBalance: Arbitrary[ShowBalance] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(ShowBalance.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ShowBalancePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration ShowBalance completed"
