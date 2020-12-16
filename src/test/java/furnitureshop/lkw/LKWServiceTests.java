package furnitureshop.lkw;

import furnitureshop.FurnitureShop;
import furnitureshop.order.OrderService;
import furnitureshop.order.ShopOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.OrderManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = FurnitureShop.class)
public class LKWServiceTests {

	@Autowired
	LKWCatalog lkwCatalog;

	@Autowired
	LKWService lkwService;

	@Autowired
	OrderManagement<ShopOrder> orderManagement;

	@Autowired
	OrderService orderService;

	LocalDate weekendDate, validDate;
	LKWType type;

	@BeforeEach
	void setUp() {
		for (ShopOrder order : orderManagement.findBy(orderService.getDummyUser().get())) {
			orderManagement.delete(order);
		}
		this.weekendDate = LocalDate.of(2023, 3, 19);
		this.validDate = LocalDate.of(2023, 3, 20);
		this.type = LKWType.SMALL;

		lkwCatalog.deleteAll();
		for (LKWType type : LKWType.values()) {
			for (int i = 0; i < 2; i++) {
				lkwCatalog.save(new LKW(type));
			}
		}
	}

	@Test
	void testCatalogFullSize() {
		assertEquals(LKWType.values().length * 2L, lkwService.findAll().stream().count(), "findAll() should find all LKWs!");
	}

	@Test
	void testCatalogCategorySize() {
		for (LKWType type : LKWType.values()) {
			assertEquals(2, lkwService.findByType(type).stream().count(), "findByType() should find the correct LKWs!");
		}
	}

	@Test
	void testFindNextAvailableDeliveryDateWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> lkwService.findNextAvailableDeliveryDate(null, type),
				"findNextAvailableDeliveryDate() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> lkwService.findNextAvailableDeliveryDate(validDate, null),
				"findNextAvailableDeliveryDate() should throw an IllegalArgumentException if the type argument is invalid!"
		);
	}

	@Test
	void testFindNextAvailableDeliveryDate() {
		LocalDate available = lkwService.findNextAvailableDeliveryDate(weekendDate, type);
		assertEquals(validDate, available, "findNextAvailableDeliveryDate() should find the correct date!");

		available = lkwService.findNextAvailableDeliveryDate(validDate, type);
		assertEquals(validDate, available, "findNextAvailableDeliveryDate() should find the correct date!");
	}

	@Test
	void testIsDeliveryAvailableWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> lkwService.isDeliveryAvailable(null, type),
				"isDeliveryAvailable() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> lkwService.isDeliveryAvailable(validDate, null),
				"isDeliveryAvailable() should throw an IllegalArgumentException if the type argument is invalid!"
		);
	}

	@Test
	void testIsDeliveryAvailable() {
		assertFalse(lkwService.isDeliveryAvailable(weekendDate, type), "isDeliveryAvailable() should not find a available LKW on a weekend!");
		assertTrue(lkwService.isDeliveryAvailable(validDate, type), "isDeliveryAvailable() should find a available LKW!");

		lkwService.createCharterLKW(validDate, type);
		assertTrue(lkwService.isDeliveryAvailable(validDate, type), "isDeliveryAvailable() should find a available LKW!");

		for (int i = 0; i < DeliveryEntry.MAX_DELIVERY; i++) {
			lkwService.createDeliveryLKW(validDate, type);
		}

		assertFalse(lkwService.isDeliveryAvailable(validDate, type), "isDeliveryAvailable() should not find a available LKW!");
	}

	@Test
	void testCreateDeliveryLKWWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> lkwService.createDeliveryLKW(null, type),
				"createDeliveryLKW() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> lkwService.createDeliveryLKW(validDate, null),
				"createDeliveryLKW() should throw an IllegalArgumentException if the type argument is invalid!"
		);
	}

	@Test
	void testCreateDeliveryLKW() {
		assertTrue(lkwService.createDeliveryLKW(weekendDate, type).isEmpty(), "createDeliveryLKW() should not find a available LKW on a weekend!");

		final Optional<LKW> lkw = lkwService.createDeliveryLKW(validDate, type);
		assertTrue(lkw.isPresent(), "createDeliveryLKW() should find a available LKW!");

		for (int i = 1; i < 4; i++) {
			final Optional<LKW> other = lkwService.createDeliveryLKW(validDate, type);
			assertTrue(other.isPresent(), "createDeliveryLKW() should find a available LKW!");
			assertEquals(lkw.get(), other.get(), "createDeliveryLKW() should find the correct LKW!");

			final DeliveryEntry entry = (DeliveryEntry) other.get().getCalendar().getEntry(validDate).orElseGet(null);
			assertEquals(i + 1, entry.getQuantity(), "createDeliveryLKW should set the correct quantity!");
		}

		final Optional<LKW> charter = lkwService.createCharterLKW(validDate, type);
		assertTrue(charter.isPresent(), "createCharterLKW() should find a available LKW!");

		Optional<LKW> other = lkwService.createDeliveryLKW(validDate, type);
		assertTrue(other.isEmpty(), "createDeliveryLKW() should not find a available LKW!");

		lkwService.cancelOrder(charter.get(), validDate);

		for (int i = 0; i < DeliveryEntry.MAX_DELIVERY; i++) {
			other = lkwService.createDeliveryLKW(validDate, type);
			assertTrue(other.isPresent(), "createDeliveryLKW() should find a available LKW!");
			assertNotEquals(lkw.get(), other.get(), "createDeliveryLKW() should find the correct LKW!");
		}

		other = lkwService.createDeliveryLKW(validDate, type);
		assertTrue(other.isEmpty(), "createDeliveryLKW() should not find a available LKW!");
	}

	@Test
	void testIsCharterAvailableWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> lkwService.isCharterAvailable(null, type),
				"isCharterAvailable() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> lkwService.isCharterAvailable(validDate, null),
				"isCharterAvailable() should throw an IllegalArgumentException if the type argument is invalid!"
		);
	}

	@Test
	void testIsCharterAvailable() {
		assertFalse(lkwService.isCharterAvailable(weekendDate, type), "isCharterAvailable() should not find a available LKW on a weekend!");

		assertTrue(lkwService.isCharterAvailable(validDate, type), "isCharterAvailable() should find a available LKW!");

		lkwService.createCharterLKW(validDate, type);
		assertTrue(lkwService.isCharterAvailable(validDate, type), "isCharterAvailable() should find a available LKW!");

		final Optional<LKW> lkw = lkwService.createCharterLKW(validDate, type);
		assertTrue(lkw.isPresent(), "createCharterLKW() should find a available LKW!");
		assertFalse(lkwService.isCharterAvailable(validDate, type), "isCharterAvailable() should not find a available LKW!");

		lkwService.cancelOrder(lkw.get(), validDate);
		assertTrue(lkwService.isCharterAvailable(validDate, type), "isCharterAvailable() should find a available LKW!");

		lkwService.createDeliveryLKW(validDate, type);
		assertFalse(lkwService.isCharterAvailable(validDate, type), "isCharterAvailable() should not find a available LKW!");
	}

	@Test
	void testCreateCharterLKWWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> lkwService.createCharterLKW(null, type),
				"createCharterLKW() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> lkwService.createCharterLKW(validDate, null),
				"createCharterLKW() should throw an IllegalArgumentException if the type argument is invalid!"
		);
	}

	@Test
	void testCreateCharterLKW() {
		assertTrue(lkwService.createCharterLKW(weekendDate, type).isEmpty(), "createCharterLKW() should not find a available LKW on a weekend!");

		assertTrue(lkwService.createCharterLKW(validDate, type).isPresent(), "createCharterLKW() should find a available LKW!");

		final Optional<LKW> lkw = lkwService.createCharterLKW(validDate, type);
		assertTrue(lkw.isPresent(), "createCharterLKW() should find a available LKW!");

		assertTrue(lkwService.createCharterLKW(validDate, type).isEmpty(), "createCharterLKW() should not find a available LKW!");

		lkwService.cancelOrder(lkw.get(), validDate);
		assertTrue(lkwService.createDeliveryLKW(validDate, type).isPresent(), "createDeliveryLKW() should find a available LKW!");

		assertTrue(lkwService.createCharterLKW(validDate, type).isEmpty(), "createCharterLKW() should not find a available LKW!");
	}

	@Test
	void testCancelOrderWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> lkwService.cancelOrder(null, validDate),
				"cancelOrder() should throw an IllegalArgumentException if the LKW argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> lkwService.cancelOrder(new LKW(type), null),
				"cancelOrder() should throw an IllegalArgumentException if the date argument is invalid!"
		);
	}

	@Test
	void testCancelOrder() {
		final Optional<LKW> delivery1 = lkwService.createDeliveryLKW(validDate, type);
		assertTrue(delivery1.isPresent(), "createDeliveryLKW() should find a available LKW!");

		final Optional<LKW> delivery2 = lkwService.createDeliveryLKW(validDate, type);
		assertTrue(delivery2.isPresent(), "createDeliveryLKW() should find a available LKW!");
		assertEquals(delivery1.get(), delivery2.get(), "createDeliveryLKW() should find the correct LKW!");

		final Optional<LKW> charter = lkwService.createCharterLKW(validDate, type);
		assertTrue(charter.isPresent(), "createCharterLKW() should find a available LKW!");

		assertTrue(lkwService.cancelOrder(charter.get(), validDate), "cancelOrder() should cancel the Order!");
		assertFalse(charter.get().getCalendar().hasEntry(validDate), "cancelOrder() should remove the CalendarEntry!");

		assertTrue(lkwService.cancelOrder(delivery2.get(), validDate), "cancelOrder() should cancel the Order!");
		assertTrue(delivery2.get().getCalendar().getEntry(validDate).isPresent(), "cancelOrder() should reduce the DeliverEntry quantity to 1!");

		final DeliveryEntry entry = (DeliveryEntry) delivery2.get().getCalendar().getEntry(validDate).get();
		assertEquals(1, entry.getQuantity(), "cancelOrder() should reduce the DeliverEntry quantity to 1!");

		assertTrue(lkwService.cancelOrder(delivery2.get(), validDate), "cancelOrder() should cancel the Order!");
		assertFalse(delivery2.get().getCalendar().hasEntry(validDate), "cancelOrder() should remove the CalendarEntry!");

		assertFalse(lkwService.cancelOrder(delivery2.get(), validDate), "cancelOrder() should not cancel the Order");
	}

}
