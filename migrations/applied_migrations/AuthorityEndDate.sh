#!/bin/bash

echo ""
echo "Applying migration AuthorityEndDate"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /authorityEndDate                  controllers.AuthorityEndDateController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /authorityEndDate                  controllers.AuthorityEndDateController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAuthorityEndDate                        controllers.AuthorityEndDateController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAuthorityEndDate                        controllers.AuthorityEndDateController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "authorityEndDate.title = AuthorityEndDate" >> ../conf/messages.en
echo "authorityEndDate.heading = AuthorityEndDate" >> ../conf/messages.en
echo "authorityEndDate.checkYourAnswersLabel = AuthorityEndDate" >> ../conf/messages.en
echo "authorityEndDate.error.required.all = Enter the authorityEndDate" >> ../conf/messages.en
echo "authorityEndDate.error.required.two = The authorityEndDate" must include {0} and {1} >> ../conf/messages.en
echo "authorityEndDate.error.required = The authorityEndDate must include {0}" >> ../conf/messages.en
echo "authorityEndDate.error.invalid = Enter a real AuthorityEndDate" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/  self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorityEndDateUserAnswersEntry: Arbitrary[(AuthorityEndDatePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AuthorityEndDatePage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorityEndDatePage: Arbitrary[AuthorityEndDatePage.type] =";\
    print "    Arbitrary(AuthorityEndDatePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AuthorityEndDatePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration AuthorityEndDate completed"
