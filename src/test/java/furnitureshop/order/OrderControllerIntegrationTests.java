package furnitureshop.order;

import com.mysema.commons.lang.Assert;
import furnitureshop.FurnitureShop;
import furnitureshop.inventory.Category;
import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemCatalog;
import furnitureshop.inventory.Piece;
import furnitureshop.lkw.LKW;
import furnitureshop.lkw.LKWCatalog;
import furnitureshop.lkw.LKWService;
import furnitureshop.lkw.LKWType;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import furnitureshop.utils.Utils;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
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

	@Autowired
	OrderService orderService;

	@Autowired
	LKWService lkwService;

	@Autowired
	BusinessTime businessTime;

	@Autowired
	SupplierRepository supplierRepository;

	@Autowired
	LKWCatalog lkwCatalog;

	Item item;
	CartItem cartItem;
	Cart cart;
	ContactInformation contactInformation;

	@BeforeEach
	void setUp() {
		Utils.clearRepositories();

		for (LKWType type : LKWType.values()) {
			for (int i = 0; i < 2; i++) {
				lkwCatalog.save(new LKW(type));
			}
		}

		final Supplier supplier = new Supplier("Supplier", 0.05);

		item = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), new byte[0], "schwarz",
				"", supplier, 5, Category.CHAIR);

		supplierRepository.save(supplier);
		itemCatalog.save(item);

		cart = new Cart();
		cart.addOrUpdateItem(item, 5);
		cartItem = cart.get().findAny().orElse(null);

		contactInformation = new ContactInformation("Max Mustermann", "Musterstr. 1", "muster@muster.de");

		// Reset Time
		final LocalDateTime time = LocalDateTime.of(2020, 12, 21, 0, 0);
		final Duration delta = Duration.between(businessTime.getTime(), time);
		businessTime.forward(delta);
	}

	/**
	 * returnsModelAndViewWhenYouTryToReachCart method
	 * Tests if you can reach the /cart
	 * Expects to reach it
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
	 */
	@Test
	void redirectsToHomeWhenYouAddAnItem() throws Exception {
		mvc.perform(post("/cart/add/{id}", item.getId())
				.param("number", "5")
				.flashAttr("cart", cart))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", endsWith("/")));
	}

	/**
	 * redirectsToCartWhenYouEditAnItem method
	 * Tests if you get redirected to "/cart" if you edit an item in the cart
	 */
	@Test
	void redirectsToCartWhenYouEditAnItem() throws Exception {
		mvc.perform(post("/cart/change/{id}", cartItem.getId())
				.param("amount", "3")
				.flashAttr("cart", cart))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart"))
				.andExpect(view().name("redirect:/cart"));
	}

	@Test
	void redirectsToCartWhenYouDeleteAnItemByEditing() throws Exception {
		mvc.perform(post("/cart/change/{id}", cartItem.getId())
				.param("amount", "-3")
				.flashAttr("cart", cart))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart"))
				.andExpect(view().name("redirect:/cart"));
	}

	/**
	 * redirectsToCartWhenYouDeleteAnItem method
	 * Tests if you get redirected to "/cart" if you delete an item in the cart
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
	 */
	@Test
	void returnsModelAndViewCheckoutWhenYouTryToCheckoutWithNoItems() throws Exception {
		mvc.perform(get("/checkout"))
				.andExpect(view().name("redirect:/cart"));
	}

	@Test
	void returnsModelAndViewCheckoutWhenYouTryToCheckoutWithItems() throws Exception {
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
		final Pickup pickup = orderService.orderPickupItem(cart, contactInformation);

		mvc.perform(get("/order/{id}", pickup.getId().getIdentifier()))
				.andExpect(view().name("orderOverview"))
				//.andExpect(model().attribute("items", ((ItemOrder) pickup).getOrderEntries()))
				.andExpect(model().attribute("order", pickup))
				.andExpect(status().isOk());
	}

	@Test
	void getOrderOverviewWithDeliveryOrder() throws Exception {
		final Delivery delivery = orderService.orderDelieveryItem(cart, contactInformation);

		mvc.perform(get("/order/{id}", delivery.getId().getIdentifier()))
				.andExpect(view().name("orderOverview"))
				//.andExpect(model().attribute("items", ((ItemOrder) pickup).getOrderEntries()))
				.andExpect(model().attribute("order", delivery))
				.andExpect(model().attribute("lkw", delivery.getLkw()))
				.andExpect(model().attribute("deliveryDate", delivery.getDeliveryDate()))
				.andExpect(status().isOk());
	}

	@Test
	void getOrderOverviewWithLKWCharter() throws Exception {
		final LocalDate deliveryDate = businessTime.getTime().toLocalDate();
		final Optional<LKW> olkw = lkwService.createCharterLKW(deliveryDate, LKWType.SMALL);
		Assert.isTrue(olkw.isPresent(), "LKW Charter konnte nicht erstellt werden");

		final LKWCharter charter = lkwService.createLKWOrder(olkw.orElse(null), deliveryDate, contactInformation);

		mvc.perform(get("/order/{id}", charter.getId().getIdentifier()))
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
		final Pickup pickup = orderService.orderPickupItem(cart, contactInformation);

		final String orderId = pickup.getId().getIdentifier();
		final long itemEntryId = pickup.getOrderEntries().get(0).getId();
		final Item item = pickup.getOrderEntries().get(0).getItem();

		mvc.perform(post("/order/{id}/cancelItem", pickup.getId().getIdentifier())
				.param("itemEntryId", String.valueOf(itemEntryId)))
				.andExpect(redirectedUrl(String.format("/order/%s", pickup.getId().getIdentifier())));

		final Optional<ShopOrder> orderPostCancelOptional = orderService.findById(orderId);
		assertTrue(orderPostCancelOptional.isPresent(), "Order wurde nicht gefunden");

		final ShopOrder orderPostCancel = orderPostCancelOptional.get();
		assertEquals(itemEntryId, ((ItemOrder) orderPostCancel).getOrderEntries().get(0).getId(), "Das Item wurde nicht erfolgreich storniert");
	}

	@Test
	@WithMockUser(username = "admin", roles = "EMPLOYEE")
	void TestChangeOrder() throws Exception {
		final Pickup pickup = orderService.orderPickupItem(cart, contactInformation);

		final String orderId = pickup.getId().getIdentifier();
		final long itemEntryId = pickup.getOrderEntries().get(0).getId();
		final Item item = pickup.getOrderEntries().get(0).getItem();

		mvc.perform(post("/order/{id}/changeStatus", pickup.getId().getIdentifier())
				.param("itemEntryId", String.valueOf(itemEntryId))
				.param("status", String.valueOf(OrderStatus.STORED)))
				.andExpect(redirectedUrl(String.format("/order/%s", pickup.getId().getIdentifier())));

		final Optional<ShopOrder> orderPostCancelOptional = orderService.findById(orderId);
		assertTrue(orderPostCancelOptional.isPresent(), "Order wurde nicht gefunden");

		final ShopOrder orderPostCancel = orderPostCancelOptional.get();
		assertSame(OrderStatus.STORED, ((ItemOrder) orderPostCancel).getOrderEntries().get(0).getStatus(), "Der Status des Items wurde nicht erfolgreich geändert");
	}

	@Test
	void TestCancelLkw() throws Exception {
		final Optional<LKW> olkw = lkwService.createCharterLKW(businessTime.getTime().toLocalDate(), LKWType.SMALL);
		Assert.isTrue(olkw.isPresent(), "LKW Charter konnte nicht erstellt werden");

		final LKWCharter charter = lkwService.createLKWOrder(olkw.orElse(null), businessTime.getTime().toLocalDate(), contactInformation);

		mvc.perform(post("/order/{id}/cancelLkw", charter.getId().getIdentifier()))
				.andExpect(redirectedUrl("/"));

		assertTrue(orderService.findById(charter.getId().getIdentifier()).isEmpty(), "Die Order wurde nicht gelöscht");
	}

}
