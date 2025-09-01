package com.isaac.ecommerce_test_framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for test framework settings
 * Supports environment-specific configurations and secure property management
 */
public class ConfigManager {

    private static ConfigManager instance;
    private Properties properties;

    private ConfigManager() {
        loadConfiguration();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfiguration() {
        properties = new Properties();

        // Load default properties
        loadPropertiesFromFile("config/default.properties");

        // Load environment-specific properties (overrides defaults)
        String environment = System.getProperty("environment", "local");
        loadPropertiesFromFile("config/" + environment + ".properties");

        // Load system properties (highest priority)
        properties.putAll(System.getProperties());
    }

    private void loadPropertiesFromFile(String fileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not load " + fileName + ". Using defaults.");
        }
    }

    // Browser Configuration
    public String getBrowser() {
        return properties.getProperty("browser", "chrome");
    }

    public String getBrowserVersion() {
        return properties.getProperty("browser.version", "latest");
    }

    public String getBrowserSize() {
        return properties.getProperty("browser.size", "1920x1080");
    }

    public boolean isHeadless() {
        return Boolean.parseBoolean(properties.getProperty("browser.headless", "true"));
    }

    public int getTimeout() {
        return Integer.parseInt(properties.getProperty("timeout", "10000"));
    }

    // Application Configuration
    public String getBaseUrl() {
        return properties.getProperty("app.base.url", "https://www.saucedemo.com");
    }

    // Test Execution Configuration
    public boolean isParallelExecution() {
        return Boolean.parseBoolean(properties.getProperty("execution.parallel", "true"));
    }

    public String getParallelMode() {
        return properties.getProperty("execution.parallel.mode", "methods");
    }

    public int getThreadCount() {
        return Integer.parseInt(properties.getProperty("execution.thread.count", "5"));
    }

    // Reporting Configuration
    public boolean isAllureEnabled() {
        return Boolean.parseBoolean(properties.getProperty("reporting.allure.enabled", "true"));
    }

    public boolean isScreenshotOnFailure() {
        return Boolean.parseBoolean(properties.getProperty("reporting.screenshot.on.failure", "true"));
    }

    public boolean isVideoRecordingEnabled() {
        return Boolean.parseBoolean(properties.getProperty("reporting.video.enabled", "false"));
    }

    // Remote Execution Configuration
    public boolean isRemoteExecution() {
        return Boolean.parseBoolean(properties.getProperty("remote.enabled", "false"));
    }

    public String getRemoteUrl() {
        return properties.getProperty("remote.url", "http://localhost:4444/wd/hub");
    }

    // Retry Configuration
    public boolean isRetryEnabled() {
        return Boolean.parseBoolean(properties.getProperty("retry.enabled", "true"));
    }

    public int getRetryCount() {
        return Integer.parseInt(properties.getProperty("retry.count", "2"));
    }

    // Test Data Configuration
    public String getTestDataSource() {
        return properties.getProperty("testdata.source", "json");
    }

    public String getTestDataPath() {
        return properties.getProperty("testdata.path", "src/test/resources/testdata");
    }

    // Security Configuration
    public boolean isSecureCredentials() {
        return Boolean.parseBoolean(properties.getProperty("security.credentials.encrypted", "false"));
    }

    public String getCredentialsKey() {
        return properties.getProperty("security.credentials.key", "");
    }

    // Generic property getter
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    // Reload configuration (useful for dynamic config changes)
    public void reloadConfiguration() {
        loadConfiguration();
    }
}
