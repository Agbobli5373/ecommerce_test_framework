# SauceDemo E-commerce Test Framework

A comprehensive test automation framework for testing the SauceDemo e-commerce application using Java, TestNG, Selenide, and Allure reporting.

## 🚀 Features

- **Page Object Model (POM)**: Clean separation of test logic and page elements
- **Comprehensive Test Coverage**: Login, Products, Cart, and Checkout functionality
- **Allure Reporting**: Beautiful test reports with screenshots and step-by-step execution
- **Cross-browser Testing**: Support for Chrome, Firefox, Edge, and Safari
- **Parallel Execution**: Run tests in parallel for faster execution
- **Data-driven Testing**: Support for different user types and test data

## 🏗️ Architecture

```
src/
├── test/
│   ├── java/
│   │   └── com/isaac/ecommerce_test_framework/
│   │       ├── BaseTest.java                 # Base test class with setup/teardown
│   │       ├── pages/                        # Page Object classes
│   │       │   ├── LoginPage.java
│   │       │   ├── ProductsPage.java
│   │       │   ├── CartPage.java
│   │       │   ├── CheckoutPage.java
│   │       │   ├── CheckoutOverviewPage.java
│   │       │   └── CheckoutCompletePage.java
│   │       └── tests/                        # Test classes
│   │           ├── LoginTests.java
│   │           ├── ProductTests.java
│   │           ├── CartTests.java
│   │           └── CheckoutTests.java
```

## 🛠️ Technologies Used

- **Java 23**: Programming language
- **TestNG**: Test framework
- **Selenide**: Web automation wrapper for Selenium
- **Allure**: Test reporting
- **Maven**: Build and dependency management
- **AspectJ**: For Allure integration

## 📋 Prerequisites

- Java 23 or higher
- Maven 3.6+
- Chrome browser (or configure for other browsers)

## 🚀 Getting Started

### 1. Clone and Setup

```bash
# Navigate to project directory
cd ecommerce_test_framework

# Install dependencies
mvn clean install
```

### 2. Run Tests

#### Run all tests

```bash
mvn clean test
```

#### Run specific test class

```bash
mvn clean test -Dtest=LoginTests
```

#### Run with TestNG XML

```bash
mvn clean test -DsuiteXmlFile=testng.xml
```

#### Run in headless mode

```bash
mvn clean test -Dselenide.headless=true
```

### 3. Generate Allure Report

```bash
# Generate report
mvn allure:report

# Serve report
mvn allure:serve
```

## 🧪 Test Scenarios Covered

### Login Tests

- ✅ Successful login with valid credentials
- ✅ Invalid username/password validation
- ✅ Empty field validation
- ✅ Locked out user handling
- ✅ Different user types (standard, problem, performance glitch)

### Product Tests

- ✅ Product display and information
- ✅ Add/remove products from cart
- ✅ Cart badge updates
- ✅ Product sorting (name A-Z, Z-A, price low-high, high-low)

### Cart Tests

- ✅ Empty cart display
- ✅ Single and multiple item management
- ✅ Item removal from cart
- ✅ Continue shopping navigation
- ✅ Cart persistence

### Checkout Tests

- ✅ Complete checkout flow
- ✅ Form validation (required fields)
- ✅ Order summary verification
- ✅ Cancel functionality
- ✅ Empty cart checkout prevention

## 🔧 Configuration

### Browser Configuration

The framework uses Chrome by default. To change browser:

```java
// In BaseTest.java
Configuration.browser = "firefox"; // or "edge", "safari"
```

### Selenoid Configuration

For running tests in Docker containers with Selenoid:

1. Update `browsers.json` with your Selenoid configuration
2. Set remote WebDriver URL:

```java
Configuration.remote = "http://localhost:4444/wd/hub";
```

### Test Data

The framework uses hardcoded test data. For data-driven testing, you can:

1. Add TestNG DataProvider methods
2. Use external data sources (CSV, JSON, databases)
3. Implement property files for configuration

## 📊 Reporting

### Allure Reports

Allure provides detailed test reports with:

- Step-by-step test execution
- Screenshots on failure
- Test execution timeline
- Historical trends
- Test categorization

### Accessing Reports

```bash
# Generate and open report
mvn allure:serve
```

Reports will be available at: `http://localhost:8080`

## 🏃‍♂️ Best Practices Implemented

- **Page Object Model**: Separation of concerns
- **Explicit Waits**: Selenide handles waits automatically
- **Test Isolation**: Each test is independent
- **Descriptive Test Names**: Clear test naming with Allure annotations
- **Error Handling**: Proper exception handling and assertions
- **Screenshots**: Automatic screenshots on test failures

## 🔍 Debugging

### Enable Debug Mode

```java
// In BaseTest.java
Configuration.screenshots = true;
Configuration.savePageSource = true;
Configuration.reopenBrowserOnFail = false; // Keep browser open on failure
```

### View Browser Logs

```java
Configuration.browserCapabilities.setCapability("goog:loggingPrefs", Map.of("browser", "ALL"));
```

## 🚀 CI/CD Integration

### GitHub Actions Example

```yaml
name: E-commerce Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: "23"
          distribution: "temurin"
      - run: mvn clean test
      - run: mvn allure:report
      - uses: actions/upload-artifact@v3
        with:
          name: allure-report
          path: target/allure-results/
```

## 📈 Extending the Framework

### Adding New Tests

1. Create new test methods in existing test classes
2. Or create new test classes following the naming convention
3. Update `testng.xml` if needed

### Adding New Pages

1. Create new Page Object class in `pages/` directory
2. Implement page-specific methods
3. Update existing page classes if navigation changes

### Custom Assertions

Add custom assertion methods in `BaseTest.java` or create utility classes.

## 🐛 Troubleshooting

### Common Issues

1. **Browser not found**: Ensure browser is installed and PATH is set
2. **Tests failing intermittently**: Add explicit waits or retry mechanisms
3. **Allure reports not generating**: Check AspectJ configuration in `pom.xml`

### Performance Tips

1. Run tests in parallel: `-Dparallel=methods -DthreadCount=3`
2. Use headless mode for CI: `-Dselenide.headless=true`
3. Disable screenshots in CI: `Configuration.screenshots = false`

## 📚 Resources

- [SauceDemo](https://www.saucedemo.com/) - Test application
- [Selenide Documentation](https://selenide.org/documentation.html)
- [TestNG Documentation](https://testng.org/doc/)
- [Allure Documentation](https://docs.qameta.io/allure/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
