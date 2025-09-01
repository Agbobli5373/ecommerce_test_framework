package com.isaac.ecommerce_test_framework.tests;

import com.isaac.ecommerce_test_framework.BaseTest;
import com.isaac.ecommerce_test_framework.pages.CartPage;
import com.isaac.ecommerce_test_framework.pages.LoginPage;
import com.isaac.ecommerce_test_framework.pages.ProductsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for SauceDemo shopping cart functionality
 */
@Epic("E-commerce")
@Feature("Shopping Cart")
public class CartTests extends BaseTest {

    @Test
    @Description("Verify empty cart display")
    @Story("Cart Display")
    public void testEmptyCart() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        CartPage cartPage = productsPage.clickShoppingCart();
        cartPage.verifyCartPageDisplayed();
        cartPage.verifyCartIsEmpty();

        Assert.assertEquals(cartPage.getCartItemCount(), 0, "Cart should be empty");
    }

    @Test
    @Description("Verify single item in cart")
    @Story("Cart Items")
    public void testSingleItemInCart() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        CartPage cartPage = productsPage.clickShoppingCart();

        cartPage.verifyCartPageDisplayed();
        cartPage.verifyCartHasItems();
        Assert.assertEquals(cartPage.getCartItemCount(), 1, "Cart should contain 1 item");

        // Verify item details
        String itemName = cartPage.getCartItemName(0);
        String itemPrice = cartPage.getCartItemPrice(0);
        String itemQuantity = cartPage.getCartItemQuantity(0);

        Assert.assertFalse(itemName.isEmpty(), "Item name should not be empty");
        Assert.assertFalse(itemPrice.isEmpty(), "Item price should not be empty");
        Assert.assertEquals(itemQuantity, "1", "Item quantity should be 1");
    }

    @Test
    @Description("Verify multiple items in cart")
    @Story("Cart Items")
    public void testMultipleItemsInCart() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        productsPage.addProductToCart(1);
        productsPage.addProductToCart(2);

        CartPage cartPage = productsPage.clickShoppingCart();
        cartPage.verifyCartPageDisplayed();

        Assert.assertEquals(cartPage.getCartItemCount(), 3, "Cart should contain 3 items");

        // Verify all items have details
        for (int i = 0; i < 3; i++) {
            String itemName = cartPage.getCartItemName(i);
            String itemPrice = cartPage.getCartItemPrice(i);
            String itemQuantity = cartPage.getCartItemQuantity(i);

            Assert.assertFalse(itemName.isEmpty(), "Item " + i + " name should not be empty");
            Assert.assertFalse(itemPrice.isEmpty(), "Item " + i + " price should not be empty");
            Assert.assertEquals(itemQuantity, "1", "Item " + i + " quantity should be 1");
        }
    }

    @Test
    @Description("Verify removing item from cart")
    @Story("Cart Modification")
    public void testRemoveItemFromCart() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        productsPage.addProductToCart(1);

        CartPage cartPage = productsPage.clickShoppingCart();
        Assert.assertEquals(cartPage.getCartItemCount(), 2, "Cart should contain 2 items");

        cartPage.removeItemFromCart(0);
        Assert.assertEquals(cartPage.getCartItemCount(), 1, "Cart should contain 1 item after removal");

        cartPage.removeItemFromCart(0);
        cartPage.verifyCartIsEmpty();
    }

    @Test
    @Description("Verify continue shopping functionality")
    @Story("Cart Navigation")
    public void testContinueShopping() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        CartPage cartPage = productsPage.clickShoppingCart();

        productsPage = cartPage.continueShopping();
        productsPage.verifyProductsPageDisplayed();
        productsPage.verifyCartBadgeCount(1);
    }

    @Test
    @Description("Verify cart item details match product details")
    @Story("Cart Validation")
    public void testCartItemDetailsMatch() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        // Get product details from products page
        String productName = productsPage.getProductName(0);
        String productPrice = productsPage.getProductPrice(0);

        productsPage.addProductToCart(0);
        CartPage cartPage = productsPage.clickShoppingCart();

        // Verify cart item details match
        String cartItemName = cartPage.getCartItemName(0);
        String cartItemPrice = cartPage.getCartItemPrice(0);

        Assert.assertEquals(cartItemName, productName, "Cart item name should match product name");
        Assert.assertEquals(cartItemPrice, productPrice, "Cart item price should match product price");
    }
}
