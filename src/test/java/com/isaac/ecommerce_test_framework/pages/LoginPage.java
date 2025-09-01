package com.isaac.ecommerce_test_framework.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object for SauceDemo Login Page
 */
public class LoginPage {

    // Page elements
    private final SelenideElement usernameField = $("#user-name");
    private final SelenideElement passwordField = $("#password");
    private final SelenideElement loginButton = $("#login-button");
    private final SelenideElement errorMessage = $("[data-test='error']");
    private final SelenideElement loginLogo = $(".login_logo");

    @Step("Verify login page is displayed")
    public void verifyLoginPageDisplayed() {
        loginLogo.shouldBe(visible);
        usernameField.shouldBe(visible);
        passwordField.shouldBe(visible);
        loginButton.shouldBe(visible);
    }

    @Step("Enter username: {username}")
    public void enterUsername(String username) {
        usernameField.setValue(username);
    }

    @Step("Enter password: {password}")
    public void enterPassword(String password) {
        passwordField.setValue(password);
    }

    @Step("Click login button")
    public void clickLoginButton() {
        loginButton.click();
    }

    @Step("Login with credentials - Username: {username}")
    public ProductsPage login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
        return new ProductsPage();
    }

    @Step("Get error message text")
    public String getErrorMessage() {
        return errorMessage.shouldBe(visible).getText();
    }

    @Step("Verify error message is displayed")
    public void verifyErrorMessageDisplayed() {
        errorMessage.shouldBe(visible);
    }

    @Step("Clear username field")
    public void clearUsername() {
        usernameField.clear();
    }

    @Step("Clear password field")
    public void clearPassword() {
        passwordField.clear();
    }
}
