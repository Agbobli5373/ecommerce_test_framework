package com.isaac.ecommerce_test_framework;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import static com.codeborne.selenide.Selenide.open;

/**
 * Base test class for all SauceDemo tests
 * Sets up Selenide configuration and provides common test utilities
 */
public class BaseTest {

    @BeforeMethod
    public void setUp() {
        // Configure Selenide
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10000;
        Configuration.screenshots = true;
        Configuration.savePageSource = false;
        Configuration.reopenBrowserOnFail = true;

        // Uncomment for headless mode
        // Configuration.headless = true;

        // Open the application
        open("https://www.saucedemo.com/");
    }

    @AfterMethod
    public void tearDown() {
        Selenide.closeWebDriver();
    }

    @Step("Navigate to SauceDemo homepage")
    protected void navigateToHomePage() {
        open("https://www.saucedemo.com/");
    }
}
