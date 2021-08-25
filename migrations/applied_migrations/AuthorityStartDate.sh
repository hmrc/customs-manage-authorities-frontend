#!/bin/bash

echo ""
echo "Applying migration AuthorityStartDate"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /authorityStartDate                  controllers.AuthorityStartDateController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /authorityStartDate                  controllers.AuthorityStartDateController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAuthorityStartDate                        controllers.AuthorityStartDateController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAuthorityStartDate                        controllers.AuthorityStartDateController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "authorityStartDate.title = AuthorityStartDate" >> ../conf/messages.en
echo "authorityStartDate.heading = AuthorityStartDate" >> ../conf/messages.en
echo "authorityStartDate.checkYourAnswersLabel = AuthorityStartDate" >> ../conf/messages.en
echo "authorityStartDate.error.required.all = Enter the authorityStartDate" >> ../conf/messages.en
echo "authorityStartDate.error.required.two = The authorityStartDate" must include {0} and {1} >> ../conf/messages.en
echo "authorityStartDate.error.required = The authorityStartDate must include {0}" >> ../conf/messages.en
echo "authorityStartDate.error.invalid = Enter a real AuthorityStartDate" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/  self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorityStartDateUserAnswersEntry: Arbitrary[(AuthorityStartDatePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AuthorityStartDatePage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorityStartDatePage: Arbitrary[AuthorityStartDatePage.type] =";\
    print "    Arbitrary(AuthorityStartDatePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AuthorityStartDatePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration AuthorityStartDate completed"
