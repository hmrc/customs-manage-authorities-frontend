#!/bin/bash

echo ""
echo "Applying migration AddConfirmation"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /addConfirmation                       controllers.AddConfirmationController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "addConfirmation.title = addConfirmation" >> ../conf/messages.en
echo "addConfirmation.heading = addConfirmation" >> ../conf/messages.en

echo "Migration AddConfirmation completed"
