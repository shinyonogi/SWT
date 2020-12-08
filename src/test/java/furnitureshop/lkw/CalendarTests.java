package furnitureshop.lkw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.Entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;

public class CalendarTests {

	Calendar calendar;
	LocalDate friday, wednesday, thursday;

	@BeforeEach
	void setUp() {
		this.calendar = new Calendar();

		this.friday = LocalDate.of(2021, 4, 2);
		this.wednesday = LocalDate.of(2021, 4, 7);
		this.thursday = LocalDate.of(2021, 4, 8);
	}

	@Test
	void testAddEntryInvalidWithType() {
		assertThrows(IllegalArgumentException.class, () -> calendar.addEntry(null),
				"addEntry() should throw an IllegalArgumentException if the entry argument is null!"
		);
	}

	@Test
	void testAddEntryIfEntryAlreadyExists() {
		assertTrue(calendar.addEntry(new DeliveryEntry(friday)), "addEntry() should be able to add the entry to the Calendar when the date is available!");
		assertFalse(calendar.addEntry(new CharterEntry(friday)), "addEntry() should not be abe to add the entry to the Calendar when the date is unavailable!");

		assertTrue(calendar.addEntry(new DeliveryEntry(wednesday)), "addEntry() should be able to add the entry to the Calendar when the date is available!");
	}

	@Test
	void testRemoveEntryIfEntryExists() {
		final DeliveryEntry deliveryFriday = new DeliveryEntry(friday);
		calendar.addEntry(deliveryFriday);

		assertTrue(calendar.getEntry(friday).isPresent(), "getEntry() should return entry when entry exists in Calendar!");
		assertTrue(calendar.removeEntry(friday), "removeEntry() should return false when entry can be removed from Calendar!");
		assertTrue(calendar.getEntry(friday).isEmpty(), "getEntry() should not return the entry if entry was removed from Calendar!");
	}

	@Test
	void testRemoveEntryInvalidWithType() {
		assertThrows(IllegalArgumentException.class, () -> calendar.removeEntry(null),
				"removeEntry() should throw an IllegalArgumentException if the date argument is null!"
		);
	}

	@Test
	void testRemoveEntryIfCalendarEmpty() {
		assertTrue(calendar.getEntries().isEmpty(), "getEntries() should not return any entries if the Calendar is empty!");
		assertFalse(calendar.removeEntry(wednesday), "removeEntry() should not return true when the Calendar is empty!");
	}

	@Test
	void testHasEntryInvalidWithType() {
		assertThrows(IllegalArgumentException.class, () -> calendar.hasEntry(null),
				"hasEntry() should throw an IllegalArgumentException if the date argument is null!"
		);
	}

	@Test
	void testHasEntryIfEntryExists() {
		calendar.addEntry(new DeliveryEntry(wednesday));
		assertTrue(calendar.hasEntry(wednesday), "hasEntry() should not return false if entry was added to Calendar!");
	}

	@Test
	void testHasEntryIfEntryNotExists() {
		calendar.addEntry(new DeliveryEntry(wednesday));
		assertFalse(calendar.hasEntry(thursday), "hasEntry() should not return true if entry was never added to Calendar!");
	}

	@Test
	void testGetEntryInvalidWithType() {
		assertThrows(IllegalArgumentException.class, () -> calendar.getEntry(null),
				"getEntry() should throw an IllegalArgumentException if the date argument is null!"
		);
	}

	@Test
	void testGetEntryIfEntryNotExists() {
		calendar.addEntry(new DeliveryEntry(friday));
		assertTrue(calendar.getEntry(thursday).isEmpty(), "getEntry() should not return an entry if the given entry does not exists in the Calendar!");
	}

	@Test
	void testGetEntryIfEntryExists() {
		final DeliveryEntry entry = new DeliveryEntry(wednesday);
		calendar.addEntry(entry);

		final Optional<CalendarEntry> getEntry = calendar.getEntry(wednesday);

		assertTrue(getEntry.isPresent(), "getEntry() should return entry when entry exists in Calendar!");
		assertEquals(entry, getEntry.get(), "getEntry() should return the correct entry for the given date!");
	}

	@Test
	void testCalendarIsEntity() {
		assertTrue(Calendar.class.isAnnotationPresent(Entity.class), "Calendar must have @Entity!");
	}

}
