#!/bin/bash

echo ""
echo "Applying migration AuthorisedUser"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /authorisedUser                        controllers.AuthorisedUserController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /authorisedUser                        controllers.AuthorisedUserController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAuthorisedUser                  controllers.AuthorisedUserController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAuthorisedUser                  controllers.AuthorisedUserController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "authorisedUser.title = authorisedUser" >> ../conf/messages.en
echo "authorisedUser.heading = authorisedUser" >> ../conf/messages.en
echo "authorisedUser.checkYourAnswersLabel = authorisedUser" >> ../conf/messages.en
echo "authorisedUser.error.required = Enter authorisedUser" >> ../conf/messages.en
echo "authorisedUser.error.length = AuthorisedUser must be 50 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/  self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorisedUserUserAnswersEntry: Arbitrary[(AuthorisedUserPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AuthorisedUserPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorisedUserPage: Arbitrary[AuthorisedUserPage.type] =";\
    print "    Arbitrary(AuthorisedUserPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AuthorisedUserPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration AuthorisedUser completed"
