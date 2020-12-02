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
//@DirtiesContext
@ContextConfiguration(classes = FurnitureShop.class)
public class LKWServiceTests {

	@Autowired
	LKWCatalog lkwCatalog;

	@Autowired
	LKWService lkwService;

	LocalDate weekendDate, validDate;

	@BeforeEach
	void setUp() {
		weekendDate = LocalDate.of(2023, 3, 19);
		validDate = LocalDate.of(2023, 3, 20);

		lkwCatalog.deleteAll();
		for (LKWType type : LKWType.values()) {
			for (int i = 0; i < 2; i++) {
				lkwCatalog.save(new LKW(type));
			}
		}
	}

	@Test
	void testCatalogFullSize() {
		assertEquals(lkwService.findAll().stream().count(), LKWType.values().length * 2, "LKWService.findAll() should find all LKWs!");
	}

	@Test
	void testCatalogCategorySize() {
		for (LKWType type : LKWType.values()) {
			assertEquals(lkwService.findByType(type).stream().count(), 2, "LKWService.findByType() should find the correct LKWs!");
		}
	}

	@Test
	void testFindNextAvailableDeliveryDate() {
		try {
			lkwService.findNextAvailableDeliveryDate(null, LKWType.SMALL);
			fail("LKWService.findNextAvailableDeliveryDate() should throw an IllegalArgumentException if the date argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}

		try {
			lkwService.findNextAvailableDeliveryDate(validDate, null);
			fail("LKWService.findNextAvailableDeliveryDate() should throw an IllegalArgumentException if the type argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}

		LocalDate available = lkwService.findNextAvailableDeliveryDate(weekendDate, LKWType.SMALL);
		assertEquals(validDate, available, "LKWService.findNextAvailableDeliveryDate() should find the correct date!");

		available = lkwService.findNextAvailableDeliveryDate(validDate, LKWType.SMALL);
		assertEquals(validDate, available, "LKWService.findNextAvailableDeliveryDate() should find the correct date!");
	}

	@Test
	void testIsDeliveryAvailable() {
		try {
			lkwService.isDeliveryAvailable(null, LKWType.SMALL);
			fail("LKWService.isDeliveryAvailable() should throw an IllegalArgumentException if the date argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}

		try {
			lkwService.isDeliveryAvailable(validDate, null);
			fail("LKWService.isDeliveryAvailable() should throw an IllegalArgumentException if the type argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}

		assertFalse(lkwService.isDeliveryAvailable(weekendDate, LKWType.SMALL), "LKW must not be available!");
		assertTrue(lkwService.isDeliveryAvailable(validDate, LKWType.SMALL), "LKW must be available!");

		lkwService.createCharterLKW(validDate, LKWType.SMALL);
		assertTrue(lkwService.isDeliveryAvailable(validDate, LKWType.SMALL), "LKW must be available!");

		for (int i = 0; i < DeliveryEntry.MAX_DELIVERY; i++) {
			lkwService.createDeliveryLKW(validDate, LKWType.SMALL);
		}

		assertFalse(lkwService.isDeliveryAvailable(validDate, LKWType.SMALL), "LKW must not be available!");
	}

	@Test
	void testCreateDeliveryLKW() {
		try {
			lkwService.createDeliveryLKW(null, LKWType.SMALL);
			fail("LKWService.createDeliveryLKW() should throw an IllegalArgumentException if the date argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}

		try {
			lkwService.createDeliveryLKW(validDate, null);
			fail("LKWService.createDeliveryLKW() should throw an IllegalArgumentException if the type argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}

		assertTrue(lkwService.createDeliveryLKW(weekendDate,LKWType.SMALL).isEmpty(), "LKW must not be available!");

		final Optional<LKW> lkw = lkwService.createDeliveryLKW(validDate, LKWType.SMALL);
		assertTrue(lkw.isPresent(), "LKW must be available");

		for (int i = 0; i < 3; i++) {
			final Optional<LKW> other = lkwService.createDeliveryLKW(validDate, LKWType.SMALL);
			assertTrue(other.isPresent(), "LKW must be available");
			assertEquals(lkw.get(), other.get(), "LKWs must be the same");
		}

		final Optional<LKW> charter = lkwService.createCharterLKW(validDate, LKWType.SMALL);
		assertTrue(charter.isPresent(), "LKW must be available");

		Optional<LKW> other = lkwService.createDeliveryLKW(validDate, LKWType.SMALL);
		assertTrue(other.isEmpty(), "LKW must not be available");

		lkwService.cancelOrder(charter.get(), validDate);

		for (int i = 0; i < DeliveryEntry.MAX_DELIVERY; i++) {
			other = lkwService.createDeliveryLKW(validDate, LKWType.SMALL);
			assertTrue(other.isPresent(), "LKW must be available");
			assertNotEquals(lkw.get(), other.get(), "LKWs must not be the same");
		}

		other = lkwService.createDeliveryLKW(validDate, LKWType.SMALL);
		assertTrue(other.isEmpty(), "LKW must not be available");
	}

	@Test
	void testIsCharterAvailable() {
		try {
			lkwService.isCharterAvailable(null, LKWType.SMALL);
			fail("LKWService.isCharterAvailable() should throw an IllegalArgumentException if the date argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}

		try {
			lkwService.isCharterAvailable(validDate, null);
			fail("LKWService.isCharterAvailable() should throw an IllegalArgumentException if the type argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}

		assertFalse(lkwService.isCharterAvailable(weekendDate, LKWType.SMALL), "LKW must not be available!");
		assertTrue(lkwService.isCharterAvailable(validDate, LKWType.SMALL), "LKW must be available!");

		lkwService.createCharterLKW(validDate, LKWType.SMALL);
		assertTrue(lkwService.isCharterAvailable(validDate, LKWType.SMALL), "LKW must be available!");

		final Optional<LKW> lkw = lkwService.createCharterLKW(validDate, LKWType.SMALL);
		assertTrue(lkw.isPresent(), "LKW must be available");
		assertFalse(lkwService.isCharterAvailable(validDate, LKWType.SMALL), "LKW must not be available!");

		lkwService.cancelOrder(lkw.get(), validDate);
		assertTrue(lkwService.isCharterAvailable(validDate, LKWType.SMALL), "LKW must be available!");

		lkwService.createDeliveryLKW(validDate, LKWType.SMALL);
		assertFalse(lkwService.isCharterAvailable(validDate, LKWType.SMALL), "LKW must not be available!");
	}

	@Test
	void testCreateCharterLKW() {
		try {
			lkwService.createCharterLKW(null, LKWType.SMALL);
			fail("LKWService.createCharterLKW() should throw an IllegalArgumentException if the date argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}

		try {
			lkwService.createCharterLKW(validDate, null);
			fail("LKWService.createCharterLKW() should throw an IllegalArgumentException if the type argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}
	}

	@Test
	void testCancelOrder() {
		try {
			lkwService.cancelOrder(null, validDate);
			fail("LKWService.cancelOrder() should throw an IllegalArgumentException if the LKW argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}

		try {
			lkwService.cancelOrder(new LKW(), null);
			fail("LKWService.cancelOrder() should throw an IllegalArgumentException if the type argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}
	}

}
