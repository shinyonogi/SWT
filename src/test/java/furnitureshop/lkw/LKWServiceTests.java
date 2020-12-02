package furnitureshop.lkw;

import furnitureshop.FurnitureShop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

	LocalDate weekendDate, validDate;
	LKWType type;

	@BeforeEach
	void setUp() {
		weekendDate = LocalDate.of(2023, 3, 19);
		validDate = LocalDate.of(2023, 3, 20);
		type = LKWType.SMALL;

		lkwCatalog.deleteAll();
		for (LKWType type : LKWType.values()) {
			for (int i = 0; i < 2; i++) {
				lkwCatalog.save(new LKW(type));
			}
		}
	}

	@Test
	void testCatalogFullSize() {
		assertEquals(LKWType.values().length * 2L, lkwService.findAll().stream().count(), "LKWService.findAll() should find all LKWs!");
	}

	@Test
	void testCatalogCategorySize() {
		for (LKWType type : LKWType.values()) {
			assertEquals(2, lkwService.findByType(type).stream().count(), "LKWService.findByType() should find the correct LKWs!");
		}
	}

	@Test
	void testFindNextAvailableDeliveryDate() {
		assertThrows(IllegalArgumentException.class, () -> lkwService.findNextAvailableDeliveryDate(null, type),
				"LKWService.findNextAvailableDeliveryDate() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> lkwService.findNextAvailableDeliveryDate(validDate, null),
				"LKWService.findNextAvailableDeliveryDate() should throw an IllegalArgumentException if the type argument is invalid!"
		);

		LocalDate available = lkwService.findNextAvailableDeliveryDate(weekendDate, type);
		assertEquals(validDate, available, "LKWService.findNextAvailableDeliveryDate() should find the correct date!");

		available = lkwService.findNextAvailableDeliveryDate(validDate, type);
		assertEquals(validDate, available, "LKWService.findNextAvailableDeliveryDate() should find the correct date!");
	}

	@Test
	void testIsDeliveryAvailable() {
		assertThrows(IllegalArgumentException.class, () -> lkwService.isDeliveryAvailable(null, type),
				"LKWService.isDeliveryAvailable() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> lkwService.isDeliveryAvailable(validDate, null),
				"LKWService.isDeliveryAvailable() should throw an IllegalArgumentException if the type argument is invalid!"
		);

		assertFalse(lkwService.isDeliveryAvailable(weekendDate, type), "LKW must not be available!");
		assertTrue(lkwService.isDeliveryAvailable(validDate, type), "LKW must be available!");

		lkwService.createCharterLKW(validDate, type);
		assertTrue(lkwService.isDeliveryAvailable(validDate, type), "LKW must be available!");

		for (int i = 0; i < DeliveryEntry.MAX_DELIVERY; i++) {
			lkwService.createDeliveryLKW(validDate, type);
		}

		assertFalse(lkwService.isDeliveryAvailable(validDate, type), "LKW must not be available!");
	}

	@Test
	void testCreateDeliveryLKW() {
		assertThrows(IllegalArgumentException.class, () -> lkwService.createDeliveryLKW(null, type),
				"LKWService.createDeliveryLKW() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> lkwService.createDeliveryLKW(validDate, null),
				"LKWService.createDeliveryLKW() should throw an IllegalArgumentException if the type argument is invalid!"
		);

		assertTrue(lkwService.createDeliveryLKW(weekendDate, type).isEmpty(), "LKW must not be available!");

		final Optional<LKW> lkw = lkwService.createDeliveryLKW(validDate, type);
		assertTrue(lkw.isPresent(), "LKW must be available");

		for (int i = 1; i < 4; i++) {
			final Optional<LKW> other = lkwService.createDeliveryLKW(validDate, type);
			assertTrue(other.isPresent(), "LKW must be available");
			assertEquals(lkw.get(), other.get(), "LKWs must be the same");

			final DeliveryEntry entry = ((DeliveryEntry) other.get().getCalendar().getEntry(validDate).orElseGet(null));
			assertEquals(i + 1, entry.getQuantity(), "Quantity must be valid");
		}

		final Optional<LKW> charter = lkwService.createCharterLKW(validDate, type);
		assertTrue(charter.isPresent(), "LKW must be available");

		Optional<LKW> other = lkwService.createDeliveryLKW(validDate, type);
		assertTrue(other.isEmpty(), "LKW must not be available");

		lkwService.cancelOrder(charter.get(), validDate);

		for (int i = 0; i < DeliveryEntry.MAX_DELIVERY; i++) {
			other = lkwService.createDeliveryLKW(validDate, type);
			assertTrue(other.isPresent(), "LKW must be available");
			assertNotEquals(lkw.get(), other.get(), "LKWs must not be the same");
		}

		other = lkwService.createDeliveryLKW(validDate, type);
		assertTrue(other.isEmpty(), "LKW must not be available");
	}

	@Test
	void testIsCharterAvailable() {
		assertThrows(IllegalArgumentException.class, () -> lkwService.isCharterAvailable(null, type),
				"LKWService.isCharterAvailable() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> lkwService.isCharterAvailable(validDate, null),
				"LKWService.isCharterAvailable() should throw an IllegalArgumentException if the type argument is invalid!"
		);

		assertFalse(lkwService.isCharterAvailable(weekendDate, type), "LKW must not be available!");

		assertTrue(lkwService.isCharterAvailable(validDate, type), "LKW must be available!");

		lkwService.createCharterLKW(validDate, type);
		assertTrue(lkwService.isCharterAvailable(validDate, type), "LKW must be available!");

		final Optional<LKW> lkw = lkwService.createCharterLKW(validDate, type);
		assertTrue(lkw.isPresent(), "LKW must be available");
		assertFalse(lkwService.isCharterAvailable(validDate, type), "LKW must not be available!");

		lkwService.cancelOrder(lkw.get(), validDate);
		assertTrue(lkwService.isCharterAvailable(validDate, type), "LKW must be available!");

		lkwService.createDeliveryLKW(validDate, type);
		assertFalse(lkwService.isCharterAvailable(validDate, type), "LKW must not be available!");
	}

	@Test
	void testCreateCharterLKW() {
		assertThrows(IllegalArgumentException.class, () -> lkwService.createCharterLKW(null, type),
				"LKWService.createCharterLKW() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> lkwService.createCharterLKW(validDate, null),
				"LKWService.createCharterLKW() should throw an IllegalArgumentException if the type argument is invalid!"
		);

		assertTrue(lkwService.createCharterLKW(weekendDate, type).isEmpty(), "LKW must not be available!");

		assertTrue(lkwService.createCharterLKW(validDate, type).isPresent(), "LKW must be available");

		final Optional<LKW> lkw = lkwService.createCharterLKW(validDate, type);
		assertTrue(lkw.isPresent(), "LKW must be available");

		assertTrue(lkwService.createCharterLKW(validDate, type).isEmpty(), "LKW must not be available");

		lkwService.cancelOrder(lkw.get(), validDate);
		assertTrue(lkwService.createDeliveryLKW(validDate, type).isPresent(), "LKW must be available");

		assertTrue(lkwService.createCharterLKW(validDate, type).isEmpty(), "LKW must not be available");
	}

	@Test
	void testCancelOrder() {
		assertThrows(IllegalArgumentException.class, () -> lkwService.cancelOrder(null, validDate),
				"LKWService.cancelOrder() should throw an IllegalArgumentException if the LKW argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> lkwService.cancelOrder(new LKW(), null),
				"LKWService.cancelOrder() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		final Optional<LKW> delivery1 = lkwService.createDeliveryLKW(validDate, type);
		assertTrue(delivery1.isPresent(), "LKW must be available");

		final Optional<LKW> delivery2 = lkwService.createDeliveryLKW(validDate, type);
		assertTrue(delivery2.isPresent(), "LKW must be available");
		assertEquals(delivery1.get(), delivery2.get(), "LKWs must be the same");

		final Optional<LKW> charter = lkwService.createCharterLKW(validDate, type);
		assertTrue(charter.isPresent(), "LKW must be available");

		assertTrue(lkwService.cancelOrder(charter.get(), validDate), "LKW must be available");
		assertFalse(charter.get().getCalendar().hasEntry(validDate), "LKW must not have an entry");

		assertTrue(lkwService.cancelOrder(delivery2.get(), validDate), "LKW must be available");
		assertTrue(delivery2.get().getCalendar().getEntry(validDate).isPresent(), "LKW must have an entry");

		final DeliveryEntry entry = (DeliveryEntry) delivery2.get().getCalendar().getEntry(validDate).get();
		assertEquals(1, entry.getQuantity(), "Quantity must be valid");

		assertTrue(lkwService.cancelOrder(delivery2.get(), validDate), "LKW must be available");
		assertFalse(delivery2.get().getCalendar().hasEntry(validDate), "LKW must not have an entry");

		assertFalse(lkwService.cancelOrder(delivery2.get(), validDate), "LKW must not have an entry");
	}

}
