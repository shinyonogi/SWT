package furnitureshop.lkw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Optional;

public class CalendarTests {

	Calendar calendar;
	LocalDate friday, wednesday, thursday;

	@BeforeEach
	void setUp() {
		calendar = new Calendar();
		friday = LocalDate.of(2021, 4, 2);
		wednesday = LocalDate.of(2021, 4, 7);
		thursday = LocalDate.of(2021, 4, 8);
	}

	@Test // U-05 U-06
	public void addEntryIfEntryAlreadyExists() {
		assertTrue(calendar.addEntry(new DeliveryEntry(friday)), "addEntry() should be able to add the entry to the Calendar when the date is available!");
		assertFalse(calendar.addEntry(new CharterEntry(friday)), "addEntry() should not be abe to add the entry to the Calendar when the date is unavailable!");

		assertTrue(calendar.addEntry(new DeliveryEntry(wednesday)), "addEntry() should be able to add the entry to the Calendar when the date is available!");
	}

	@Test // U-07
	public void removeEntryIfEntryExists() {
		DeliveryEntry deliveryFriday = new DeliveryEntry(friday);
		calendar.addEntry(deliveryFriday);

		assertTrue(calendar.getEntry(friday).isPresent(), "getEntry() should return entry when entry exists in Calendar!");
		assertTrue(calendar.removeEntry(friday), "removeEntry() should return false when entry can be removed from Calendar!");
		assertFalse(calendar.getEntry(friday).isPresent(), "getEntry() should not return the entry if entry was removed from Calendar!");
	}

	@Test // U-08
	public void removeEntryIfCalendarEmpty() {
		assertFalse(calendar.getEntries().iterator().hasNext(), "getEntries() should not return any entries if the Calendar is empty!");
		assertFalse(calendar.removeEntry(wednesday), "removeEntry() should not return true when the Calendar is empty!");
	}

	@Test // U-09
	public void hasEntryIfEntryExists() {
		calendar.addEntry(new DeliveryEntry(wednesday));
		assertTrue(calendar.hasEntry(wednesday), "hasEntry() should not return false if entry was added to Calendar!");
	}

	@Test // U-10
	public void hasEntryIfEntryNotExists() {
		calendar.addEntry(new DeliveryEntry(wednesday));
		assertFalse(calendar.hasEntry(thursday), "hasEntry() should not return true if entry was never added to Calendar!");
	}

	@Test // U-11
	public void getEntryIfEntryNotExists() {
		calendar.addEntry(new DeliveryEntry(friday));
		assertTrue(calendar.getEntry(thursday).isEmpty(), "getEntry() should not return an entry if the given entry does not exists in the Calendar!");
	}

	@Test // U-12
	public void getEntryIfEntryExists() {
		DeliveryEntry entry = new DeliveryEntry(wednesday);
		calendar.addEntry(entry);
		Optional<CalendarEntry> getEntry = calendar.getEntry(wednesday);

		assertTrue(getEntry.isPresent(), "getEntry() should return entry when entry exists in Calendar!");
		assertEquals(entry, getEntry.get(), "getEntry() should return the correct entry for the given date!");
	}
}
