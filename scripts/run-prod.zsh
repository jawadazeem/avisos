#!/bin/zsh

#
# (C) Copyright 2026 Jawad Azeem
# Apache 2.0 License
#

chmod +x run-prod.zsh

echo "Building the project..."
mvn -f ../pom.xml clean package

echo "Running the app..."
java -jar ../target/avisos-1.0-SNAPSHOT.jar
