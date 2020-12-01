package furnitureshop.lkw;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Optional;

public class CalendarTests {

	Calendar calendar = new Calendar();
	LocalDate friday = LocalDate.of(2021, 4, 2);
	LocalDate wednesday = LocalDate.of(2021, 4, 7);
	LocalDate thursday = LocalDate.of(2021, 4, 8);


	@Test // U-05 U-06
	public void addEntryIfEntryAlreadyExists() {
		assertTrue(calendar.addEntry(new DeliveryEntry(friday)));
		assertFalse(calendar.addEntry(new CharterEntry(friday)));

		assertTrue(calendar.addEntry(new DeliveryEntry(wednesday)));
	}

	@Test // U-07
	public void removeEntryIfEntryExists() {
		DeliveryEntry deliveryFriday = new DeliveryEntry(friday);
		calendar.addEntry(deliveryFriday);

		assertTrue(calendar.getEntry(friday).isPresent());
		assertTrue(calendar.removeEntry(friday));
		assertFalse(calendar.getEntry(friday).isPresent());
	}

	@Test // U-08
	public void removeEntryIfCalendarEmpty() {
		assertFalse(calendar.getEntries().iterator().hasNext());
		assertFalse(calendar.removeEntry(wednesday));
	}

	@Test // U-09
	public void hasEntryIfEntryExists() {
		calendar.addEntry(new DeliveryEntry(wednesday));
		assertTrue(calendar.hasEntry(wednesday));
	}

	@Test // U-10
	public void hasEntryIfEntryNotExists() {
		calendar.addEntry(new DeliveryEntry(wednesday));
		assertFalse(calendar.hasEntry(thursday));
	}

	@Test // U-11
	public void getEntryIfEntryNotExists() {
		calendar.addEntry(new DeliveryEntry(friday));
		assertTrue(calendar.getEntry(thursday).isEmpty());
	}

	@Test // U-12
	public void getEntryIfEntryExists() {
		DeliveryEntry entry = new DeliveryEntry(wednesday);
		calendar.addEntry(entry);
		Optional<CalendarEntry> getEntry = calendar.getEntry(wednesday);

		assertTrue(getEntry.isPresent());
		assertEquals(entry, getEntry.get());
	}
}
