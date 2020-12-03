package furnitureshop.order;

import furnitureshop.FurnitureShop;
import furnitureshop.inventory.Category;
import furnitureshop.inventory.Piece;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Iterator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link OrderController} (cart)
 *
 * @author Shintaro Onogi
 * @version 1.0
 */

@SpringBootTest
@ContextConfiguration(classes = FurnitureShop.class)
public class CartTests {

	@Autowired
	SupplierRepository supplierRepository;

	@Autowired OrderController orderController;

	Iterator<Supplier> iterator = supplierRepository.findAll().iterator();
	Supplier supplier = iterator.next();
	Cart cart = new Cart();
	Piece Stuhl1 = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "/resources/img/chair_2.jpg", "schwarz",
			"Stuhl 1 in schwarz.", supplier, 5, Category.CHAIR);

	@BeforeEach
	void setUp() {
		cart.clear();
		assertTrue(cart.isEmpty());
	}

	/**
	 * checkIfAnItemCanBeAddedTest method
	 *
	 * Tests if you can add an item to cart
	 * Expects the item to be present in the cart
	 */

	/*
	@Test
	void checkIfAnItemCanBeAddedTest() {

		String result = orderController.addItem(Stuhl1, 1, cart);
		//System.out.println(Stuhl1.getId().toString());
		//System.out.println(cart.getItem(Stuhl1.getId().toString()));
		assertEquals(result, "redirect:/");
		try {
			Optional<CartItem> item = cart.getItem(Stuhl1.getId().toString());
			assertTrue(item.isPresent());
		} catch (NullPointerException ignored) {}
	}

	 */

	/**
	 * checkIfAnItemCanBeRemovedTest method
	 *
	 * Tests if you can delete/remove an item from the cart
	 * Expects the item to be not present in the cart
	 */

	/*
	@Test
	void checkIfAnItemCanBeRemovedTest() {

		String result = orderController.addItem(Stuhl1, 1, cart);
		try {
			String result_2 = orderController.editItem(Stuhl1.getId().toString(), 0, cart);
			assertEquals(result_2, "redirect:/cart");
			Optional<CartItem> item = cart.getItem(Stuhl1.getId().toString());
			assertFalse(item.isPresent());
		} catch (NullPointerException ignored) {}
	}
	*/

	/**
	 * checkIfTheNumberOfProductsCanBeReducedTest method
	 *
	 * Tests if the quantity of the same product in the cart can be reduced
	 * Expects the quantity to be reduced from 2 to 1
	 */

	/*
	@Test
	void checkIfTheNumberOfProductsCanBeReducedTest() {

		String result = orderController.addItem(Stuhl1, 2, cart);
		try {
			String result = orderController.editItem(Stuhl1.getId().toString(), 1, cart);
			assertEquals(result, "redirect:/cart");
		} catch (NullPointerException ignored) {}
	}
	*/

	/**
	 * checkIfTheNumberOfProductsCanBeIncreasedTest method
	 *
	 * Tests if the number of the same product in the cart can be increased
	 * Expects the quantity to be increased from 1 to 2
	 */

	/*

	@Test
	void checkIfTheNumberOfProductsCanBeIncreasedTest() {

		orderController.addItem(Stuhl1, 1, cart);
		try {
			String result = orderController.editItem(Stuhl1.getId().toString(), 2, cart);
			assertEquals(result, "redirect:/");
		} catch (NullPointerException ignored) {}
	}

	*/

	/**
	 * checkIfTheUserCanGotoCheckoutPage
	 *
	 * Tests if the customer can reach the checkout page if a product exists in the cart
	 * Expects to be directed to /orderCheckout
	 */

	/*

	@Test
	void checkIfTheUserCanGotoCheckoutPage() {

		orderController.addItem(Stuhl1, 1, cart);
		String result = orderController.checkout(null, cart);
		assertEquals(result, "orderCheckout");
	}
	*/
}
