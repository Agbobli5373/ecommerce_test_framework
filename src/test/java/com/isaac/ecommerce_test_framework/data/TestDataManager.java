package com.isaac.ecommerce_test_framework.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isaac.ecommerce_test_framework.config.ConfigManager;
import com.github.javafaker.Faker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Test Data Manager for handling test data from various sources
 * Supports JSON, CSV, and dynamic data generation
 */
public class TestDataManager {

    private static TestDataManager instance;
    private ObjectMapper objectMapper;
    private Faker faker;
    private Map<String, Object> testDataCache;

    private TestDataManager() {
        this.objectMapper = new ObjectMapper();
        this.faker = new Faker();
        this.testDataCache = new HashMap<>();
        loadTestData();
    }

    public static TestDataManager getInstance() {
        if (instance == null) {
            instance = new TestDataManager();
        }
        return instance;
    }

    private void loadTestData() {
        String dataPath = ConfigManager.getInstance().getTestDataPath();
        String dataSource = ConfigManager.getInstance().getTestDataSource();

        try {
            switch (dataSource.toLowerCase()) {
                case "json":
                    loadJsonData(dataPath);
                    break;
                case "csv":
                    loadCsvData(dataPath);
                    break;
                default:
                    System.out.println("Unsupported data source: " + dataSource + ". Using JSON as default.");
                    loadJsonData(dataPath);
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not load test data from " + dataPath + ". Using defaults.");
            loadDefaultData();
        }
    }

    private void loadJsonData(String dataPath) throws IOException {
        File dataDir = new File(dataPath);
        if (dataDir.exists() && dataDir.isDirectory()) {
            File[] jsonFiles = dataDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (jsonFiles != null) {
                for (File jsonFile : jsonFiles) {
                    String fileName = jsonFile.getName().replace(".json", "");
                    JsonNode jsonNode = objectMapper.readTree(jsonFile);
                    testDataCache.put(fileName, convertJsonNodeToMap(jsonNode));
                }
            }
        }
    }

    private void loadCsvData(String dataPath) {
        // CSV loading implementation can be added here
        System.out.println("CSV data loading not yet implemented. Using default data.");
        loadDefaultData();
    }

    private void loadDefaultData() {
        // Load default test data
        Map<String, Object> userData = new HashMap<>();
        userData.put("standard_user", Map.of(
            "username", "standard_user",
            "password", "secret_sauce",
            "expectedProducts", 6
        ));
        userData.put("locked_out_user", Map.of(
            "username", "locked_out_user",
            "password", "secret_sauce",
            "expectedError", "Sorry, this user has been locked out"
        ));
        userData.put("problem_user", Map.of(
            "username", "problem_user",
            "password", "secret_sauce",
            "hasVisualIssues", true
        ));

        testDataCache.put("users", userData);
        testDataCache.put("products", createDefaultProducts());
    }

    private Map<String, Object> createDefaultProducts() {
        Map<String, Object> products = new HashMap<>();
        products.put("backpack", Map.of(
            "name", "Sauce Labs Backpack",
            "price", 29.99,
            "description", "carry.allTheThings() with the sleek, streamlined Sly Pack"
        ));
        products.put("bike_light", Map.of(
            "name", "Sauce Labs Bike Light",
            "price", 9.99,
            "description", "A red light isn't the desired state in testing but it sure helps when riding"
        ));
        return products;
    }

    private Map<String, Object> convertJsonNodeToMap(JsonNode jsonNode) {
        return objectMapper.convertValue(jsonNode, Map.class);
    }

    // Get test data by key
    @SuppressWarnings("unchecked")
    public <T> T getTestData(String key) {
        return (T) testDataCache.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getTestData(String category, String key) {
        Map<String, Object> categoryData = (Map<String, Object>) testDataCache.get(category);
        if (categoryData != null) {
            return (T) categoryData.get(key);
        }
        return null;
    }

    // Get user credentials
    public Map<String, String> getUserCredentials(String userType) {
        return getTestData("users", userType);
    }

    public String getUsername(String userType) {
        Map<String, String> userData = getUserCredentials(userType);
        return userData != null ? userData.get("username") : null;
    }

    public String getPassword(String userType) {
        Map<String, String> userData = getUserCredentials(userType);
        return userData != null ? userData.get("password") : null;
    }

    // Get product data
    public Map<String, Object> getProductData(String productKey) {
        return getTestData("products", productKey);
    }

    // Dynamic data generation using Faker
    public String generateRandomEmail() {
        return faker.internet().emailAddress();
    }

    public String generateRandomName() {
        return faker.name().fullName();
    }

    public String generateRandomAddress() {
        return faker.address().fullAddress();
    }

    public String generateRandomPhoneNumber() {
        return faker.phoneNumber().phoneNumber();
    }

    public String generateRandomCreditCardNumber() {
        return faker.business().creditCardNumber();
    }

    public String generateRandomCompanyName() {
        return faker.company().name();
    }

    // Data provider methods for TestNG
    public static Object[][] getLoginTestData() {
        TestDataManager dataManager = getInstance();
        Map<String, Object> users = dataManager.getTestData("users");

        List<Object[]> testData = new ArrayList<>();
        for (Map.Entry<String, Object> entry : users.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, String> userData = (Map<String, String>) entry.getValue();
            testData.add(new Object[]{
                userData.get("username"),
                userData.get("password"),
                userData.getOrDefault("expectedError", ""),
                Boolean.parseBoolean(userData.getOrDefault("shouldSucceed", "true"))
            });
        }

        return testData.toArray(new Object[0][]);
    }

    public static Object[][] getProductTestData() {
        TestDataManager dataManager = getInstance();
        Map<String, Object> products = dataManager.getTestData("products");

        List<Object[]> testData = new ArrayList<>();
        for (Map.Entry<String, Object> entry : products.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> productData = (Map<String, Object>) entry.getValue();
            testData.add(new Object[]{
                productData.get("name"),
                productData.get("price"),
                productData.get("description")
            });
        }

        return testData.toArray(new Object[0][]);
    }

    // Reload test data (useful for dynamic data updates)
    public void reloadTestData() {
        testDataCache.clear();
        loadTestData();
    }

    // Add custom test data programmatically
    public void addTestData(String key, Object data) {
        testDataCache.put(key, data);
    }

    // Get all test data keys
    public Set<String> getTestDataKeys() {
        return testDataCache.keySet();
    }
}
