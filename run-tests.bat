@echo off
REM SauceDemo Test Framework Runner Script for Windows

echo 🚀 Starting SauceDemo Test Framework...

REM Clean and run tests
echo 📋 Running tests...
call mvn clean test

REM Generate Allure report
echo 📊 Generating Allure report...
call mvn allure:report

echo ✅ Test execution completed!
echo 📈 View reports at: target\site\allure-maven-plugin\index.html
echo 🌐 Or run: mvn allure:serve

pause
