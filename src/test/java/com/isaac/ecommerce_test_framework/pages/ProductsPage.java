package com.isaac.ecommerce_test_framework.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

/**
 * Page Object for SauceDemo Products/Inventory Page
 */
public class ProductsPage {

    // Page elements
    private final SelenideElement productsTitle = $(".title");
    private final SelenideElement shoppingCartLink = $(".shopping_cart_link");
    private final SelenideElement shoppingCartBadge = $(".shopping_cart_badge");
    private final ElementsCollection productItems = $$(".inventory_item");
    private final ElementsCollection addToCartButtons = $$("[id^='add-to-cart-']");
    private final ElementsCollection removeFromCartButtons = $$("[id^='remove-']");
    private final SelenideElement menuButton = $("#react-burger-menu-btn");
    private final SelenideElement logoutLink = $("#logout_sidebar_link");

    @Step("Verify products page is displayed")
    public void verifyProductsPageDisplayed() {
        productsTitle.shouldHave(text("Products"));
    }

    @Step("Get number of products displayed")
    public int getProductCount() {
        return productItems.size();
    }

    @Step("Add product to cart by index: {index}")
    public void addProductToCart(int index) {
        addToCartButtons.get(index).click();
    }

    @Step("Remove product from cart by index: {index}")
    public void removeProductFromCart(int index) {
        removeFromCartButtons.get(index).click();
    }

    @Step("Add specific product to cart: {productName}")
    public void addProductToCart(String productName) {
        SelenideElement addButton = $("[data-test='add-to-cart-" + productName.toLowerCase().replace(" ", "-") + "']");
        addButton.click();
    }

    @Step("Remove specific product from cart: {productName}")
    public void removeProductFromCart(String productName) {
        SelenideElement removeButton = $("[data-test='remove-" + productName.toLowerCase().replace(" ", "-") + "']");
        removeButton.click();
    }

    @Step("Get shopping cart item count")
    public int getCartItemCount() {
        if (shoppingCartBadge.isDisplayed()) {
            return Integer.parseInt(shoppingCartBadge.getText());
        }
        return 0;
    }

    @Step("Click shopping cart")
    public CartPage clickShoppingCart() {
        shoppingCartLink.click();
        return new CartPage();
    }

    @Step("Verify shopping cart badge shows count: {expectedCount}")
    public void verifyCartBadgeCount(int expectedCount) {
        if (expectedCount > 0) {
            shoppingCartBadge.shouldHave(text(String.valueOf(expectedCount)));
        } else {
            shoppingCartBadge.shouldNotBe(visible);
        }
    }

    @Step("Get product name by index: {index}")
    public String getProductName(int index) {
        return productItems.get(index).$(".inventory_item_name").getText();
    }

    @Step("Get product price by index: {index}")
    public String getProductPrice(int index) {
        return productItems.get(index).$(".inventory_item_price").getText();
    }

    @Step("Sort products by: {sortOption}")
    public void sortProducts(String sortOption) {
        SelenideElement sortDropdown = $(".product_sort_container");
        sortDropdown.selectOption(sortOption);
    }

    @Step("Open menu")
    public void openMenu() {
        menuButton.click();
    }

    @Step("Logout from application")
    public LoginPage logout() {
        openMenu();
        logoutLink.click();
        return new LoginPage();
    }
}
