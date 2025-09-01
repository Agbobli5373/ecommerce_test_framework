package com.isaac.ecommerce_test_framework.utils;

import com.isaac.ecommerce_test_framework.config.ConfigManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import static com.codeborne.selenide.Selenide.screenshot;

/**
 * Retry analyzer for handling flaky tests
 * Automatically retries failed tests based on configuration
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private final int maxRetryCount = ConfigManager.getInstance().getRetryCount();
    private final boolean retryEnabled = ConfigManager.getInstance().isRetryEnabled();

    @Override
    public boolean retry(ITestResult result) {
        if (!retryEnabled) {
            return false;
        }

        if (retryCount < maxRetryCount) {
            retryCount++;
            String testName = result.getMethod().getMethodName();
            String className = result.getTestClass().getName();

            System.out.println("Retrying test: " + className + "." + testName +
                             " (Attempt " + (retryCount + 1) + " of " + (maxRetryCount + 1) + ")");

            // Add retry information to Allure report
            Allure.addAttachment("Retry Attempt", "text/plain",
                "Test: " + testName + "\nAttempt: " + (retryCount + 1) + "\nMax Attempts: " + (maxRetryCount + 1));

            // Capture screenshot on retry if enabled
            if (ConfigManager.getInstance().isScreenshotOnFailure()) {
                captureScreenshotOnRetry(testName, retryCount);
            }

            return true;
        }

        return false;
    }

    @Attachment(value = "Screenshot on Retry Attempt {retryAttempt}", type = "image/png")
    private byte[] captureScreenshotOnRetry(String testName, int retryAttempt) {
        try {
            // Take screenshot and return as byte array for Allure attachment
            String screenshotPath = screenshot(testName + "_retry_" + retryAttempt);
            if (screenshotPath != null) {
                java.nio.file.Path path = java.nio.file.Paths.get(screenshotPath);
                return java.nio.file.Files.readAllBytes(path);
            }
        } catch (Exception e) {
            System.out.println("Failed to capture screenshot on retry: " + e.getMessage());
        }
        return new byte[0];
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }
}
