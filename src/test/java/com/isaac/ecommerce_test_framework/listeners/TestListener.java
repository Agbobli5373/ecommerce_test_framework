package com.isaac.ecommerce_test_framework.listeners;

import com.isaac.ecommerce_test_framework.config.ConfigManager;
import com.isaac.ecommerce_test_framework.utils.TestUtilities;
import io.qameta.allure.Allure;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Test listener for enhanced test reporting and monitoring
 */
public class TestListener implements ITestListener {

    private ConfigManager config = ConfigManager.getInstance();

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();

        TestUtilities.logInfo("Starting test: " + className + "." + testName);
        Allure.addAttachment("Test Start Time", "text/plain",
            new java.util.Date().toString());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();

        TestUtilities.logInfo("Test PASSED: " + testName + " (Duration: " + duration + "ms)");
        Allure.addAttachment("Test Result", "text/plain", "PASSED");
        Allure.addAttachment("Execution Time", "text/plain", duration + " ms");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String errorMessage = result.getThrowable().getMessage();
        long duration = result.getEndMillis() - result.getStartMillis();

        TestUtilities.logError("Test FAILED: " + testName + " - " + errorMessage);

        // Capture screenshot on failure
        TestUtilities.captureScreenshotOnFailure(testName);

        // Add failure details to Allure report
        Allure.addAttachment("Test Result", "text/plain", "FAILED");
        Allure.addAttachment("Error Message", "text/plain", errorMessage);
        Allure.addAttachment("Execution Time", "text/plain", duration + " ms");
        Allure.addAttachment("Failure Time", "text/plain", new java.util.Date().toString());

        // Log stack trace
        String stackTrace = getStackTrace(result.getThrowable());
        Allure.addAttachment("Stack Trace", "text/plain", stackTrace);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        TestUtilities.logInfo("Test SKIPPED: " + testName);
        Allure.addAttachment("Test Result", "text/plain", "SKIPPED");
    }

    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");

        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\t").append(element.toString()).append("\n");
        }

        return sb.toString();
    }
}
