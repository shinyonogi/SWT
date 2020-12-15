package furnitureshop.order;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;


/**
 * IntegrationTest for {@link OrderController}
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
	OrderForm orderForm;
	ContactInformation contactInformation;

	@Autowired
	OrderService orderService;

	@BeforeEach
	void setUp() {
		supplier = new Supplier("supplier", 0.05);
		item = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "chair_2.jpg", "schwarz",
				"Stuhl 1 in schwarz.", supplier, 5, Category.CHAIR);
		cart = new Cart();
		cart.addOrUpdateItem(item, 5);
		cartItem = cart.get().findAny().get();

		orderForm = new OrderForm("Max Mustermann", "Musterstr. 1", "muster@muster.de", 1);
		contactInformation = new ContactInformation("Max Mustermann", "Musterstr. 1", "muster@muster.de");

		//orderService.orderDelieveryItem(cart, contactInformation);
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
	void redirectsToHomeWhenYouAddAnItem() throws Exception {
		mvc.perform(post("/cart/add/{id}", itemCatalog.findAll().stream().findAny().get().getId())
				.param("number", String.valueOf(5))
				.flashAttr("cart", cart))
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
				.param("amount", String.valueOf(3))
				.flashAttr("cart", cart))
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
		mvc.perform(post("/cart/delete/{id}", cartItem.getId())
				.flashAttr("cart", cart))
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

	@Test
	void returnsModelAndViewCheckoutWhenYouTryToCheckoutWithItems() throws Exception{
		mvc.perform(get("/checkout")
				.flashAttr("cart", cart))
				.andExpect(status().isOk())
				.andExpect(view().name("orderCheckout"));
	}

	/*
	@Test
	void returnsModelAndViewOrderSummaryIfValuesAreValid() throws Exception {
		mvc.perform(post("/checkout")
				.flashAttr("cart", cart)
				.flashAttr("orderform", orderForm))
				.andDo(print());
	}
	*/

	/**
	 * returnsModelAndViewOrderSearchWhenYouTryToReachIt() method
	 * Tests if user can reach the orderSearch page
	 *
	 * @throws Exception
	 */

	@Test
	void returnsModelAndViewOrderSearchWhenYouTryToReachIt() throws Exception {
		mvc.perform(get("/order"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("result", is(0)))
				.andExpect(status().isOk())
				.andExpect(view().name("orderSearch"));
	}

	@Test
	void returnsModelAndViewOrderSearchWhenYouTryToReachItWithInvalidID() throws Exception {
		mvc.perform(post("/order")
				.param("orderId", "test"))
				.andExpect(status().isOk())
				.andExpect(view().name("orderSearch"));
	}

	@Test
	void returnsNullWhenYouTryToReachItNotAsAdmin() throws Exception {
		mvc.perform(get("/admin/orders"))
				.andExpect(status().is(302));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsCustomerOrdersWhenYouTryToReachItAsAdmin() throws Exception {
		mvc.perform(get("/admin/orders"))
				.andExpect(status().isOk())
				.andExpect(view().name("customerOrders"));
	}
}
