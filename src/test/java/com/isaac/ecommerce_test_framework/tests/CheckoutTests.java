package com.isaac.ecommerce_test_framework.tests;

import com.isaac.ecommerce_test_framework.BaseTest;
import com.isaac.ecommerce_test_framework.pages.*;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for SauceDemo checkout functionality
 */
@Epic("E-commerce")
@Feature("Checkout Process")
public class CheckoutTests extends BaseTest {

    @Test
    @Description("Verify complete checkout process with single item")
    @Story("Complete Checkout")
    public void testCompleteCheckoutSingleItem() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        CartPage cartPage = productsPage.clickShoppingCart();

        CheckoutPage checkoutPage = cartPage.clickCheckout();
        checkoutPage.verifyCheckoutPageDisplayed();

        CheckoutOverviewPage overviewPage = checkoutPage.fillCheckoutInformation("John", "Doe", "12345");
        overviewPage.verifyCheckoutOverviewPageDisplayed();

        // Verify order summary
        overviewPage.verifyOrderSummary();
        Assert.assertEquals(overviewPage.getCheckoutItemCount(), 1, "Should have 1 item in checkout");

        CheckoutCompletePage completePage = overviewPage.clickFinish();
        completePage.verifyCheckoutCompletePageDisplayed();
        completePage.verifyOrderCompletion();
    }

    @Test
    @Description("Verify complete checkout process with multiple items")
    @Story("Complete Checkout")
    public void testCompleteCheckoutMultipleItems() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        productsPage.addProductToCart(1);
        productsPage.addProductToCart(2);

        CartPage cartPage = productsPage.clickShoppingCart();
        CheckoutPage checkoutPage = cartPage.clickCheckout();

        CheckoutOverviewPage overviewPage = checkoutPage.fillCheckoutInformation("Jane", "Smith", "67890");
        Assert.assertEquals(overviewPage.getCheckoutItemCount(), 3, "Should have 3 items in checkout");

        CheckoutCompletePage completePage = overviewPage.clickFinish();
        completePage.verifyOrderCompletion();
    }

    @Test
    @Description("Verify checkout validation - empty first name")
    @Story("Checkout Validation")
    public void testCheckoutValidationEmptyFirstName() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        CartPage cartPage = productsPage.clickShoppingCart();
        CheckoutPage checkoutPage = cartPage.clickCheckout();

        checkoutPage.enterFirstName("");
        checkoutPage.enterLastName("Doe");
        checkoutPage.enterPostalCode("12345");
        checkoutPage.clickContinue();

        checkoutPage.verifyErrorMessageDisplayed();
        String errorMessage = checkoutPage.getErrorMessage();
        Assert.assertTrue(errorMessage.contains("First Name is required"),
                "Error message should indicate first name is required");
    }

    @Test
    @Description("Verify checkout validation - empty last name")
    @Story("Checkout Validation")
    public void testCheckoutValidationEmptyLastName() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        CartPage cartPage = productsPage.clickShoppingCart();
        CheckoutPage checkoutPage = cartPage.clickCheckout();

        checkoutPage.enterFirstName("John");
        checkoutPage.enterLastName("");
        checkoutPage.enterPostalCode("12345");
        checkoutPage.clickContinue();

        checkoutPage.verifyErrorMessageDisplayed();
        String errorMessage = checkoutPage.getErrorMessage();
        Assert.assertTrue(errorMessage.contains("Last Name is required"),
                "Error message should indicate last name is required");
    }

    @Test
    @Description("Verify checkout validation - empty postal code")
    @Story("Checkout Validation")
    public void testCheckoutValidationEmptyPostalCode() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        CartPage cartPage = productsPage.clickShoppingCart();
        CheckoutPage checkoutPage = cartPage.clickCheckout();

        checkoutPage.enterFirstName("John");
        checkoutPage.enterLastName("Doe");
        checkoutPage.enterPostalCode("");
        checkoutPage.clickContinue();

        checkoutPage.verifyErrorMessageDisplayed();
        String errorMessage = checkoutPage.getErrorMessage();
        Assert.assertTrue(errorMessage.contains("Postal Code is required"),
                "Error message should indicate postal code is required");
    }

    @Test
    @Description("Verify checkout cancel functionality")
    @Story("Checkout Navigation")
    public void testCheckoutCancel() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        CartPage cartPage = productsPage.clickShoppingCart();
        CheckoutPage checkoutPage = cartPage.clickCheckout();

        cartPage = checkoutPage.clickCancel();
        cartPage.verifyCartPageDisplayed();
        Assert.assertEquals(cartPage.getCartItemCount(), 1, "Should return to cart with item intact");
    }

    @Test
    @Description("Verify checkout overview cancel functionality")
    @Story("Checkout Navigation")
    public void testCheckoutOverviewCancel() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        CartPage cartPage = productsPage.clickShoppingCart();
        CheckoutPage checkoutPage = cartPage.clickCheckout();

        CheckoutOverviewPage overviewPage = checkoutPage.fillCheckoutInformation("John", "Doe", "12345");
        productsPage = overviewPage.clickCancel();

        productsPage.verifyProductsPageDisplayed();
        productsPage.verifyCartBadgeCount(1);
    }

    @Test
    @Description("Verify checkout with empty cart")
    @Story("Checkout Edge Cases")
    public void testCheckoutWithEmptyCart() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        CartPage cartPage = productsPage.clickShoppingCart();
        cartPage.verifyCartIsEmpty();

        // Should not be able to proceed to checkout with empty cart
        // The checkout button should not be available or should redirect
        // This test verifies the cart is empty and checkout isn't attempted
        Assert.assertEquals(cartPage.getCartItemCount(), 0, "Cart should be empty");
    }

    @Test
    @Description("Verify order summary calculations")
    @Story("Order Summary")
    public void testOrderSummary() {
        LoginPage loginPage = new LoginPage();
        ProductsPage productsPage = loginPage.login("standard_user", "secret_sauce");

        productsPage.addProductToCart(0);
        CartPage cartPage = productsPage.clickShoppingCart();
        CheckoutPage checkoutPage = cartPage.clickCheckout();
        CheckoutOverviewPage overviewPage = checkoutPage.fillCheckoutInformation("Test", "User", "00000");

        // Verify order summary elements are present
        String paymentInfo = overviewPage.getPaymentInfo();
        String shippingInfo = overviewPage.getShippingInfo();
        String itemTotal = overviewPage.getItemTotal();
        String tax = overviewPage.getTax();
        String total = overviewPage.getTotal();

        Assert.assertFalse(paymentInfo.isEmpty(), "Payment info should not be empty");
        Assert.assertFalse(shippingInfo.isEmpty(), "Shipping info should not be empty");
        Assert.assertFalse(itemTotal.isEmpty(), "Item total should not be empty");
        Assert.assertFalse(tax.isEmpty(), "Tax should not be empty");
        Assert.assertFalse(total.isEmpty(), "Total should not be empty");

        // Verify total is greater than item total (includes tax)
        double itemTotalValue = Double.parseDouble(itemTotal.replace("Item total: $", ""));
        double taxValue = Double.parseDouble(tax.replace("Tax: $", ""));
        double totalValue = Double.parseDouble(total.replace("Total: $", ""));

        Assert.assertEquals(totalValue, itemTotalValue + taxValue, 0.01,
                "Total should equal item total plus tax");
    }
}
