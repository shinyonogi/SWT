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

	@Test
	void checkIfAnItemCanBeRemovedTest() {

		orderController.addItem(Stuhl1, 1, cart);
		try {
			String result = orderController.editItem(Stuhl1.getId().toString(), 0, cart);
			assertEquals(result, "redirect:/");
			Optional<CartItem> item = cart.getItem(Stuhl1.getId().toString());
			assertFalse(item.isPresent());
		} catch (NullPointerException ignored) {}
	}

	@Test
	void checkIfTheNumberOfProductsCanBeReducedTest() {

		orderController.addItem(Stuhl1, 2, cart);
		try {
			String result = orderController.editItem(Stuhl1.getId().toString(), 1, cart);
			assertEquals(result, "redirect:/");
		} catch (NullPointerException ignored) {}
	}

	@Test
	void checkIfTheNumberOfProductsCanBeIncreasedTest() {

		orderController.addItem(Stuhl1, 1, cart);
		try {
			String result = orderController.editItem(Stuhl1.getId().toString(), 2, cart);
			assertEquals(result, "redirect:/");
		} catch (NullPointerException ignored) {}
	}

	@Test
	void checkIfTheUserCanGotoCheckoutPage() {

		orderController.addItem(Stuhl1, 1, cart);
		String result = orderController.checkout(null, cart);
		assertEquals(result, "orderCheckout");
	}

	 */

}
