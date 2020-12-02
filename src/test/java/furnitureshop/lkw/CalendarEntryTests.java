package furnitureshop.lkw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarEntryTests {

	private DeliveryEntry entry;

	@BeforeEach
	void setUp() {
		entry = new DeliveryEntry(LocalDate.of(2021, 4, 2));
	}

	@Test
	public void calendarEntryIsAbstract() {
		assertTrue(Modifier.isAbstract(CalendarEntry.class.getModifiers()), "CalendarEntry should be an abstract class!");
	}

	@Test
	public void deliveryEntrySetValidQuantity() {
		entry.setQuantity(2);
		assertEquals(entry.getQuantity(), 2, "setQuantity should set the correct Quantity!");
	}

	@Test
	public void deliveryEntrySetNegativeQuality() {
		try {
			entry.setQuantity(-1);
			fail("DeliveryEntry.setQuantity() should throw an IllegalArgumentException if the quantity argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}
	}

	@Test
	public void deliveryEntrySetInvalidQuantity() {
		try {
			entry.setQuantity(5);
			fail("DeliveryEntry.setQuantity() should throw an IllegalArgumentException if the quantity argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}

		try {
			entry.setQuantity(25);
			fail("DeliveryEntry.setQuantity() should throw an IllegalArgumentException if the quantity argument is invalid!");
		} catch (IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}
	}
}
