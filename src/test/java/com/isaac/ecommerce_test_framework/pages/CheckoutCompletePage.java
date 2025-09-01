package com.isaac.ecommerce_test_framework.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object for SauceDemo Checkout Complete Page
 */
public class CheckoutCompletePage {

    // Page elements
    private final SelenideElement completeTitle = $(".title");
    private final SelenideElement completeHeader = $(".complete-header");
    private final SelenideElement completeText = $(".complete-text");
    private final SelenideElement ponyExpressImage = $(".pony_express");
    private final SelenideElement backHomeButton = $("#back-to-products");

    @Step("Verify checkout complete page is displayed")
    public void verifyCheckoutCompletePageDisplayed() {
        completeTitle.shouldHave(text("Checkout: Complete!"));
        completeHeader.shouldHave(text("Thank you for your order!"));
    }

    @Step("Get complete header text")
    public String getCompleteHeader() {
        return completeHeader.getText();
    }

    @Step("Get complete text")
    public String getCompleteText() {
        return completeText.getText();
    }

    @Step("Verify pony express image is displayed")
    public void verifyPonyExpressImageDisplayed() {
        ponyExpressImage.shouldBe(visible);
    }

    @Step("Click back home button")
    public ProductsPage clickBackHome() {
        backHomeButton.click();
        return new ProductsPage();
    }

    @Step("Verify order completion message")
    public void verifyOrderCompletion() {
        completeHeader.shouldHave(text("Thank you for your order!"));
        completeText.shouldHave(
                text("Your order has been dispatched, and will arrive just as fast as the pony can get there!"));
    }
}
