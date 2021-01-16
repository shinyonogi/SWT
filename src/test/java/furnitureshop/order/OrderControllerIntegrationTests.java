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
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderManagement;
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
	SupplierRepository supplierRepository;

	@Autowired
	LKWCatalog lkwCatalog;

	@Autowired
	OrderManagement<ShopOrder> orderManagement;

	@Autowired
	LKWService lkwService;

	@Autowired
	OrderService orderService;

	@Autowired
	BusinessTime businessTime;

	Item item;
	CartItem cartItem;
	Cart cart;
	ContactInformation contactInformation;

	@BeforeEach
	void setUp() {
		for (ShopOrder order : orderService.findAll()) {
			orderManagement.delete(order);
		}

		lkwCatalog.deleteAll();
		itemCatalog.deleteAll();
		supplierRepository.deleteAll();

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

		contactInformation = new ContactInformation("Max Mustermann", "Musterstr. 1", "praxis.ecg2020@gmail.com");

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
				.andExpect(redirectedUrl("/cart"));
	}

	@Test
	void redirectsToCartWhenYouDeleteAnItemByEditing() throws Exception {
		mvc.perform(post("/cart/change/{id}", cartItem.getId())
				.param("amount", "-3")
				.flashAttr("cart", cart))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart"))
				.andExpect(redirectedUrl("/cart"));
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
				.andExpect(redirectedUrl("/cart"));
	}

	/**
	 * returnsModelAndViewCheckoutWhenYouTryToReachCheckout() method
	 * Tests if user can reach the "/orderCheckout" page if there are no item in the cart
	 */
	@Test
	void returnsModelAndViewCheckoutWhenYouTryToCheckoutWithNoItems() throws Exception {
		mvc.perform(get("/checkout"))
				.andExpect(redirectedUrl("/cart"));
	}

	@Test
	void returnsModelAndViewCheckoutWhenYouTryToCheckoutWithItems() throws Exception {
		mvc.perform(get("/checkout")
				.flashAttr("cart", cart))
				.andExpect(status().isOk())
				.andExpect(view().name("orderCheckout"));
	}

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
	void redirectsAfterOrderSearch() throws Exception {
		final Pickup pickup = orderService.orderPickupItem(cart, contactInformation);

		mvc.perform(post("/order")
				.param("orderId", pickup.getId().getIdentifier()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/order/" + pickup.getId().getIdentifier()));
	}

	@Test
	void returnsNullWhenYouTryToReachItNotAsAdmin() throws Exception {
		mvc.perform(get("/admin/orders"))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsCustomerOrdersWhenYouTryToReachItAsAdmin() throws Exception {
		mvc.perform(get("/admin/orders"))
				.andExpect(status().isOk())
				.andExpect(view().name("customerOrders"));
	}

	@Test
	void returnsNullWhenChangingOptions() throws Exception {
		mvc.perform(post("/admin/orders"))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsCustomerOrdersWhenChangingOptions() throws Exception {
		mvc.perform(post("/admin/orders")
				.param("text", "abc")
				.param("filter", "1")
				.param("sort", "0")
				.param("reverse", "false"))
				.andExpect(status().isOk())
				.andExpect(view().name("customerOrders"));
	}

	@Test
	void returnsOrderCheckoutWhenYouTryToBuyWithInvalidContactInformation() throws Exception {
		mvc.perform(post("/checkout")
				.flashAttr("cart", cart)
				.param("name", "")
				.param("address", "Address")
				.param("email", "1@1")
				.param("index", "1"))
				.andExpect(status().isOk())
				.andExpect(view().name("orderCheckout"))
				.andExpect(model().attribute("result", 1));

		mvc.perform(post("/checkout")
				.flashAttr("cart", cart)
				.param("name", "Name")
				.param("address", "")
				.param("email", "1@1")
				.param("index", "1"))
				.andExpect(status().isOk())
				.andExpect(view().name("orderCheckout"))
				.andExpect(model().attribute("result", 2));

		mvc.perform(post("/checkout")
				.flashAttr("cart", cart)
				.param("name", "Name")
				.param("address", "Address")
				.param("email", "")
				.param("index", "1"))
				.andExpect(status().isOk())
				.andExpect(view().name("orderCheckout"))
				.andExpect(model().attribute("result", 3));


		mvc.perform(post("/checkout")
				.flashAttr("cart", cart)
				.param("name", "Name")
				.param("address", "Address")
				.param("email", "")
				.param("index", "2"))
				.andExpect(status().isOk())
				.andExpect(view().name("orderCheckout"))
				.andExpect(model().attribute("result", 4));
	}

	@Test
	void returnsModelAndViewAfterPickupOrderCheckout() throws Exception {
		mvc.perform(post("/checkout")
				.flashAttr("cart", cart)
				.param("name", "Name")
				.param("address", "")
				.param("email", "1@1")
				.param("index", "0"))
				.andExpect(status().isOk())
				.andExpect(view().name("orderSummary"));

		assertEquals(1, orderService.findAll().stream().count());
	}

	@Test
	void returnsModelAndViewAfterDeliveryOrderCheckout() throws Exception {
		mvc.perform(post("/checkout")
				.flashAttr("cart", cart)
				.param("name", "Name")
				.param("address", "Adresse")
				.param("email", "1@1")
				.param("index", "1"))
				.andExpect(status().isOk())
				.andExpect(view().name("orderSummary"));

		assertEquals(1, orderService.findAll().stream().count());
	}

	@Test
	void returnsModelAndViewOrderOverviewWithNotExistingOrderId() throws Exception {
		mvc.perform(get("/order/someRandomId"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/order"));
	}

	@Test
	void returnsModelAndViewOrderOverviewWithPickupOrder() throws Exception {
		final Pickup pickup = orderService.orderPickupItem(cart, contactInformation);

		mvc.perform(get("/order/{id}", pickup.getId().getIdentifier()))
				.andExpect(view().name("orderOverview"))
				//.andExpect(model().attribute("items", ((ItemOrder) pickup).getOrderEntries()))
				.andExpect(model().attribute("order", pickup))
				.andExpect(status().isOk());
	}

	@Test
	void returnsModelAndViewOrderOverviewWithDeliveryOrder() throws Exception {
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
	void returnsModelAndViewOrderOverviewWithLKWCharter() throws Exception {
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
	void returnsModelAndViewOrderOverviewCancelItemOrderWithoutAuthentication() throws Exception {
		final Pickup pickup = orderService.orderPickupItem(cart, contactInformation);

		final String orderId = pickup.getId().getIdentifier();
		final long itemEntryId = pickup.getOrderEntries().get(0).getId();
		final Item item = pickup.getOrderEntries().get(0).getItem();

		mvc.perform(post("/order/{id}/cancelItem", pickup.getId().getIdentifier())
				.param("itemEntryId", String.valueOf(itemEntryId)))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/order/%s", pickup.getId().getIdentifier())));

		final Optional<ShopOrder> orderPostCancelOptional = orderService.findById(orderId);
		assertTrue(orderPostCancelOptional.isPresent(), "Order wurde nicht gefunden");

		final ItemOrder orderPostCancel = (ItemOrder) orderPostCancelOptional.get();
		assertEquals(itemEntryId, orderPostCancel.getOrderEntries().get(0).getId(), "Das Item wurde nicht erfolgreich storniert");
	}

	@Test
	void redirectOrderOverviewCancelItemWithInvalidOrderWithoutAuthentication() throws Exception {
		mvc.perform(post("/order/{id}/cancelItem", "random")
				.param("itemEntryId", "1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/order"));
	}

	@Test
	@WithMockUser(username = "admin", roles = "EMPLOYEE")
	void redirectOrderOverviewCancelItemWithInvalidOrderWithAuthentication() throws Exception {
		mvc.perform(post("/order/{id}/cancelItem", "random")
				.param("itemEntryId", "1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/orders"));
	}

	@Test
	@WithMockUser(username = "admin", roles = "EMPLOYEE")
	void returnsModelAndViewOrderOverviewChangeOrder() throws Exception {
		final Pickup pickup = orderService.orderPickupItem(cart, contactInformation);

		final String orderId = pickup.getId().getIdentifier();
		final long itemEntryId = pickup.getOrderEntries().get(0).getId();
		final Item item = pickup.getOrderEntries().get(0).getItem();

		mvc.perform(post("/order/{id}/changeStatus", pickup.getId().getIdentifier())
				.param("itemEntryId", String.valueOf(itemEntryId))
				.param("status", String.valueOf(OrderStatus.STORED)))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/order/%s", pickup.getId().getIdentifier())));

		final Optional<ShopOrder> orderPostCancelOptional = orderService.findById(orderId);
		assertTrue(orderPostCancelOptional.isPresent(), "Order wurde nicht gefunden");

		final ShopOrder orderPostCancel = orderPostCancelOptional.get();
		assertSame(OrderStatus.STORED, ((ItemOrder) orderPostCancel).getOrderEntries().get(0).getStatus(), "Der Status des Items wurde nicht erfolgreich geändert");
	}

	@Test
	void redirectOrderOverviewChangeOrderWithInvalidOrderWithoutAuthentication() throws Exception {
		mvc.perform(post("/order/{id}/changeStatus", "random")
				.param("itemEntryId", "1")
				.param("status", String.valueOf(OrderStatus.STORED)))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	@WithMockUser(username = "admin", roles = "EMPLOYEE")
	void redirectOrderOverviewChangeOrderWithInvalidOrderWithAuthentication() throws Exception {
		mvc.perform(post("/order/{id}/changeStatus", "random")
				.param("itemEntryId", "1")
				.param("status", String.valueOf(OrderStatus.STORED)))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/orders"));
	}

	@Test
	void returnsModelAndViewOrderOverviewCancelLkw() throws Exception {
		final Optional<LKW> olkw = lkwService.createCharterLKW(businessTime.getTime().toLocalDate(), LKWType.SMALL);
		Assert.isTrue(olkw.isPresent(), "LKW Charter konnte nicht erstellt werden");

		final LKWCharter charter = lkwService.createLKWOrder(olkw.orElse(null), businessTime.getTime().toLocalDate(), contactInformation);

		mvc.perform(post("/order/{id}/cancelLkw", charter.getId().getIdentifier()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"));

		assertTrue(orderService.findById(charter.getId().getIdentifier()).isEmpty(), "Die Order wurde nicht gelöscht");
	}

	@Test
	@WithMockUser(username = "admin", roles = "EMPLOYEE")
	void returnsModelAndViewOrderOverviewCancelLkwWithAuthentication() throws Exception {
		final Optional<LKW> olkw = lkwService.createCharterLKW(businessTime.getTime().toLocalDate(), LKWType.SMALL);
		Assert.isTrue(olkw.isPresent(), "LKW Charter konnte nicht erstellt werden");

		final LKWCharter charter = lkwService.createLKWOrder(olkw.orElse(null), businessTime.getTime().toLocalDate(), contactInformation);

		mvc.perform(post("/order/{id}/cancelLkw", charter.getId().getIdentifier()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/orders"));

		assertTrue(orderService.findById(charter.getId().getIdentifier()).isEmpty(), "Die Order wurde nicht gelöscht");
	}

	@Test
	void redirectOrderOverviewCancelLkwWithInvalidOrderWithoutAuthentication() throws Exception {
		mvc.perform(post("/order/{id}/cancelLkw", "random"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/order"));
	}

	@Test
	@WithMockUser(username = "admin", roles = "EMPLOYEE")
	void redirectOrderOverviewCancelLkwWithInvalidOrderWithAuthentication() throws Exception {
		mvc.perform(post("/order/{id}/cancelLkw", "random"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/orders"));
	}

	@Test
	@WithMockUser(username = "admin", roles = "EMPLOYEE")
	void returnsModelAndViewOrderOverviewSendMail() throws Exception {
		Pickup order = orderService.orderPickupItem(cart, contactInformation);

		mvc.perform(post("/order/{id}/sendUpdate", order.getId().getIdentifier()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/order/" + order.getId()));

		order = (Pickup) orderService.findById(order.getId().getIdentifier()).orElse(null);

		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(0).getId(), OrderStatus.STORED);

		mvc.perform(post("/order/{id}/sendUpdate", order.getId().getIdentifier()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/order/" + order.getId()));
	}

	@Test
	void redirectOrderOverviewSendMailWithoutAuthentication() throws Exception {
		mvc.perform(post("/order/{id}/sendUpdate", "random"))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	@WithMockUser(username = "admin", roles = "EMPLOYEE")
	void redirectOrderOverviewSendMailWithInvalidOrderWithAuthentication() throws Exception {
		mvc.perform(post("/order/{id}/sendUpdate", "random"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/orders"));
	}

}
