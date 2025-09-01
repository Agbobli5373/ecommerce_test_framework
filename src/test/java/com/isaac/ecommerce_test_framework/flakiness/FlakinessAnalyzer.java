package com.isaac.ecommerce_test_framework.flakiness;

import com.isaac.ecommerce_test_framework.analytics.TestExecutionAnalytics;
import com.isaac.ecommerce_test_framework.config.ConfigManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.testng.ITestResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Advanced flakiness analyzer for detecting and managing unstable tests
 */
public class FlakinessAnalyzer {

    // Inner classes for data structures
    public static class FlakinessPattern {
        public final String testName;
        public double flakinessScore;
        public FlakinessLevel flakinessLevel;
        public int consecutiveFailures;
        public int consecutiveSuccesses;
        public long lastFailureTime;
        public long lastAnalyzed;
        public long lastUpdated;
        public int totalAnalyzed;
        public final List<String> timePatterns;
        public final List<String> environmentPatterns;
        public final Map<String, Integer> environmentFailures;

        public FlakinessPattern(String testName) {
            this.testName = testName;
            this.flakinessScore = 0.0;
            this.flakinessLevel = FlakinessLevel.STABLE;
            this.consecutiveFailures = 0;
            this.consecutiveSuccesses = 0;
            this.lastFailureTime = 0;
            this.lastAnalyzed = System.currentTimeMillis();
            this.lastUpdated = System.currentTimeMillis();
            this.totalAnalyzed = 0;
            this.timePatterns = new ArrayList<>();
            this.environmentPatterns = new ArrayList<>();
            this.environmentFailures = new HashMap<>();
        }
    }

    public enum FlakinessLevel {
        STABLE("Stable"),
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        CRITICAL("Critical");

        private final String displayName;

        FlakinessLevel(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static FlakinessAnalyzer instance;
    private final TestExecutionAnalytics analytics;
    private final ConfigManager config;
    private final Map<String, FlakinessPattern> flakinessPatterns;
    private final Map<String, List<Long>> failureTimestamps;

    private FlakinessAnalyzer() {
        this.analytics = TestExecutionAnalytics.getInstance();
        this.config = ConfigManager.getInstance();
        this.flakinessPatterns = new ConcurrentHashMap<>();
        this.failureTimestamps = new ConcurrentHashMap<>();
        loadFlakinessData();
    }

    public static FlakinessAnalyzer getInstance() {
        if (instance == null) {
            instance = new FlakinessAnalyzer();
        }
        return instance;
    }

    /**
     * Analyze test result for flakiness patterns
     */
    public void analyzeTestResult(ITestResult result) {
        String testName = getTestName(result);
        String className = result.getTestClass().getName();
        String fullTestName = className + "." + testName;

        // Record failure timestamp for pattern analysis
        if (result.getStatus() == ITestResult.FAILURE) {
            failureTimestamps.computeIfAbsent(fullTestName, k -> new ArrayList<>())
                .add(result.getEndMillis());
        }

        // Analyze flakiness patterns
        analyzeFlakinessPatterns(fullTestName, result);

        // Update flakiness metrics
        updateFlakinessMetrics(fullTestName, result);
    }

    /**
     * Analyze flakiness patterns for a test
     */
    private void analyzeFlakinessPatterns(String testName, ITestResult result) {
        FlakinessPattern pattern = flakinessPatterns.computeIfAbsent(testName, k -> new FlakinessPattern(testName));

        // Analyze failure patterns
        if (result.getStatus() == ITestResult.FAILURE) {
            pattern.consecutiveFailures++;
            pattern.lastFailureTime = result.getEndMillis();

            // Check for time-based patterns
            analyzeTimeBasedPatterns(pattern, result);

            // Check for environment-specific failures
            analyzeEnvironmentPatterns(pattern, result);

        } else if (result.getStatus() == ITestResult.SUCCESS) {
            if (pattern.consecutiveFailures > 0) {
                pattern.consecutiveSuccesses++;
                // Reset consecutive failures after a success
                if (pattern.consecutiveSuccesses >= 2) {
                    pattern.consecutiveFailures = 0;
                }
            }
        }

        // Calculate flakiness score
        pattern.flakinessScore = calculateFlakinessScore(pattern, testName);
        pattern.lastAnalyzed = System.currentTimeMillis();
    }

    /**
     * Analyze time-based failure patterns
     */
    private void analyzeTimeBasedPatterns(FlakinessPattern pattern, ITestResult result) {
        long currentTime = result.getEndMillis();
        List<Long> timestamps = failureTimestamps.get(pattern.testName);

        if (timestamps != null && timestamps.size() >= 3) {
            // Check for daily patterns
            long dayInMillis = 24 * 60 * 60 * 1000;
            long recentFailures = timestamps.stream()
                .filter(timestamp -> (currentTime - timestamp) < dayInMillis)
                .count();

            if (recentFailures >= 3) {
                pattern.timePatterns.add("Daily recurrence detected");
            }

            // Check for hourly patterns
            long hourInMillis = 60 * 60 * 1000;
            long hourlyFailures = timestamps.stream()
                .filter(timestamp -> (currentTime - timestamp) < hourInMillis)
                .count();

            if (hourlyFailures >= 2) {
                pattern.timePatterns.add("Hourly recurrence detected");
            }
        }
    }

    /**
     * Analyze environment-specific failure patterns
     */
    private void analyzeEnvironmentPatterns(FlakinessPattern pattern, ITestResult result) {
        String environment = System.getProperty("environment", "local");
        String browser = System.getProperty("browser", "chrome");

        // Track failures by environment
        String envKey = environment + "_" + browser;
        pattern.environmentFailures.merge(envKey, 1, Integer::sum);

        // Detect environment-specific issues
        int envFailureCount = pattern.environmentFailures.getOrDefault(envKey, 0);
        var metrics = analytics.getTestMetrics(pattern.testName);
        if (metrics != null && metrics.totalExecutions >= 5) {
            double envFailureRate = (double) envFailureCount / metrics.totalExecutions;
            if (envFailureRate > 0.5) {
                pattern.environmentPatterns.add("High failure rate in " + envKey + " environment");
            }
        }
    }

    /**
     * Calculate comprehensive flakiness score
     */
    private double calculateFlakinessScore(FlakinessPattern pattern, String testName) {
        var metrics = analytics.getTestMetrics(testName);
        if (metrics == null || metrics.totalExecutions < 3) {
            return 0.0;
        }

        double baseScore = 0.0;

        // Success rate component (lower success rate = higher flakiness)
        double successRate = metrics.successRate / 100.0;
        baseScore += (1.0 - successRate) * 40.0;

        // Consecutive failures component
        if (pattern.consecutiveFailures > 0) {
            baseScore += Math.min(pattern.consecutiveFailures * 10.0, 30.0);
        }

        // Time pattern component
        baseScore += pattern.timePatterns.size() * 10.0;

        // Environment pattern component
        baseScore += pattern.environmentPatterns.size() * 15.0;

        // Variance in execution time (higher variance = potentially more flakiness)
        if (metrics.executionTimes.size() > 3) {
            double avgTime = metrics.averageExecutionTime;
            double variance = metrics.executionTimes.stream()
                .mapToDouble(time -> Math.pow(time - avgTime, 2))
                .average()
                .orElse(0.0);

            double coefficientOfVariation = Math.sqrt(variance) / avgTime;
            baseScore += Math.min(coefficientOfVariation * 20.0, 20.0);
        }

        return Math.min(baseScore, 100.0);
    }

    /**
     * Update flakiness metrics
     */
    private void updateFlakinessMetrics(String testName, ITestResult result) {
        FlakinessPattern pattern = flakinessPatterns.get(testName);
        if (pattern != null) {
            pattern.totalAnalyzed++;
            pattern.lastUpdated = System.currentTimeMillis();

            // Determine flakiness level
            pattern.flakinessLevel = determineFlakinessLevel(pattern.flakinessScore);
        }
    }

    /**
     * Determine flakiness level based on score
     */
    private FlakinessLevel determineFlakinessLevel(double score) {
        if (score >= 70.0) {
            return FlakinessLevel.CRITICAL;
        } else if (score >= 50.0) {
            return FlakinessLevel.HIGH;
        } else if (score >= 30.0) {
            return FlakinessLevel.MEDIUM;
        } else if (score >= 15.0) {
            return FlakinessLevel.LOW;
        } else {
            return FlakinessLevel.STABLE;
        }
    }

    /**
     * Get flakiness report for all tests
     */
    @Attachment(value = "Flakiness Analysis Report", type = "text/html")
    public String generateFlakinessReport() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Flakiness Analysis Report</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;}");
        html.append("table{border-collapse:collapse;width:100%;margin:10px 0;}");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;}");
        html.append("th{background-color:#f2f2f2;}");
        html.append(".critical{background-color:#dc3545;color:white;}");
        html.append(".high{background-color:#fd7e14;color:white;}");
        html.append(".medium{background-color:#ffc107;}");
        html.append(".low{background-color:#28a745;color:white;}");
        html.append(".stable{background-color:#6c757d;color:white;}");
        html.append("</style></head><body>");

        html.append("<h1>Test Flakiness Analysis Report</h1>");
        html.append("<p><strong>Generated:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</p>");

        // Summary statistics
        Map<FlakinessLevel, Long> levelCount = flakinessPatterns.values().stream()
            .collect(Collectors.groupingBy(p -> p.flakinessLevel, Collectors.counting()));

        html.append("<h2>Flakiness Summary</h2>");
        html.append("<ul>");
        for (FlakinessLevel level : FlakinessLevel.values()) {
            long count = levelCount.getOrDefault(level, 0L);
            html.append("<li><strong>").append(level).append(":</strong> ").append(count).append(" tests</li>");
        }
        html.append("</ul>");

        // Detailed flakiness table
        html.append("<h2>Detailed Flakiness Analysis</h2>");
        html.append("<table>");
        html.append("<tr><th>Test Name</th><th>Flakiness Score</th><th>Level</th><th>Consecutive Failures</th><th>Patterns Detected</th><th>Last Analyzed</th></tr>");

        flakinessPatterns.values().stream()
            .sorted((a, b) -> Double.compare(b.flakinessScore, a.flakinessScore))
            .forEach(pattern -> {
                String cssClass = getFlakinessCssClass(pattern.flakinessLevel);
                html.append("<tr class='").append(cssClass).append("'>");
                html.append("<td>").append(pattern.testName).append("</td>");
                html.append("<td>").append(String.format("%.1f", pattern.flakinessScore)).append("</td>");
                html.append("<td>").append(pattern.flakinessLevel).append("</td>");
                html.append("<td>").append(pattern.consecutiveFailures).append("</td>");
                html.append("<td>").append(getPatternsSummary(pattern)).append("</td>");
                html.append("<td>").append(new Date(pattern.lastAnalyzed)).append("</td>");
                html.append("</tr>");
            });

        html.append("</table></body></html>");

        return html.toString();
    }

    /**
     * Get CSS class for flakiness level visualization
     */
    private String getFlakinessCssClass(FlakinessLevel level) {
        switch (level) {
            case CRITICAL:
                return "critical";
            case HIGH:
                return "high";
            case MEDIUM:
                return "medium";
            case LOW:
                return "low";
            case STABLE:
                return "stable";
            default:
                return "";
        }
    }

    /**
     * Get summary of detected patterns
     */
    private String getPatternsSummary(FlakinessPattern pattern) {
        List<String> patterns = new ArrayList<>();
        patterns.addAll(pattern.timePatterns);
        patterns.addAll(pattern.environmentPatterns);

        if (pattern.consecutiveFailures > 2) {
            patterns.add("Consecutive failures: " + pattern.consecutiveFailures);
        }

        return patterns.isEmpty() ? "No patterns detected" : String.join(", ", patterns);
    }

    /**
     * Get tests that should be quarantined
     */
    public List<String> getTestsForQuarantine() {
        return flakinessPatterns.entrySet().stream()
            .filter(entry -> entry.getValue().flakinessLevel == FlakinessLevel.CRITICAL ||
                           entry.getValue().flakinessLevel == FlakinessLevel.HIGH)
            .filter(entry -> entry.getValue().consecutiveFailures >= 3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Get tests that need investigation
     */
    public List<String> getTestsNeedingInvestigation() {
        return flakinessPatterns.entrySet().stream()
            .filter(entry -> entry.getValue().flakinessScore > 40.0)
            .filter(entry -> !entry.getValue().timePatterns.isEmpty() ||
                           !entry.getValue().environmentPatterns.isEmpty())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Predict if a test is likely to fail
     */
    public double predictFailureProbability(String testName) {
        FlakinessPattern pattern = flakinessPatterns.get(testName);
        if (pattern == null) {
            return 0.0;
        }

        // Base probability from historical data
        var metrics = analytics.getTestMetrics(testName);
        double baseProbability = metrics != null ?
            (double) metrics.failureCount / metrics.totalExecutions : 0.0;

        // Adjust based on current patterns
        double adjustmentFactor = 1.0;

        if (pattern.consecutiveFailures > 0) {
            adjustmentFactor += pattern.consecutiveFailures * 0.1;
        }

        if (!pattern.timePatterns.isEmpty()) {
            adjustmentFactor += 0.2;
        }

        if (!pattern.environmentPatterns.isEmpty()) {
            adjustmentFactor += 0.15;
        }

        return Math.min(baseProbability * adjustmentFactor, 1.0);
    }

    /**
     * Get flakiness recommendations
     */
    public List<String> getFlakinessRecommendations() {
        List<String> recommendations = new ArrayList<>();

        // Tests needing quarantine
        List<String> quarantineTests = getTestsForQuarantine();
        if (!quarantineTests.isEmpty()) {
            recommendations.add("🚨 CRITICAL: Quarantine these tests (" + quarantineTests.size() + "): " +
                              String.join(", ", quarantineTests));
        }

        // Tests needing investigation
        List<String> investigationTests = getTestsNeedingInvestigation();
        if (!investigationTests.isEmpty()) {
            recommendations.add("🔍 INVESTIGATE: These tests show suspicious patterns (" +
                              investigationTests.size() + "): " + String.join(", ", investigationTests));
        }

        // Environment-specific issues
        Map<String, List<String>> envIssues = new HashMap<>();
        for (FlakinessPattern pattern : flakinessPatterns.values()) {
            for (String envPattern : pattern.environmentPatterns) {
                envIssues.computeIfAbsent(envPattern, k -> new ArrayList<>()).add(pattern.testName);
            }
        }

        for (Map.Entry<String, List<String>> entry : envIssues.entrySet()) {
            recommendations.add("🌍 ENVIRONMENT: " + entry.getKey() + " affects " +
                              entry.getValue().size() + " tests: " + String.join(", ", entry.getValue()));
        }

        return recommendations;
    }

    /**
     * Extract test name from ITestResult
     */
    private String getTestName(ITestResult result) {
        return result.getMethod().getMethodName();
    }

    /**
     * Load flakiness data from file
     */
    private void loadFlakinessData() {
        // Implementation for loading historical flakiness data
        // This would typically load from a JSON file similar to analytics
    }

    /**
     * Save flakiness data to file
     */
    private void saveFlakinessData() {
        // Implementation for saving flakiness data
        // This would typically save to a JSON file
    }

    /**
     * Get flakiness pattern for a test
     */
    public FlakinessPattern getFlakinessPattern(String testName) {
        return flakinessPatterns.get(testName);
    }

    /**
     * Get all flakiness patterns
     */
    public Map<String, FlakinessPattern> getAllFlakinessPatterns() {
        return new HashMap<>(flakinessPatterns);
    }
}
