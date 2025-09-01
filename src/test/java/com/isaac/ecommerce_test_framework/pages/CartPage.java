package com.isaac.ecommerce_test_framework.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.testng.Assert;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

/**
 * Page Object for SauceDemo Shopping Cart Page
 */
public class CartPage {

    // Page elements
    private final SelenideElement cartTitle = $(".title");
    private final ElementsCollection cartItems = $$(".cart_item");
    private final SelenideElement continueShoppingButton = $("#continue-shopping");
    private final SelenideElement checkoutButton = $("#checkout");

    @Step("Verify cart page is displayed")
    public void verifyCartPageDisplayed() {
        cartTitle.shouldHave(text("Your Cart"));
    }

    @Step("Get number of items in cart")
    public int getCartItemCount() {
        return cartItems.size();
    }

    @Step("Get cart item name by index: {index}")
    public String getCartItemName(int index) {
        return cartItems.get(index).$(".inventory_item_name").getText();
    }

    @Step("Get cart item price by index: {index}")
    public String getCartItemPrice(int index) {
        return cartItems.get(index).$(".inventory_item_price").getText();
    }

    @Step("Get cart item quantity by index: {index}")
    public String getCartItemQuantity(int index) {
        return cartItems.get(index).$(".cart_quantity").getText();
    }

    @Step("Remove item from cart by index: {index}")
    public void removeItemFromCart(int index) {
        cartItems.get(index).$("[id^='remove-']").click();
    }

    @Step("Click continue shopping button")
    public ProductsPage continueShopping() {
        continueShoppingButton.click();
        return new ProductsPage();
    }

    @Step("Click checkout button")
    public CheckoutPage clickCheckout() {
        checkoutButton.click();
        return new CheckoutPage();
    }

    @Step("Verify cart is empty")
    public void verifyCartIsEmpty() {
        Assert.assertEquals(cartItems.size(), 0, "Cart should be empty");
    }

    @Step("Verify cart has items")
    public void verifyCartHasItems() {
        Assert.assertTrue(cartItems.size() > 0, "Cart should have items");
    }
}
