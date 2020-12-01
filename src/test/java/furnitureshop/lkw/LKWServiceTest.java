package furnitureshop.lkw;

import furnitureshop.FurnitureShop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@ContextConfiguration(classes = FurnitureShop.class)
public class LKWServiceTest {

	@Autowired
	LKWCatalog lkwCatalog;

	@Autowired
	LKWService lkwService;

	LocalDate oldDate, weekendDate, validDate;

	@BeforeEach
	void setUp() {
		oldDate = LocalDate.of(2000, 1, 1);
		weekendDate = LocalDate.of(2023, 3, 19);
		validDate = LocalDate.of(2023, 3, 20);
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
		assertEquals(available, validDate, "LKWService.findNextAvailableDeliveryDate() should find the correct date!");
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
