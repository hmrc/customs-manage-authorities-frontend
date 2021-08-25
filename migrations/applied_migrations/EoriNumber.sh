#!/bin/bash

echo ""
echo "Applying migration EoriNumber"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /eoriNumber                        controllers.EoriNumberController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /eoriNumber                        controllers.EoriNumberController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeEoriNumber                  controllers.EoriNumberController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeEoriNumber                  controllers.EoriNumberController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "eoriNumber.title = eoriNumber" >> ../conf/messages.en
echo "eoriNumber.heading = eoriNumber" >> ../conf/messages.en
echo "eoriNumber.checkYourAnswersLabel = eoriNumber" >> ../conf/messages.en
echo "eoriNumber.error.required = Enter eoriNumber" >> ../conf/messages.en
echo "eoriNumber.error.length = EoriNumber must be 14 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/  self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEoriNumberUserAnswersEntry: Arbitrary[(EoriNumberPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[EoriNumberPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEoriNumberPage: Arbitrary[EoriNumberPage.type] =";\
    print "    Arbitrary(EoriNumberPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(EoriNumberPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration EoriNumber completed"
