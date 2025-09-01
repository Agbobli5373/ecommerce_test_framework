package com.isaac.ecommerce_test_framework;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.isaac.ecommerce_test_framework.config.ConfigManager;
import com.isaac.ecommerce_test_framework.utils.RetryAnalyzer;
import com.isaac.ecommerce_test_framework.utils.TestUtilities;
import io.qameta.allure.Step;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import static com.codeborne.selenide.Selenide.open;

/**
 * Enhanced base test class for all SauceDemo tests
 * Features improved configuration management, retry mechanism, and comprehensive utilities
 */
@Listeners({com.isaac.ecommerce_test_framework.listeners.TestListener.class})
public class BaseTest {

    protected ConfigManager config = ConfigManager.getInstance();

    @BeforeMethod
    public void setUp() {
        // Configure Selenide using ConfigManager
        Configuration.browser = config.getBrowser();
        Configuration.browserSize = config.getBrowserSize();
        Configuration.timeout = config.getTimeout();
        Configuration.screenshots = config.isScreenshotOnFailure();
        Configuration.savePageSource = config.getProperty("reporting.save.page.source", "false").equals("true");
        Configuration.reopenBrowserOnFail = config.getProperty("browser.reopen.on.fail", "true").equals("true");
        Configuration.headless = config.isHeadless();

        // Configure remote execution if enabled
        if (config.isRemoteExecution()) {
            Configuration.remote = config.getRemoteUrl();
        }

        // Log test setup information
        TestUtilities.logInfo("Test setup completed with browser: " + config.getBrowser() +
                            ", headless: " + config.isHeadless());

        // Open the application
        navigateToHomePage();
    }

    @AfterMethod
    public void tearDown() {
        // Log performance metrics
        TestUtilities.logPerformanceMetrics(this.getClass().getSimpleName());

        // Close browser
        Selenide.closeWebDriver();
    }

    @Step("Navigate to application homepage")
    protected void navigateToHomePage() {
        String baseUrl = config.getBaseUrl();
        TestUtilities.logStep("Navigating to: " + baseUrl);
        open(baseUrl);
        TestUtilities.waitForPageLoad();
    }

    // Utility methods for common test operations
    protected void loginWithUser(String username, String password) {
        TestUtilities.logStep("Logging in with user: " + username);
        // This would be implemented using the LoginPage
        // For now, just log the action
    }

    protected void captureScreenshot(String description) {
        TestUtilities.captureScreenshot(description);
    }

    protected void logTestStep(String step) {
        TestUtilities.logStep(step);
    }

    // Configuration access methods
    protected String getTestEnvironment() {
        return System.getProperty("environment", "local");
    }

    protected boolean isHeadlessMode() {
        return config.isHeadless();
    }

    protected String getCurrentBrowser() {
        return config.getBrowser();
    }
}
