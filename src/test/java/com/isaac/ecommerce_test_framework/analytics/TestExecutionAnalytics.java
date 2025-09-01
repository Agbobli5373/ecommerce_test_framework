package com.isaac.ecommerce_test_framework.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.isaac.ecommerce_test_framework.config.ConfigManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Advanced analytics for test execution tracking and trend analysis
 */
public class TestExecutionAnalytics {

    // Inner classes for data structures
    public static class TestExecutionRecord {
        public final String testName;
        public final int status;
        public final long startTime;
        public final long endTime;
        public final String errorMessage;
        public final String category;
        public final String priority;

        public TestExecutionRecord(String testName, int status, long startTime, long endTime,
                                 String errorMessage, String category, String priority) {
            this.testName = testName;
            this.status = status;
            this.startTime = startTime;
            this.endTime = endTime;
            this.errorMessage = errorMessage;
            this.category = category;
            this.priority = priority;
        }
    }

    public static class TestMetrics {
        public final String testName;
        public int totalExecutions;
        public int successCount;
        public int failureCount;
        public int skipCount;
        public double successRate;
        public double averageExecutionTime;
        public long lastExecutionTime;
        public TestStability stability;
        public final List<Long> executionTimes;

        public TestMetrics(String testName) {
            this.testName = testName;
            this.executionTimes = new ArrayList<>();
            this.stability = TestStability.INSUFFICIENT_DATA;
        }
    }

    public enum TestStability {
        INSUFFICIENT_DATA("Insufficient Data"),
        STABLE("Stable"),
        MOSTLY_STABLE("Mostly Stable"),
        UNSTABLE("Unstable"),
        FLAKY("Flaky");

        private final String displayName;

        TestStability(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static TestExecutionAnalytics instance;
    private final ObjectMapper objectMapper;
    private final Map<String, TestExecutionRecord> executionHistory;
    private final Map<String, TestMetrics> testMetrics;
    private final ConfigManager config;

    private TestExecutionAnalytics() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.executionHistory = new ConcurrentHashMap<>();
        this.testMetrics = new ConcurrentHashMap<>();
        this.config = ConfigManager.getInstance();
        loadHistoricalData();
    }

    public static TestExecutionAnalytics getInstance() {
        if (instance == null) {
            instance = new TestExecutionAnalytics();
        }
        return instance;
    }

    /**
     * Record test execution result
     */
    public void recordTestResult(ITestResult result) {
        String testName = getTestName(result);
        String className = result.getTestClass().getName();
        String fullTestName = className + "." + testName;

        TestExecutionRecord record = new TestExecutionRecord(
            fullTestName,
            result.getStatus(),
            result.getStartMillis(),
            result.getEndMillis(),
            result.getThrowable() != null ? result.getThrowable().getMessage() : null,
            getTestCategory(result),
            getTestPriority(result)
        );

        executionHistory.put(fullTestName + "_" + System.currentTimeMillis(), record);
        updateTestMetrics(fullTestName, record);

        // Save to file periodically
        if (executionHistory.size() % 10 == 0) {
            saveHistoricalData();
        }
    }

    /**
     * Update test metrics with new execution data
     */
    private void updateTestMetrics(String testName, TestExecutionRecord record) {
        TestMetrics metrics = testMetrics.computeIfAbsent(testName, k -> new TestMetrics(testName));

        metrics.totalExecutions++;
        metrics.lastExecutionTime = record.endTime;

        switch (record.status) {
            case ITestResult.SUCCESS:
                metrics.successCount++;
                break;
            case ITestResult.FAILURE:
                metrics.failureCount++;
                break;
            case ITestResult.SKIP:
                metrics.skipCount++;
                break;
        }

        // Calculate execution time statistics
        long executionTime = record.endTime - record.startTime;
        metrics.executionTimes.add(executionTime);
        metrics.averageExecutionTime = metrics.executionTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);

        // Calculate success rate
        metrics.successRate = (double) metrics.successCount / metrics.totalExecutions * 100.0;

        // Determine stability classification
        metrics.stability = calculateStability(metrics);
    }

    /**
     * Calculate test stability based on execution history
     */
    private TestStability calculateStability(TestMetrics metrics) {
        if (metrics.totalExecutions < 5) {
            return TestStability.INSUFFICIENT_DATA;
        }

        double successRate = metrics.successRate;

        if (successRate >= 95.0) {
            return TestStability.STABLE;
        } else if (successRate >= 80.0) {
            return TestStability.MOSTLY_STABLE;
        } else if (successRate >= 60.0) {
            return TestStability.UNSTABLE;
        } else {
            return TestStability.FLAKY;
        }
    }

    /**
     * Generate execution summary report
     */
    @Attachment(value = "Test Execution Summary", type = "text/html")
    public String generateExecutionSummary() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Test Execution Summary</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;}");
        html.append("table{border-collapse:collapse;width:100%;}");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;}");
        html.append("th{background-color:#f2f2f2;}");
        html.append(".stable{background-color:#d4edda;}.unstable{background-color:#f8d7da;}");
        html.append(".flaky{background-color:#fff3cd;}</style></head><body>");

        html.append("<h1>Test Execution Analytics Summary</h1>");
        html.append("<p><strong>Generated:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</p>");
        html.append("<p><strong>Total Tests:</strong> ").append(testMetrics.size()).append("</p>");
        html.append("<p><strong>Total Executions:</strong> ").append(executionHistory.size()).append("</p>");

        // Summary statistics
        long totalExecutions = testMetrics.values().stream().mapToLong(m -> m.totalExecutions).sum();
        long totalSuccess = testMetrics.values().stream().mapToLong(m -> m.successCount).sum();
        long totalFailures = testMetrics.values().stream().mapToLong(m -> m.failureCount).sum();
        long totalSkips = testMetrics.values().stream().mapToLong(m -> m.skipCount).sum();

        html.append("<h2>Overall Statistics</h2>");
        html.append("<ul>");
        html.append("<li><strong>Success Rate:</strong> ").append(String.format("%.2f%%", (double) totalSuccess / totalExecutions * 100)).append("</li>");
        html.append("<li><strong>Total Success:</strong> ").append(totalSuccess).append("</li>");
        html.append("<li><strong>Total Failures:</strong> ").append(totalFailures).append("</li>");
        html.append("<li><strong>Total Skips:</strong> ").append(totalSkips).append("</li>");
        html.append("</ul>");

        // Test stability breakdown
        Map<TestStability, Long> stabilityCount = testMetrics.values().stream()
            .collect(Collectors.groupingBy(m -> m.stability, Collectors.counting()));

        html.append("<h2>Test Stability Breakdown</h2>");
        html.append("<ul>");
        for (TestStability stability : TestStability.values()) {
            long count = stabilityCount.getOrDefault(stability, 0L);
            html.append("<li><strong>").append(stability).append(":</strong> ").append(count).append(" tests</li>");
        }
        html.append("</ul>");

        // Detailed test metrics table
        html.append("<h2>Detailed Test Metrics</h2>");
        html.append("<table>");
        html.append("<tr><th>Test Name</th><th>Total Runs</th><th>Success Rate</th><th>Avg Execution Time</th><th>Stability</th><th>Last Run</th></tr>");

        testMetrics.values().stream()
            .sorted((a, b) -> Double.compare(b.successRate, a.successRate))
            .forEach(metrics -> {
                String cssClass = getStabilityCssClass(metrics.stability);
                html.append("<tr class='").append(cssClass).append("'>");
                html.append("<td>").append(metrics.testName).append("</td>");
                html.append("<td>").append(metrics.totalExecutions).append("</td>");
                html.append("<td>").append(String.format("%.1f%%", metrics.successRate)).append("</td>");
                html.append("<td>").append(String.format("%.2f ms", metrics.averageExecutionTime)).append("</td>");
                html.append("<td>").append(metrics.stability).append("</td>");
                html.append("<td>").append(new Date(metrics.lastExecutionTime)).append("</td>");
                html.append("</tr>");
            });

        html.append("</table></body></html>");

        return html.toString();
    }

    /**
     * Get CSS class for stability visualization
     */
    private String getStabilityCssClass(TestStability stability) {
        switch (stability) {
            case STABLE:
                return "stable";
            case MOSTLY_STABLE:
                return "stable";
            case UNSTABLE:
                return "unstable";
            case FLAKY:
                return "flaky";
            default:
                return "";
        }
    }

    /**
     * Get tests by stability classification
     */
    public List<String> getTestsByStability(TestStability stability) {
        return testMetrics.entrySet().stream()
            .filter(entry -> entry.getValue().stability == stability)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Get slowest tests
     */
    public List<String> getSlowestTests(int limit) {
        return testMetrics.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue().averageExecutionTime, a.getValue().averageExecutionTime))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Get most failing tests
     */
    public List<String> getMostFailingTests(int limit) {
        return testMetrics.entrySet().stream()
            .filter(entry -> entry.getValue().totalExecutions >= 3) // Only consider tests with sufficient runs
            .sorted((a, b) -> Double.compare(
                (double) a.getValue().failureCount / a.getValue().totalExecutions,
                (double) b.getValue().failureCount / b.getValue().totalExecutions))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Extract test name from ITestResult
     */
    private String getTestName(ITestResult result) {
        return result.getMethod().getMethodName();
    }

    /**
     * Extract test category from annotations or method name
     */
    private String getTestCategory(ITestResult result) {
        // Check for Allure annotations or other categorization
        // For now, categorize based on method name patterns
        String methodName = result.getMethod().getMethodName().toLowerCase();

        if (methodName.contains("login")) return "Authentication";
        if (methodName.contains("product")) return "Product Management";
        if (methodName.contains("cart")) return "Shopping Cart";
        if (methodName.contains("checkout")) return "Checkout Process";
        if (methodName.contains("smoke")) return "Smoke Tests";
        if (methodName.contains("regression")) return "Regression Tests";

        return "General";
    }

    /**
     * Extract test priority from annotations
     */
    private String getTestPriority(ITestResult result) {
        // Check for TestNG priority or custom annotations
        // Default to medium priority
        return "Medium";
    }

    /**
     * Load historical data from file
     */
    private void loadHistoricalData() {
        try {
            Path analyticsPath = Paths.get("target", "test-analytics");
            if (!Files.exists(analyticsPath)) {
                Files.createDirectories(analyticsPath);
                return;
            }

            File metricsFile = analyticsPath.resolve("test-metrics.json").toFile();
            if (metricsFile.exists()) {
                Map<String, TestMetrics> loadedMetrics = objectMapper.readValue(metricsFile,
                    objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, TestMetrics.class));
                testMetrics.putAll(loadedMetrics);
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not load historical analytics data: " + e.getMessage());
        }
    }

    /**
     * Save historical data to file
     */
    private void saveHistoricalData() {
        try {
            Path analyticsPath = Paths.get("target", "test-analytics");
            Files.createDirectories(analyticsPath);

            File metricsFile = analyticsPath.resolve("test-metrics.json").toFile();
            objectMapper.writeValue(metricsFile, testMetrics);

        } catch (IOException e) {
            System.out.println("Warning: Could not save analytics data: " + e.getMessage());
        }
    }

    /**
     * Get all test metrics
     */
    public Map<String, TestMetrics> getAllTestMetrics() {
        return new HashMap<>(testMetrics);
    }

    /**
     * Get metrics for specific test
     */
    public TestMetrics getTestMetrics(String testName) {
        return testMetrics.get(testName);
    }

    /**
     * Clear all analytics data
     */
    public void clearAnalytics() {
        executionHistory.clear();
        testMetrics.clear();
        saveHistoricalData();
    }
}