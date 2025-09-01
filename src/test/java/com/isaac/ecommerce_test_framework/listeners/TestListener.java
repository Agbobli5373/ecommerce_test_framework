package com.isaac.ecommerce_test_framework.listeners;

import com.isaac.ecommerce_test_framework.analytics.TestExecutionAnalytics;
import com.isaac.ecommerce_test_framework.config.ConfigManager;
import com.isaac.ecommerce_test_framework.flakiness.FlakinessAnalyzer;
import com.isaac.ecommerce_test_framework.utils.TestUtilities;
import io.qameta.allure.Allure;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Enhanced test listener for comprehensive test reporting, analytics, and flakiness detection
 */
public class TestListener implements ITestListener {

    private ConfigManager config = ConfigManager.getInstance();
    private TestExecutionAnalytics analytics = TestExecutionAnalytics.getInstance();
    private FlakinessAnalyzer flakinessAnalyzer = FlakinessAnalyzer.getInstance();

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();

        TestUtilities.logInfo("Starting test: " + className + "." + testName);

        // Record test start in analytics and add metadata (with error handling)
        try {
            Allure.addAttachment("Test Start Time", "text/plain",
                new java.util.Date().toString());
            Allure.addAttachment("Test Class", "text/plain", className);
            Allure.addAttachment("Test Method", "text/plain", testName);
            Allure.addAttachment("Environment", "text/plain",
                System.getProperty("environment", "local"));
            Allure.addAttachment("Browser", "text/plain",
                System.getProperty("browser", "chrome"));
        } catch (Exception e) {
            System.err.println("Warning: Failed to add Allure attachments on test start: " + e.getMessage());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();

        TestUtilities.logInfo("Test PASSED: " + testName + " (Duration: " + duration + "ms)");

        // Record in analytics
        analytics.recordTestResult(result);

        // Analyze for flakiness patterns
        flakinessAnalyzer.analyzeTestResult(result);

        // Add to Allure report (with error handling)
        try {
            Allure.addAttachment("Test Result", "text/plain", "PASSED");
            Allure.addAttachment("Execution Time", "text/plain", duration + " ms");
        } catch (Exception e) {
            System.err.println("Warning: Failed to add Allure attachments on test success: " + e.getMessage());
        }

        // Add performance metrics
        TestUtilities.logPerformanceMetrics(testName);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String errorMessage = result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown error";
        long duration = result.getEndMillis() - result.getStartMillis();

        TestUtilities.logError("Test FAILED: " + testName + " - " + errorMessage);

        // Record in analytics
        analytics.recordTestResult(result);

        // Analyze for flakiness patterns
        flakinessAnalyzer.analyzeTestResult(result);

        // Capture screenshot on failure
        TestUtilities.captureScreenshotOnFailure(testName);

        // Add failure details to Allure report (with null checks)
        try {
            Allure.addAttachment("Test Result", "text/plain", "FAILED");
            if (errorMessage != null) {
                Allure.addAttachment("Error Message", "text/plain", errorMessage);
            }
            Allure.addAttachment("Execution Time", "text/plain", duration + " ms");
            Allure.addAttachment("Failure Time", "text/plain", new java.util.Date().toString());

            // Add flakiness analysis if available
            double failureProbability = flakinessAnalyzer.predictFailureProbability(
                result.getTestClass().getName() + "." + testName);
            if (failureProbability > 0.5) {
                Allure.addAttachment("Flakiness Alert", "text/plain",
                    "High failure probability detected: " + String.format("%.1f%%", failureProbability * 100));
            }

            // Log stack trace
            String stackTrace = getStackTrace(result.getThrowable());
            if (stackTrace != null) {
                Allure.addAttachment("Stack Trace", "text/plain", stackTrace);
            }
        } catch (Exception e) {
            // Silently handle Allure attachment errors to prevent test framework crashes
            System.err.println("Warning: Failed to add Allure attachments: " + e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        TestUtilities.logInfo("Test SKIPPED: " + testName);

        // Record in analytics
        analytics.recordTestResult(result);

        // Analyze for flakiness patterns (skips can indicate issues too)
        flakinessAnalyzer.analyzeTestResult(result);

        // Add to Allure report (with error handling)
        try {
            Allure.addAttachment("Test Result", "text/plain", "SKIPPED");
            Allure.addAttachment("Skip Reason", "text/plain",
                result.getThrowable() != null ? result.getThrowable().getMessage() : "No reason provided");
        } catch (Exception e) {
            System.err.println("Warning: Failed to add Allure attachments on test skip: " + e.getMessage());
        }
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
