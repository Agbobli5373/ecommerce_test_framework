package com.isaac.ecommerce_test_framework.tests;

import com.isaac.ecommerce_test_framework.BaseTest;
import com.isaac.ecommerce_test_framework.pages.LoginPage;
import com.isaac.ecommerce_test_framework.pages.ProductsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for SauceDemo login functionality
 */
@Epic("Authentication")
@Feature("Login")
public class LoginTests extends BaseTest {

    @Test
    @Description("Verify successful login with valid credentials")
    @Story("Successful Login")
    public void testSuccessfulLogin() {
        LoginPage loginPage = new LoginPage();
        loginPage.verifyLoginPageDisplayed();

        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");
        productsPage.verifyProductsPageDisplayed();

        Assert.assertEquals(productsPage.getProductCount(), 6, "Should display 6 products");
    }

    @Test
    @Description("Verify login fails with invalid username")
    @Story("Login Validation")
    public void testInvalidUsername() {
        LoginPage loginPage = new LoginPage();
        loginPage.verifyLoginPageDisplayed();

        loginPage.login("invalid_user", "secret_sauce");
        loginPage.verifyErrorMessageDisplayed();

        String errorMessage = loginPage.getErrorMessage();
        Assert.assertTrue(errorMessage.contains("Username and password do not match"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Description("Verify login fails with invalid password")
    @Story("Login Validation")
    public void testInvalidPassword() {
        LoginPage loginPage = new LoginPage();
        loginPage.verifyLoginPageDisplayed();

        loginPage.login("standard_user", "wrong_password");
        loginPage.verifyErrorMessageDisplayed();

        String errorMessage = loginPage.getErrorMessage();
        Assert.assertTrue(errorMessage.contains("Username and password do not match"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Description("Verify login fails with empty username")
    @Story("Login Validation")
    public void testEmptyUsername() {
        LoginPage loginPage = new LoginPage();
        loginPage.verifyLoginPageDisplayed();

        loginPage.enterUsername("");
        loginPage.enterPassword("secret_sauce");
        loginPage.clickLoginButton();

        loginPage.verifyErrorMessageDisplayed();
        String errorMessage = loginPage.getErrorMessage();
        Assert.assertTrue(errorMessage.contains("Username is required"),
                "Error message should indicate username is required");
    }

    @Test
    @Description("Verify login fails with empty password")
    @Story("Login Validation")
    public void testEmptyPassword() {
        LoginPage loginPage = new LoginPage();
        loginPage.verifyLoginPageDisplayed();

        loginPage.enterUsername("standard_user");
        loginPage.enterPassword("");
        loginPage.clickLoginButton();

        loginPage.verifyErrorMessageDisplayed();
        String errorMessage = loginPage.getErrorMessage();
        Assert.assertTrue(errorMessage.contains("Password is required"),
                "Error message should indicate password is required");
    }

    @Test
    @Description("Verify login fails with both fields empty")
    @Story("Login Validation")
    public void testEmptyCredentials() {
        LoginPage loginPage = new LoginPage();
        loginPage.verifyLoginPageDisplayed();

        loginPage.enterUsername("");
        loginPage.enterPassword("");
        loginPage.clickLoginButton();

        loginPage.verifyErrorMessageDisplayed();
        String errorMessage = loginPage.getErrorMessage();
        Assert.assertTrue(errorMessage.contains("Username is required"),
                "Error message should indicate username is required");
    }

    @Test
    @Description("Verify login with locked out user")
    @Story("Account Lockout")
    public void testLockedOutUser() {
        LoginPage loginPage = new LoginPage();
        loginPage.verifyLoginPageDisplayed();

        loginPage.login("locked_out_user", "secret_sauce");
        loginPage.verifyErrorMessageDisplayed();

        String errorMessage = loginPage.getErrorMessage();
        Assert.assertTrue(errorMessage.contains("Sorry, this user has been locked out"),
                "Error message should indicate user is locked out");
    }

    @Test
    @Description("Verify login with problem user (visual issues)")
    @Story("User Types")
    public void testProblemUserLogin() {
        LoginPage loginPage = new LoginPage();
        loginPage.verifyLoginPageDisplayed();

        ProductsPage productsPage = loginPage.login("problem_user", "secret_sauce");
        productsPage.verifyProductsPageDisplayed();

        // Problem user can login but has visual issues on products page
        Assert.assertTrue(productsPage.getProductCount() > 0, "Should display products even with visual issues");
    }

    @Test
    @Description("Verify login with performance glitch user")
    @Story("User Types")
    public void testPerformanceGlitchUserLogin() {
        LoginPage loginPage = new LoginPage();
        loginPage.verifyLoginPageDisplayed();

        ProductsPage productsPage = loginPage.login("performance_glitch_user", "secret_sauce");
        productsPage.verifyProductsPageDisplayed();

        Assert.assertEquals(productsPage.getProductCount(), 6, "Should display 6 products");
    }
}
