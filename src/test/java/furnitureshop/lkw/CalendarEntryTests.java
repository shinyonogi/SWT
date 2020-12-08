package furnitureshop.lkw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product;

import javax.persistence.Entity;
import java.lang.reflect.Modifier;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarEntryTests {

	DeliveryEntry entry;

	@BeforeEach
	void setUp() {
		this.entry = new DeliveryEntry(LocalDate.of(2021, 4, 2));
	}

	@Test
	void testConstructorWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> new DeliveryEntry(null),
				"DeliveryEntry() should throw an IllegalArgumentException if the date argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new CharterEntry(null),
				"CharterEntry() should throw an IllegalArgumentException if the date argument is invalid!"
		);
	}

	@Test
	void testDeliveryEntrySetValidQuantity() {
		entry.setQuantity(2);
		assertEquals(2, entry.getQuantity(), "setQuantity() should set the correct Quantity!");
	}

	@Test
	void testDeliveryEntrySetNegativeQuantity() {
		assertThrows(IllegalArgumentException.class, () -> entry.setQuantity(-1),
				"setQuantity() should throw an IllegalArgumentException if the quantity argument is negative!"
		);
	}

	@Test
	void testDeliveryEntrySetInvalidQuantity() {
		assertThrows(IllegalArgumentException.class, () -> entry.setQuantity(5),
				"setQuantity() should throw an IllegalArgumentException if the quantity argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> entry.setQuantity(25),
				"setQuantity() should throw an IllegalArgumentException if the quantity argument is invalid!"
		);
	}

	@Test
	void testCalendarEntryIsAbstract() {
		assertTrue(Modifier.isAbstract(CalendarEntry.class.getModifiers()), "CalendarEntry should be an abstract class!");
	}

	@Test
	void testCalendarEntryIsChild() {
		assertTrue(CalendarEntry.class.isAssignableFrom(DeliveryEntry.class), "DeliveryEntry must extends CalendarEntry!");
		assertTrue(CalendarEntry.class.isAssignableFrom(CharterEntry.class), "CharterEntry must extends CalendarEntry!");
	}

	@Test
	void testCalenderEntryIsEntity() {
		assertTrue(CalendarEntry.class.isAnnotationPresent(Entity.class), "CalenderEntry must have @Entity!");
		assertTrue(DeliveryEntry.class.isAnnotationPresent(Entity.class), "DeliveryEntry must have @Entity!");
		assertTrue(CharterEntry.class.isAnnotationPresent(Entity.class), "CharterEntry must have @Entity!");
	}

}
