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
 * Test cases for SauceDemo product browsing and cart functionality
 */
@Epic("E-commerce")
@Feature("Product Management")
public class ProductTests extends BaseTest {

    @Test
    @Description("Verify products are displayed correctly on products page")
    @Story("Product Display")
    public void testProductsDisplay() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.verifyProductsPageDisplayed();
        int productCount = productsPage.getProductCount();
        Assert.assertEquals(productCount, 6, "Should display 6 products");

        // Verify the first product has name and price
        String firstProductName = productsPage.getProductName(0);
        String firstProductPrice = productsPage.getProductPrice(0);
        Assert.assertFalse(firstProductName.isEmpty(), "Product name should not be empty");
        Assert.assertFalse(firstProductPrice.isEmpty(), "Product price should not be empty");
        Assert.assertTrue(firstProductPrice.startsWith("$"), "Price should start with $");
    }

    @Test
    @Description("Verify adding single product to cart")
    @Story("Shopping Cart")
    public void testAddSingleProductToCart() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        productsPage.verifyCartBadgeCount(1);

        Assert.assertEquals(productsPage.getCartItemCount(), 1, "Cart should contain 1 item");
    }

    @Test
    @Description("Verify adding multiple products to cart")
    @Story("Shopping Cart")
    public void testAddMultipleProductsToCart() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        productsPage.addProductToCart(1);
        productsPage.addProductToCart(2);

        productsPage.verifyCartBadgeCount(3);
        Assert.assertEquals(productsPage.getCartItemCount(), 3, "Cart should contain 3 items");
    }

    @Test
    @Description("Verify removing product from cart")
    @Story("Shopping Cart")
    public void testRemoveProductFromCart() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        productsPage.verifyCartBadgeCount(1);

        productsPage.removeProductFromCart(0);
        productsPage.verifyCartBadgeCount(0);

        Assert.assertEquals(productsPage.getCartItemCount(), 0, "Cart should be empty");
    }

    @Test
    @Description("Verify cart persistence when navigating")
    @Story("Shopping Cart")
    public void testCartPersistence() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        productsPage.addProductToCart(1);
        productsPage.verifyCartBadgeCount(2);

        // Navigate to cart and back
        CartPage cartPage = productsPage.clickShoppingCart();
        cartPage.verifyCartPageDisplayed();
        Assert.assertEquals(cartPage.getCartItemCount(), 2, "Cart should contain 2 items");

        productsPage = cartPage.continueShopping();
        productsPage.verifyProductsPageDisplayed();
        productsPage.verifyCartBadgeCount(2);
    }

    @Test
    @Description("Verify product sorting functionality")
    @Story("Product Sorting")
    public void testProductSorting() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        // Test sorting by name (A to Z)
        productsPage.sortProducts("Name (A to Z)");
        String firstProductName = productsPage.getProductName(0);
        String lastProductName = productsPage.getProductName(5);

        // Verify alphabetical order (first should be before last alphabetically)
        Assert.assertTrue(firstProductName.compareTo(lastProductName) < 0,
                "Products should be sorted alphabetically A to Z");

        // Test sorting by name (Z to A)
        productsPage.sortProducts("Name (Z to A)");
        firstProductName = productsPage.getProductName(0);
        lastProductName = productsPage.getProductName(5);

        Assert.assertTrue(firstProductName.compareTo(lastProductName) > 0,
                "Products should be sorted alphabetically Z to A");
    }

    @Test
    @Description("Verify product price sorting")
    @Story("Product Sorting")
    public void testPriceSorting() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        // Test sorting by price (low to high)
        productsPage.sortProducts("Price (low to high)");
        String firstProductPrice = productsPage.getProductPrice(0);
        String lastProductPrice = productsPage.getProductPrice(5);

        double firstPrice = Double.parseDouble(firstProductPrice.replace("$", ""));
        double lastPrice = Double.parseDouble(lastProductPrice.replace("$", ""));

        Assert.assertTrue(firstPrice <= lastPrice, "Products should be sorted by price low to high");

        // Test sorting by price (high to low)
        productsPage.sortProducts("Price (high to low)");
        firstProductPrice = productsPage.getProductPrice(0);
        lastProductPrice = productsPage.getProductPrice(5);

        firstPrice = Double.parseDouble(firstProductPrice.replace("$", ""));
        lastPrice = Double.parseDouble(lastProductPrice.replace("$", ""));

        Assert.assertTrue(firstPrice >= lastPrice, "Products should be sorted by price high to low");
    }
}
