package com.isaac.ecommerce_test_framework.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object for SauceDemo Checkout Page
 */
public class CheckoutPage {

    // Page elements
    private final SelenideElement checkoutTitle = $(".title");
    private final SelenideElement firstNameField = $("#first-name");
    private final SelenideElement lastNameField = $("#last-name");
    private final SelenideElement postalCodeField = $("#postal-code");
    private final SelenideElement continueButton = $("#continue");
    private final SelenideElement cancelButton = $("#cancel");
    private final SelenideElement errorMessage = $("[data-test='error']");

    @Step("Verify checkout page is displayed")
    public void verifyCheckoutPageDisplayed() {
        checkoutTitle.shouldHave(text("Checkout: Your Information"));
    }

    @Step("Enter first name: {firstName}")
    public void enterFirstName(String firstName) {
        firstNameField.setValue(firstName);
    }

    @Step("Enter last name: {lastName}")
    public void enterLastName(String lastName) {
        lastNameField.setValue(lastName);
    }

    @Step("Enter postal code: {postalCode}")
    public void enterPostalCode(String postalCode) {
        postalCodeField.setValue(postalCode);
    }

    @Step("Fill checkout information")
    public CheckoutOverviewPage fillCheckoutInformation(String firstName, String lastName, String postalCode) {
        enterFirstName(firstName);
        enterLastName(lastName);
        enterPostalCode(postalCode);
        continueButton.click();
        return new CheckoutOverviewPage();
    }

    @Step("Click continue button")
    public CheckoutOverviewPage clickContinue() {
        continueButton.click();
        return new CheckoutOverviewPage();
    }

    @Step("Click cancel button")
    public CartPage clickCancel() {
        cancelButton.click();
        return new CartPage();
    }

    @Step("Get error message text")
    public String getErrorMessage() {
        return errorMessage.shouldBe(visible).getText();
    }

    @Step("Verify error message is displayed")
    public void verifyErrorMessageDisplayed() {
        errorMessage.shouldBe(visible);
    }

    @Step("Clear first name field")
    public void clearFirstName() {
        firstNameField.clear();
    }

    @Step("Clear last name field")
    public void clearLastName() {
        lastNameField.clear();
    }

    @Step("Clear postal code field")
    public void clearPostalCode() {
        postalCodeField.clear();
    }
}
