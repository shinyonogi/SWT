package furnitureshop.order;

import com.mysema.commons.lang.Assert;
import furnitureshop.FurnitureShop;
import furnitureshop.inventory.Category;
import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemCatalog;
import furnitureshop.inventory.Piece;
import furnitureshop.lkw.LKW;
import furnitureshop.lkw.LKWService;
import furnitureshop.lkw.LKWType;
import furnitureshop.supplier.Supplier;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.Order;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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

	@Autowired
	LKWService lkwService;

	@Autowired
	BusinessTime businessTime;

	@BeforeEach
	void setUp() {
		supplier = new Supplier("supplier", 0.05);
		item = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), new byte[0], "schwarz",
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

	@Test
	void redirectsToCartWhenYouDeleteAnItemByEditing() throws Exception {
		mvc.perform(post("/cart/change/{id}", cartItem.getId())
				.param("amount", String.valueOf(-3))
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

	@Test
	void returnsOrderCheckoutWhenYouTryToBuyWithInvalidContactInformation() throws Exception {
		OrderForm orderForm = new OrderForm("", "", "", 1);

		mvc.perform(post("/checkout")
				.flashAttr("cart", cart)
				.flashAttr("orderform", orderForm))
				.andExpect(status().isOk())
				.andExpect(view().name("orderCheckout"))
				.andExpect(model().attribute("result", 1));

		orderForm = new OrderForm("test", "", "", 1);

		mvc.perform(post("/checkout")
				.flashAttr("cart", cart)
				.flashAttr("orderform", orderForm))
				.andExpect(status().isOk())
				.andExpect(view().name("orderCheckout"))
				.andExpect(model().attribute("result", 2));

		orderForm = new OrderForm("test", "test", "@", 1);

		mvc.perform(post("/checkout")
				.flashAttr("cart", cart)
				.flashAttr("orderform", orderForm))
				.andExpect(status().isOk())
				.andExpect(view().name("orderCheckout"))
				.andExpect(model().attribute("result", 3));
	}

	//@Todo
	/*
	@Test
	void testCorrectWeightCalculationInOrderCheckout() throws Exception{
		Item item2 = new Piece(1, "Stuhl 2", Money.of(59.99, Currencies.EURO), new byte[0], "schwarz",
				"Stuhl 1 in schwarz.", supplier, 5, Category.CHAIR);

		Cart cart = new Cart();
		cart.addOrUpdateItem(item2, 5);
		OrderForm orderForm = new OrderForm("test","test","1@1", 1);

		mvc.perform(post("/checkout")
				.flashAttr("cart", cart)
				.flashAttr("orderform", orderForm))
				.andExpect(status().isOk())
				.andExpect(view().name("orderSummary"))
				.andExpect(model().attribute("lkwtype", LKWType.SMALL));

		Cart cart2 = new Cart();
		cart2.addOrUpdateItem(item2, 401);

		mvc.perform(post("/checkout")
				.flashAttr("cart", cart2)
				.flashAttr("orderform", orderForm))
				.andExpect(status().isOk())
				.andExpect(view().name("orderSummary"))
				.andExpect(model().attribute("lkwtype", LKWType.MEDIUM));

		Cart cart3 = new Cart();
		cart3.addOrUpdateItem(item2, 801);

		mvc.perform(post("/checkout")
				.flashAttr("cart", cart3)
				.flashAttr("orderform", orderForm))
				.andExpect(status().isOk())
				.andExpect(view().name("orderSummary"))
				.andExpect(model().attribute("lkwtype", LKWType.LARGE));
	}
	*/
	@Test
	void getOrderOverviewWithNotExistingOrderId() throws Exception {
		mvc.perform(get("/order/someRandomId"))
				.andExpect(redirectedUrl("/order"));
	}

	@Test
	void getOrderOverviewWithPickupOrder() throws Exception {
		ShopOrder pickup = null;
		for (ItemOrder order : orderService.findAllItemOrders()) {
			if (order instanceof Pickup) {
				pickup = order;
			}
		}
		if (pickup == null) {
			pickup = orderService.orderPickupItem(cart, contactInformation);
		}

		mvc.perform(get(String.format("/order/%s", pickup.getId().getIdentifier())))
				.andExpect(view().name("orderOverview"))
				//.andExpect(model().attribute("items", ((ItemOrder) pickup).getOrderEntries()))
				.andExpect(model().attribute("order", pickup))
				.andExpect(status().isOk());
	}

	@Test
	void getOrderOverviewWithDeliveryOrder() throws Exception {
		ShopOrder delivery = null;
		for (ItemOrder order : orderService.findAllItemOrders()) {
			if (order instanceof Delivery) {
				delivery = order;
			}
		}
		if (delivery == null) {
			delivery = orderService.orderDelieveryItem(cart, contactInformation);
		}

		mvc.perform(get(String.format("/order/%s", delivery.getId().getIdentifier())))
				.andExpect(view().name("orderOverview"))
				//.andExpect(model().attribute("items", ((ItemOrder) pickup).getOrderEntries()))
				.andExpect(model().attribute("order", delivery))
				.andExpect(model().attribute("lkw", ((Delivery) delivery).getLkw()))
				.andExpect(model().attribute("deliveryDate", ((Delivery) delivery).getDeliveryDate()))
				.andExpect(status().isOk());
	}

	@Test
	void getOrderOverviewWithLKWCharter() throws Exception {
		LKWCharter charter = null;
		for (Order order : orderService.findAllItemOrders()) {
			if (order instanceof LKWCharter) {
				charter = (LKWCharter) order;
			}
		}
		if (charter == null) {
			LocalDate deliveryDate = LocalDate.now();
			Optional<LKW> olkw = lkwService.createCharterLKW(deliveryDate, LKWType.SMALL);
			Assert.isTrue(olkw.isPresent(), "LKW Charter konnte nicht erstellt werden");
			charter = lkwService.createLKWOrder(olkw.get(), deliveryDate, contactInformation);
		}

		mvc.perform(get(String.format("/order/%s", charter.getId().getIdentifier())))
				.andExpect(view().name("orderOverview"))
				//.andExpect(model().attribute("items", ((ItemOrder) pickup).getOrderEntries()))
				.andExpect(model().attribute("order", charter))
				.andExpect(model().attribute("lkw", charter.getLkw()))
				.andExpect(model().attribute("cancelable", false))
				.andExpect(model().attribute("charterDate", charter.getRentDate()))
				.andExpect(status().isOk());
	}

	@Test
	void TestCancelItemOrderWithoutAuthentication() throws Exception {
		ItemOrder pickup = null;
		for (ItemOrder order : orderService.findAllItemOrders()) {
			if (order instanceof Pickup) {
				pickup = order;
			}
		}
		if (pickup == null) {
			pickup = orderService.orderPickupItem(cart, contactInformation);
		}
		String orderId = pickup.getId().getIdentifier();
		Long itemEntryId = pickup.getOrderEntries().get(0).getId();
		Item item = pickup.getOrderEntries().get(0).getItem();


		mvc.perform(post(String.format("/order/%s/cancelItem", pickup.getId().getIdentifier()))
				.param("itemEntryId", String.valueOf(itemEntryId)))
				.andExpect(redirectedUrl(String.format("/order/%s", pickup.getId().getIdentifier())));

		Optional<ShopOrder> orderPostCancelOptional = orderService.findById(orderId);
		assertTrue(orderPostCancelOptional.isPresent(), "Order wurde nicht gefunden");
		ShopOrder orderPostCancel = orderPostCancelOptional.get();

		assertTrue(((ItemOrder) orderPostCancel).getOrderEntries().get(0).getId() == itemEntryId, "Das Item wurde" +
				"nicht erfolgreich storniert");
	}

	@Test
	@WithMockUser(username = "admin", roles = "EMPLOYEE")
	void TestChangeOrder() throws Exception {
		ItemOrder pickup = null;
		for (ItemOrder order : orderService.findAllItemOrders()) {
			if (order instanceof Pickup) {
				pickup = order;
			}
		}
		if (pickup == null) {
			pickup = orderService.orderPickupItem(cart, contactInformation);
		}
		String orderId = pickup.getId().getIdentifier();
		Long itemEntryId = pickup.getOrderEntries().get(0).getId();
		Item item = pickup.getOrderEntries().get(0).getItem();

		mvc.perform(post(String.format("/order/%s/changeStatus", pickup.getId().getIdentifier()))
				.param("itemEntryId", String.valueOf(itemEntryId))
				.param("status", String.valueOf(OrderStatus.STORED)))
				.andExpect(redirectedUrl(String.format("/order/%s", pickup.getId().getIdentifier())));

		Optional<ShopOrder> orderPostCancelOptional = orderService.findById(orderId);
		assertTrue(orderPostCancelOptional.isPresent(), "Order wurde nicht gefunden");
		ShopOrder orderPostCancel = orderPostCancelOptional.get();

		assertTrue(((ItemOrder) orderPostCancel).getOrderEntries().get(0).getStatus() == OrderStatus.STORED,
				"Der Status des Items wurde nicht erfolgreich geändert");
	}

	@Test
	void TestCancelLkw() throws Exception {
		LKWCharter charter = null;
		for (Order order : orderService.findAllItemOrders()) {
			if (order instanceof LKWCharter) {
				charter = (LKWCharter) order;
			}
		}
		if (charter == null) {
			LocalDate date = lkwService.findNextAvailableDeliveryDate(LocalDate.now(), LKWType.SMALL);
			Optional<LKW> olkw = lkwService.createCharterLKW(date, LKWType.SMALL);
			Assert.isTrue(olkw.isPresent(), "LKW Charter konnte nicht erstellt werden");
			charter = lkwService.createLKWOrder(olkw.get(), date, contactInformation);
		}

		mvc.perform(post(String.format("/order/%s/cancelLkw", charter.getId().getIdentifier())))
				.andExpect(redirectedUrl("/"));

		assertTrue(orderService.findById(charter.getId().getIdentifier()).isEmpty(), "Die Order wurde nicht" +
				" gelöscht");
	}
}
