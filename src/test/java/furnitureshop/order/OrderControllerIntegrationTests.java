package furnitureshop.order;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import furnitureshop.FurnitureShop;
import furnitureshop.inventory.Category;
import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemCatalog;
import furnitureshop.inventory.Piece;
import furnitureshop.supplier.Supplier;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;


/**
 * IntegrationTest for {@link OrderController}
 *
 * @author Shintaro Onogi
 */

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = FurnitureShop.class)
public class OrderControllerIntegrationTests {

	@Autowired
	MockMvc mvc;

	@Autowired
	ItemCatalog itemCatalog;

	Supplier supplier;
	Item item;
	CartItem cartItem;
	Cart cart;

	@BeforeEach
	void setUp() {
		supplier = new Supplier("supplier", 0.05);
		item = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "chair_2.jpg", "schwarz",
				"Stuhl 1 in schwarz.", supplier, 5, Category.CHAIR);
		cart = new Cart();
		cart.addOrUpdateItem(item, 5);
		cartItem = cart.get().findAny().get();
	}

	/**
	 * returnsModelAndViewWhenYouTryToReachCart method
	 * Tests if you can reach the /cart
	 * Expects to reach it
	 *
	 * @throws Exception
	 */

	@Test
	void returnsModelAndViewCartWhenYouTryToReachCart() throws Exception {

		mvc.perform(get("/cart"))
			.andExpect(status().isOk())
			.andExpect(view().name("cart"));
	}

	/**
	 * redirectsToHomeWhenYouAddAnItem method
	 * Tests if you get redirected to "/" if you add an item to cart
	 *
	 * @throws Exception
	 */

	@Test
	void redirectsToHomeWhenYouAddAnItem() throws Exception { //Trying to figure out why it won't work with local item
		System.out.println(item.getId());
		System.out.println(itemCatalog.findAll().stream().findAny().get().getId());
		mvc.perform(post("/cart/add/{id}", itemCatalog.findAll().stream().findAny().get().getId() /*item.getId()*/)
				.param("number", String.valueOf(5)))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", endsWith("/")));
	}

	/**
	 * redirectsToCartWhenYouEditAnItem method
	 * Tests if you get redirected to "/cart" if you edit an item in the cart
	 *
	 * @throws Exception
	 */

	@Test
	void redirectsToCartWhenYouEditAnItem() throws Exception {
		mvc.perform(post("/cart/change/{id}", cartItem.getId())
				.param("amount", String.valueOf(3)))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart"))
				.andExpect(view().name("redirect:/cart"));
	}

	/**
	 * redirectsToCartWhenYouDeleteAnItem method
	 * Tests if you get redirected to "/cart" if you delete an item in the cart
	 *
	 * @throws Exception
	 */

	@Test
	void redirectsToCartWhenYouDeleteAnItem() throws Exception {
		mvc.perform(post("/cart/delete/{id}", cartItem.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart"))
				.andExpect(view().name("redirect:/cart"));
	}

	/**
	 * returnsModelAndViewCheckoutWhenYouTryToReachCheckout() method
	 * Tests if user can reach the "/orderCheckout" page if there are no item in the cart
	 *
	 * @throws Exception
	 */

	@Test
	void returnsModelAndViewCheckoutWhenYouTryToCheckoutWithNoItems() throws Exception {
		mvc.perform(get("/checkout"))
				.andExpect(view().name("redirect:/cart"));
	}
}