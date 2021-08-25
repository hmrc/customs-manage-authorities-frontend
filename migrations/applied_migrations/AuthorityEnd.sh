#!/bin/bash

echo ""
echo "Applying migration AuthorityEnd"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /authorityEnd                        controllers.AuthorityEndController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /authorityEnd                        controllers.AuthorityEndController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAuthorityEnd                  controllers.AuthorityEndController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAuthorityEnd                  controllers.AuthorityEndController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "authorityEnd.title = How long should the authority last?" >> ../conf/messages.en
echo "authorityEnd.heading = How long should the authority last?" >> ../conf/messages.en
echo "authorityEnd.indefinite = Until further notice" >> ../conf/messages.en
echo "authorityEnd.setDate = Until a set date" >> ../conf/messages.en
echo "authorityEnd.checkYourAnswersLabel = How long should the authority last?" >> ../conf/messages.en
echo "authorityEnd.error.required = Select authorityEnd" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/  self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorityEndUserAnswersEntry: Arbitrary[(AuthorityEndPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AuthorityEndPage.type]";\
    print "        value <- arbitrary[AuthorityEnd].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorityEndPage: Arbitrary[AuthorityEndPage.type] =";\
    print "    Arbitrary(AuthorityEndPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/  self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorityEnd: Arbitrary[AuthorityEnd] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(AuthorityEnd.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AuthorityEndPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration AuthorityEnd completed"
