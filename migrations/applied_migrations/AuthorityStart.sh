#!/bin/bash

echo ""
echo "Applying migration AuthorityStart"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /authorityStart                        controllers.AuthorityStartController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /authorityStart                        controllers.AuthorityStartController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAuthorityStart                  controllers.AuthorityStartController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAuthorityStart                  controllers.AuthorityStartController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "authorityStart.title = When do you want the authority to start?" >> ../conf/messages.en
echo "authorityStart.heading = When do you want the authority to start?" >> ../conf/messages.en
echo "authorityStart.today = Today (usually active within an hour)" >> ../conf/messages.en
echo "authorityStart.setDate = On a set date" >> ../conf/messages.en
echo "authorityStart.checkYourAnswersLabel = When do you want the authority to start?" >> ../conf/messages.en
echo "authorityStart.error.required = Select authorityStart" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/  self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorityStartUserAnswersEntry: Arbitrary[(AuthorityStartPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AuthorityStartPage.type]";\
    print "        value <- arbitrary[AuthorityStart].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorityStartPage: Arbitrary[AuthorityStartPage.type] =";\
    print "    Arbitrary(AuthorityStartPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/  self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorityStart: Arbitrary[AuthorityStart] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(AuthorityStart.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AuthorityStartPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration AuthorityStart completed"
