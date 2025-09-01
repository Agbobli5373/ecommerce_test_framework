package com.isaac.ecommerce_test_framework.utils;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.isaac.ecommerce_test_framework.config.ConfigManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import static com.codeborne.selenide.Selenide.*;

/**
 * Comprehensive test utilities for enhanced test automation
 */
public class TestUtilities {

    private static final Random random = new Random();
    private static final ConfigManager config = ConfigManager.getInstance();

    // Wait Utilities
    public static void waitForPageLoad() {
        waitForPageLoad(config.getTimeout());
    }

    public static void waitForPageLoad(int timeoutMs) {
        WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofMillis(timeoutMs));
        wait.until(driver -> ((JavascriptExecutor) driver)
            .executeScript("return document.readyState").equals("complete"));
    }

    public static void waitForAjax() {
        waitForAjax(config.getTimeout());
    }

    public static void waitForAjax(int timeoutMs) {
        WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofMillis(timeoutMs));
        wait.until(driver -> (Boolean) ((JavascriptExecutor) driver)
            .executeScript("return jQuery.active === 0"));
    }



    // Element Interaction Utilities
    public static void safeClick(SelenideElement element) {
        safeClick(element, config.getTimeout());
    }

    public static void safeClick(SelenideElement element, int timeoutMs) {
        try {
            element.shouldBe(Condition.visible).click();
        } catch (Exception e) {
            // Try JavaScript click as fallback
            executeJavaScript("arguments[0].click();", element);
        }
    }

    public static void safeSendKeys(SelenideElement element, String text) {
        safeSendKeys(element, text, config.getTimeout());
    }

    public static void safeSendKeys(SelenideElement element, String text, int timeoutMs) {
        element.shouldBe(Condition.visible).clear();
        element.setValue(text);
    }

    public static void scrollToElement(SelenideElement element) {
        executeJavaScript("arguments[0].scrollIntoView(true);", element);
    }

    public static void scrollToTop() {
        executeJavaScript("window.scrollTo(0, 0);");
    }

    public static void scrollToBottom() {
        executeJavaScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    // JavaScript Utilities
    public static Object executeJavaScript(String script, Object... args) {
        try {
            return ((JavascriptExecutor) WebDriverRunner.getWebDriver()).executeScript(script, args);
        } catch (Exception e) {
            System.err.println("Failed to execute JavaScript: " + e.getMessage());
            return null;
        }
    }

    public static String getPageTitle() {
        return (String) executeJavaScript("return document.title;");
    }

    public static String getCurrentUrl() {
        return WebDriverRunner.url();
    }

    public static void refreshPage() {
        refresh();
    }

    public static void navigateBack() {
        back();
    }

    public static void navigateForward() {
        forward();
    }

    // Screenshot and Visual Utilities
    @Attachment(value = "Screenshot", type = "image/png")
    public static byte[] captureScreenshot() {
        return captureScreenshot("screenshot");
    }

    @Attachment(value = "{description}", type = "image/png")
    public static byte[] captureScreenshot(String description) {
        try {
            // Use Selenide's screenshot method which handles path creation properly
            String screenshotPath = screenshot(description);
            if (screenshotPath != null && !screenshotPath.isEmpty()) {
                // Ensure the path is valid for file operations
                java.nio.file.Path path = java.nio.file.Paths.get(screenshotPath);
                if (java.nio.file.Files.exists(path)) {
                    return java.nio.file.Files.readAllBytes(path);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        }
        return new byte[0];
    }

    public static void captureScreenshotOnFailure(String testName) {
        if (config.isScreenshotOnFailure()) {
            // Sanitize test name to avoid illegal characters in filename
            String sanitizedTestName = testName.replaceAll("[^a-zA-Z0-9_-]", "_");
            captureScreenshot("FAILURE_" + sanitizedTestName);
        }
    }

    // Random Data Generation
    public static String generateRandomString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    public static String generateRandomNumber(int length) {
        return RandomStringUtils.randomNumeric(length);
    }

    public static String generateRandomAlphanumeric(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    public static int generateRandomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static String generateRandomEmail() {
        return generateRandomString(8) + "@" + generateRandomString(5) + ".com";
    }

    // Validation Utilities
    public static void assertElementVisible(SelenideElement element) {
        assertElementVisible(element, "Element should be visible");
    }

    public static void assertElementVisible(SelenideElement element, String message) {
        try {
            element.shouldBe(Condition.visible);
        } catch (AssertionError e) {
            captureScreenshot("ASSERTION_FAILURE");
            Assert.fail(message + ": " + e.getMessage());
        }
    }

    public static void assertElementNotVisible(SelenideElement element) {
        assertElementNotVisible(element, "Element should not be visible");
    }

    public static void assertElementNotVisible(SelenideElement element, String message) {
        try {
            element.shouldNotBe(Condition.visible);
        } catch (AssertionError e) {
            captureScreenshot("ASSERTION_FAILURE");
            Assert.fail(message + ": " + e.getMessage());
        }
    }

    public static void assertTextEquals(SelenideElement element, String expectedText) {
        assertTextEquals(element, expectedText, "Text should match expected value");
    }

    public static void assertTextEquals(SelenideElement element, String expectedText, String message) {
        try {
            element.shouldHave(Condition.text(expectedText));
        } catch (AssertionError e) {
            captureScreenshot("ASSERTION_FAILURE");
            Assert.fail(message + ": Expected '" + expectedText + "', but was '" + element.getText() + "'");
        }
    }

    public static void assertTextContains(SelenideElement element, String expectedText) {
        assertTextContains(element, expectedText, "Text should contain expected value");
    }

    public static void assertTextContains(SelenideElement element, String expectedText, String message) {
        try {
            element.shouldHave(Condition.text(expectedText));
        } catch (AssertionError e) {
            captureScreenshot("ASSERTION_FAILURE");
            Assert.fail(message + ": Expected to contain '" + expectedText + "', but was '" + element.getText() + "'");
        }
    }

    // List and Collection Utilities
    public static int getElementCount(List<SelenideElement> elements) {
        return elements.size();
    }

    public static boolean isListEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isListNotEmpty(List<?> list) {
        return !isListEmpty(list);
    }

    // Browser and Window Utilities
    public static void maximizeWindow() {
        WebDriverRunner.getWebDriver().manage().window().maximize();
    }

    public static void setWindowSize(int width, int height) {
        WebDriverRunner.getWebDriver().manage().window().setSize(new Dimension(width, height));
    }

    public static Dimension getWindowSize() {
        return WebDriverRunner.getWebDriver().manage().window().getSize();
    }

    public static void switchToNewTab() {
        String currentWindow = WebDriverRunner.getWebDriver().getWindowHandle();
        switchTo().window(1); // Switch to second tab/window
    }

    public static void closeCurrentTabAndSwitchBack() {
        WebDriverRunner.getWebDriver().close();
        switchTo().window(0); // Switch back to first tab/window
    }

    // Cookie and Local Storage Utilities
    public static void addCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        WebDriverRunner.getWebDriver().manage().addCookie(cookie);
    }

    public static Cookie getCookie(String name) {
        return WebDriverRunner.getWebDriver().manage().getCookieNamed(name);
    }

    public static void deleteCookie(String name) {
        WebDriverRunner.getWebDriver().manage().deleteCookieNamed(name);
    }

    public static void clearAllCookies() {
        WebDriverRunner.getWebDriver().manage().deleteAllCookies();
    }

    // Performance Utilities
    public static long getPageLoadTime() {
        return (Long) executeJavaScript("return performance.timing.loadEventEnd - performance.timing.navigationStart;");
    }

    public static void logPerformanceMetrics(String testName) {
        try {
            long loadTime = getPageLoadTime();
            Allure.addAttachment("Performance Metrics", "text/plain",
                "Test: " + testName + "\nPage Load Time: " + loadTime + "ms");

            if (loadTime > Integer.parseInt(config.getProperty("performance.slow.test.threshold", "5000"))) {
                System.out.println("WARNING: Slow test detected - " + testName + " took " + loadTime + "ms");
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to log performance metrics: " + e.getMessage());
        }
    }

    // Logging Utilities
    public static void logInfo(String message) {
        System.out.println("[INFO] " + message);
        try {
            Allure.addAttachment("Log Info", "text/plain", message);
        } catch (Exception e) {
            // Silently handle Allure attachment errors
        }
    }

    public static void logError(String message) {
        System.err.println("[ERROR] " + message);
        try {
            Allure.addAttachment("Log Error", "text/plain", message);
        } catch (Exception e) {
            // Silently handle Allure attachment errors
        }
    }

    public static void logStep(String stepDescription) {
        System.out.println("[STEP] " + stepDescription);
        try {
            Allure.addAttachment("Test Step", "text/plain", stepDescription);
        } catch (Exception e) {
            // Silently handle Allure attachment errors
        }
    }
}
