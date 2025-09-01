#!/bin/bash

# SauceDemo Test Framework Runner Script

echo "🚀 Starting SauceDemo Test Framework..."

# Clean and run tests
echo "📋 Running tests..."
mvn clean test

# Generate Allure report
echo "📊 Generating Allure report..."
mvn allure:report

echo "✅ Test execution completed!"
echo "📈 View reports at: target/site/allure-maven-plugin/index.html"
echo "🌐 Or run: mvn allure:serve"
