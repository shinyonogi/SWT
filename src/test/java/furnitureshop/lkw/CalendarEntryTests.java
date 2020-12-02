package furnitureshop.lkw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.Entity;
import java.lang.reflect.Modifier;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarEntryTests {

	DeliveryEntry entry;

	@BeforeEach
	void setUp() {
		entry = new DeliveryEntry(LocalDate.of(2021, 4, 2));
	}

	@Test
	void constructorWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> new DeliveryEntry(null),
				"DeliveryEntry.DeliveryEntry() should throw an IllegalArgumentException if the date argument is invalid!"
		);
	}

	@Test
	public void calendarEntryIsAbstract() {
		assertTrue(Modifier.isAbstract(CalendarEntry.class.getModifiers()), "CalendarEntry should be an abstract class!");
	}

	@Test
	void calenderEntryIsEntity() {
		assertTrue(CalendarEntry.class.isAnnotationPresent(Entity.class), "CalenderEntry must have @Entity!");
		assertTrue(DeliveryEntry.class.isAnnotationPresent(Entity.class), "DeliveryEntry must have @Entity!");
		assertTrue(CharterEntry.class.isAnnotationPresent(Entity.class), "CharterEntry must have @Entity!");
	}

	@Test
	public void deliveryEntrySetValidQuantity() {
		entry.setQuantity(2);
		assertEquals(2, entry.getQuantity(), "setQuantity should set the correct Quantity!");
	}

	@Test
	public void deliveryEntrySetNegativeQuality() {
		assertThrows(IllegalArgumentException.class, () -> entry.setQuantity(-1),
				"DeliveryEntry.setQuantity() should throw an IllegalArgumentException if the quantity argument is invalid!"
		);
	}

	@Test
	public void deliveryEntrySetInvalidQuantity() {
		assertThrows(IllegalArgumentException.class, () -> entry.setQuantity(5),
				"DeliveryEntry.setQuantity() should throw an IllegalArgumentException if the quantity argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> entry.setQuantity(25),
				"DeliveryEntry.setQuantity() should throw an IllegalArgumentException if the quantity argument is invalid!"
		);
	}
}
