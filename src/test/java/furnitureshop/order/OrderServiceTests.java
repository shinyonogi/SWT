package furnitureshop.order;

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
import furnitureshop.supplier.SupplierService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = FurnitureShop.class)
public class OrderServiceTests {

	@Autowired
	ItemCatalog itemCatalog;

	@Autowired
	SupplierRepository supplierRepository;

	@Autowired
	LKWCatalog lkwCatalog;

	@Autowired
	OrderManagement<ShopOrder> orderManagement;

	@Autowired
	SupplierService supplierService;

	@Autowired
	LKWService lkwService;

	@Autowired
	OrderService orderService;

	@Autowired
	BusinessTime businessTime;

	Cart exampleCart;

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
				lkwCatalog.save(new LKW(LKWType.SMALL));
			}
		}

		final Supplier supplier = new Supplier("test", 0.2);
		supplierRepository.save(supplier);

		Piece stuhl1 = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), new byte[0], "schwarz",
				"", supplier, 5, Category.CHAIR);
		Piece sofa1_green = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), new byte[0], "grün",
				"", supplier, 50, Category.COUCH);

		itemCatalog.save(stuhl1);
		itemCatalog.save(sofa1_green);

		this.exampleCart = new Cart();

		exampleCart.addOrUpdateItem(stuhl1, 1);
		exampleCart.addOrUpdateItem(sofa1_green, 2);

		// Reset Time
		final LocalDateTime time = LocalDateTime.of(2020, 12, 21, 0, 0);
		final Duration delta = Duration.between(businessTime.getTime(), time);
		businessTime.forward(delta);
	}

	@Test
	void testOrderPickupItemWithInvalidType() {
		final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");

		assertThrows(IllegalArgumentException.class, () -> orderService.orderPickupItem(null, info),
				"orderPickupItem() should throw an IllegalArgumentException if the cart argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> orderService.orderPickupItem(exampleCart, null),
				"orderPickupItem() should throw an IllegalArgumentException if the contactInformation argument is invalid!"
		);
	}

	@Test
	void testOrderPickupItem() {
		final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");

		final Pickup order = orderService.orderPickupItem(exampleCart, info);

		final Pickup goalOrder = new Pickup(orderService.getDummyUser(), info);
		exampleCart.addItemsTo(goalOrder);

		assertEquals(info, order.getContactInformation(), "orderPickupItem() should use the correct ContactInformation!");

		final Iterator<OrderLine> goalOrderLineIterator = goalOrder.getOrderLines().get().iterator();
		final Iterator<OrderLine> orderOrderLineIterator = order.getOrderLines().get().iterator();

		for (int i = 0; i < goalOrder.getOrderLines().get().count(); i++) {
			final OrderLine goalOrderEntry = goalOrderLineIterator.next();
			final OrderLine orderOrderEntry = orderOrderLineIterator.next();

			assertEquals(goalOrderEntry.getProductName(), orderOrderEntry.getProductName(), "orderPickupItem() should add the correct Item!");
			assertEquals(goalOrderEntry.getPrice(), orderOrderEntry.getPrice(), "orderPickupItem() should add the correct Item!");
			assertEquals(goalOrderEntry.getQuantity(), orderOrderEntry.getQuantity(), "orderPickupItem() should add the correct Item!");
		}
	}

	@Test
	void testOrderDeliveryItemWithInvalidType() {
		final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");

		assertThrows(IllegalArgumentException.class, () -> orderService.orderDelieveryItem(null, info),
				"orderDelieveryItem() should throw an IllegalArgumentException if the cart argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> orderService.orderPickupItem(exampleCart, null),
				"orderDelieveryItem() should throw an IllegalArgumentException if the contactInformation argument is invalid!"
		);
	}

	@Test
	void testOrderDeliveryItem() {
		final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");

		final LocalDate deliveryDate = businessTime.getTime().toLocalDate().plusDays(2);

		final LKW lkw = lkwService.createDeliveryLKW(deliveryDate, LKWType.SMALL).orElse(null);
		final Delivery order = orderService.orderDelieveryItem(exampleCart, info);

		final Delivery goalOrder = new Delivery(orderService.getDummyUser(), info, lkw, deliveryDate);
		exampleCart.addItemsTo(goalOrder);

		assertEquals(deliveryDate, order.getDeliveryDate(), "orderDelieveryItem() should use the correct DeliveryDate!");
		assertEquals(info, order.getContactInformation(), "orderDelieveryItem() should use the correct ContactInformation!");
		assertEquals(lkw, order.getLkw(), "orderDelieveryItem() should use the correct LKW!");

		final Iterator<OrderLine> goalOrderLineIterator = goalOrder.getOrderLines().get().iterator();
		final Iterator<OrderLine> orderOrderLineIterator = order.getOrderLines().get().iterator();

		for (int i = 0; i < goalOrder.getOrderLines().get().count(); i++) {
			final OrderLine goalOrderEntry = goalOrderLineIterator.next();
			final OrderLine orderOrderEntry = orderOrderLineIterator.next();

			assertEquals(goalOrderEntry.getProductName(), orderOrderEntry.getProductName(), "orderDelieveryItem() should add the correct Item!");
			assertEquals(goalOrderEntry.getPrice(), orderOrderEntry.getPrice(), "orderDelieveryItem() should add the correct Item!");
			assertEquals(goalOrderEntry.getQuantity(), orderOrderEntry.getQuantity(), "orderDelieveryItem() should add the correct Item!");
		}
	}

	@Test
	void testOrderLKWWithInvalidType() {
		final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");
		final LocalDate date = businessTime.getTime().toLocalDate();
		final LKW lkw = lkwService.createCharterLKW(date, LKWType.SMALL).orElse(null);

		assertThrows(IllegalArgumentException.class, () -> orderService.orderLKW(null, date, info),
				"orderLKW() should throw an IllegalArgumentException if the lkw argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> orderService.orderLKW(lkw, null, info),
				"orderLKW() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> orderService.orderLKW(lkw, date, null),
				"orderLKW() should throw an IllegalArgumentException if the contactInformation argument is invalid!"
		);
	}

	@Test
	void testOrderLKW() {
		final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");

		final LocalDate date = businessTime.getTime().toLocalDate();
		final LKW lkw = lkwService.createCharterLKW(date, LKWType.SMALL).orElse(null);

		final LKWCharter order = orderService.orderLKW(lkw, date, info);

		assertEquals(date, order.getRentDate(), "orderLKW() should use the correct RentDate!");
		assertEquals(info, order.getContactInformation(), "orderLKW() should use the correct ContactInformation!");
		assertEquals(lkw, order.getLkw(), "orderLKW() should use the correct LKW!");
	}

	@Test
	void testChangeItemEntryStatusWithInvalidType() {
		final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");

		final Pickup order = new Pickup(orderService.getDummyUser(), info);

		assertThrows(IllegalArgumentException.class, () -> orderService.changeItemEntryStatus(null, 0, OrderStatus.CANCELLED),
				"changeItemEntryStatus() should throw an IllegalArgumentException if the order argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> orderService.changeItemEntryStatus(order, 0, null),
				"changeItemEntryStatus() should throw an IllegalArgumentException if the status argument is invalid!"
		);
	}

	@Test
	void testChangeItemEntryStatus() {
		final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");

		Pickup order = orderService.orderPickupItem(exampleCart, info);
		order = (Pickup) orderService.findById(order.getId().getIdentifier()).orElse(null);

		final long id = order.getOrderEntries().get(0).getId();

		assertTrue(orderService.changeItemEntryStatus(order, id, OrderStatus.COMPLETED), "changeItemEntryStatus() should change the status!");
		assertEquals(OrderStatus.COMPLETED, order.getOrderEntries().get(0).getStatus(), "changeItemEntryStatus() should change the status!");

		assertFalse(orderService.changeItemEntryStatus(order, -1, OrderStatus.COMPLETED), "changeItemEntryStatus() should not change the status!");
	}

	@Test
	void testCancelLKWWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> orderService.cancelLKW(null),
				"cancelLKW() should throw an IllegalArgumentException if the order argument is invalid!"
		);
	}

	@Test
	void testCancelLKW() {
		final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");

		final LocalDate date = businessTime.getTime().toLocalDate();
		final LKW lkw = lkwService.createCharterLKW(date, LKWType.SMALL).orElse(null);

		final LKWCharter order = orderService.orderLKW(lkw, date, info);
		final String id = order.getId().getIdentifier();

		assertTrue(orderService.cancelLKW(order), "cancelLKW() should return the correct value!");
		assertTrue(orderService.findById(id).isEmpty(), "cancelLKW() should cancel the Order!");
	}

	@Test
	void testRemoveItemFromOrdersWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> orderService.removeItemFromOrders(null),
				"cancelLKW() should throw an IllegalArgumentException if the item argument is invalid!"
		);
	}

	@Test
	void testRemoveItemFromOrders() {
		final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");
		final List<Product> items = exampleCart.get().map(CartItem::getProduct).collect(Collectors.toList());
		final Item item = (Item) items.get(0);

		final String id = orderService.orderPickupItem(exampleCart, info).getId().getIdentifier();
		orderService.removeItemFromOrders(item);

		final ItemOrder itemOrder = (ItemOrder) orderService.findById(id).get();
		assertTrue(() -> itemOrder.getOrderEntries().stream().noneMatch(entry -> entry.getItem().equals(item)), "removeItemFromOrders() should remove simular Items!");

		final Item item2 = (Item) items.get(1);

		orderService.removeItemFromOrders(item2);
		assertTrue(orderService.findById(id).isEmpty(), "removeItemFromOrders() should remove empty Orders!");
	}

	@Test
	void testGetStatusWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> orderService.getStatus(null),
				"cancelLKW() should throw an IllegalArgumentException if the order argument is invalid!"
		);
	}

	@Test
	void testGetStatus() {
		final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");
		final LKW lkw = new LKW(LKWType.SMALL);

		LocalDate date = businessTime.getTime().toLocalDate().minusDays(1);
		LKWCharter lkwOrder = new LKWCharter(orderService.getDummyUser(), info, lkw, date);
		assertEquals(OrderStatus.COMPLETED, orderService.getStatus(lkwOrder), "getStatus() should return the correct OrderStatus!");

		date = businessTime.getTime().toLocalDate().plusDays(1);
		lkwOrder = new LKWCharter(orderService.getDummyUser(), info, lkw, date);
		assertEquals(OrderStatus.PAID, orderService.getStatus(lkwOrder), "getStatus() should return the correct OrderStatus!");

		final Pickup itemOrder = new Pickup(orderService.getDummyUser(), info);
		assertEquals(OrderStatus.OPEN, orderService.getStatus(itemOrder), "getStatus() should return the correct OrderStatus!");

		exampleCart.addItemsTo(itemOrder);
		assertEquals(OrderStatus.OPEN, orderService.getStatus(itemOrder), "getStatus() should return the correct OrderStatus!");

		itemOrder.changeAllStatus(OrderStatus.PAID);
		assertEquals(OrderStatus.PAID, orderService.getStatus(itemOrder), "getStatus() should return the correct OrderStatus!");

		itemOrder.changeStatus(0, OrderStatus.COMPLETED);
		assertEquals(OrderStatus.PAID, orderService.getStatus(itemOrder), "getStatus() should return the correct OrderStatus!");
	}

	@Test
	void testFindByIdWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> orderService.findById(null),
				"findById() should throw an IllegalArgumentException if the identifier argument is invalid!"
		);
	}

	@Test
	void testFindById() {
		final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");
		final Pickup order = orderService.orderPickupItem(exampleCart, info);

		final Optional<ShopOrder> shopOrder = orderService.findById(order.getId().getIdentifier());
		assertTrue(shopOrder.isPresent(), "findById() should find the correct Order!");
		assertEquals(order, shopOrder.get(), "findById() should find the correct Order!");

		assertTrue(orderService.findById("id").isEmpty(), "findById() should not find an Order!");
	}

	@Test
	void testFindAll() {
		for (int i = 0; i < 10; i++) {
			final ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");
			orderService.orderPickupItem(exampleCart, info);
		}

		assertEquals(10L, orderService.findAll().stream().count(), "findAll() should find all Orders!");
	}

}
