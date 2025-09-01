package com.isaac.ecommerce_test_framework.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

/**
 * Page Object for SauceDemo Checkout Overview Page
 */
public class CheckoutOverviewPage {

    // Page elements
    private final SelenideElement overviewTitle = $(".title");
    private final ElementsCollection cartItems = $$(".cart_item");
    private final SelenideElement paymentInfo = $(".summary_value_label");
    private final SelenideElement shippingInfo = $(".summary_value_label");
    private final SelenideElement itemTotal = $(".summary_subtotal_label");
    private final SelenideElement tax = $(".summary_tax_label");
    private final SelenideElement total = $(".summary_total_label");
    private final SelenideElement finishButton = $("#finish");
    private final SelenideElement cancelButton = $("#cancel");

    @Step("Verify checkout overview page is displayed")
    public void verifyCheckoutOverviewPageDisplayed() {
        overviewTitle.shouldHave(text("Checkout: Overview"));
    }

    @Step("Get number of items in checkout")
    public int getCheckoutItemCount() {
        return cartItems.size();
    }

    @Step("Get checkout item name by index: {index}")
    public String getCheckoutItemName(int index) {
        return cartItems.get(index).$(".inventory_item_name").getText();
    }

    @Step("Get checkout item price by index: {index}")
    public String getCheckoutItemPrice(int index) {
        return cartItems.get(index).$(".inventory_item_price").getText();
    }

    @Step("Get payment information")
    public String getPaymentInfo() {
        return paymentInfo.getText();
    }

    @Step("Get shipping information")
    public String getShippingInfo() {
        return shippingInfo.getText();
    }

    @Step("Get item total")
    public String getItemTotal() {
        return itemTotal.getText();
    }

    @Step("Get tax amount")
    public String getTax() {
        return tax.getText();
    }

    @Step("Get total amount")
    public String getTotal() {
        return total.getText();
    }

    @Step("Click finish button")
    public CheckoutCompletePage clickFinish() {
        finishButton.click();
        return new CheckoutCompletePage();
    }

    @Step("Click cancel button")
    public ProductsPage clickCancel() {
        cancelButton.click();
        return new ProductsPage();
    }

    @Step("Verify order summary is correct")
    public void verifyOrderSummary() {
        paymentInfo.shouldNotBe(empty);
        shippingInfo.shouldNotBe(empty);
        itemTotal.shouldNotBe(empty);
        tax.shouldNotBe(empty);
        total.shouldNotBe(empty);
    }
}
