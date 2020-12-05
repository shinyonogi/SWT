package furnitureshop.lkw;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * This class represents the {@link Calendar} for an {@link LKW}.
 * It saves {@link CalendarEntry} for each {@link LocalDate}.
 * <p>
 * There are two types of {@link CalendarEntry}:
 * <li>
 *     {@link DeliveryEntry} is used to mark the LKW as used for delivering {@link furnitureshop.inventory.Item Items} to customers
 * </li>
 * <li>
 *     {@link CharterEntry} is used to mark the LKW as rented by a customer for a day
 * </li>
 */
@Entity
public class Calendar {

	@Id @GeneratedValue
	private long id;

	// List of all Entries of the calendar
	@OneToMany(cascade = CascadeType.ALL)
	private final List<CalendarEntry> entries;

	/**
	 * Creates a new instance of a calendar with no entries
	 */
	public Calendar() {
		this.entries = new LinkedList<>();
	}

	/**
	 * Adds an entry to the {@link Calendar}
	 *
	 * @param entry The new {@link CalendarEntry}
	 *
	 * @return {@code true} if no entry was found on that {@link LocalDate}
	 *
	 * @throws IllegalArgumentException If the {@code entry} is {@code null}
	 */
	public boolean addEntry(CalendarEntry entry) {
		Assert.notNull(entry, "Entry must not be null!");

		// Check if calendar already have an entry on that day
		if (hasEntry(entry.getDate())) {
			return false;
		}

		// Add entry to calendar
		entries.add(entry);

		return true;
	}

	/**
	 * Removes an entry from the {@link Calendar}
	 *
	 * @param date The {@link LocalDate} which should be removed
	 *
	 * @return {@code true} if the {@link Calendar} contains an entry on that {@link LocalDate}
	 *
	 * @throws IllegalArgumentException If the {@code date} is {@code null}
	 */
	public boolean removeEntry(LocalDate date) {
		Assert.notNull(date, "Date must not be null!");

		// Get entry on that day
		final Optional<CalendarEntry> entry = getEntry(date);

		// If entry exists remove it -> return if an entry existed
		return entry.map(entries::remove).orElse(false);
	}

	/**
	 * Checks if an entry exists in the {@link Calendar}
	 *
	 * @param date The {@link LocalDate} which should be checked
	 *
	 * @return {@code true} if the {@link Calendar} contains an entry on that {@link LocalDate}
	 *
	 * @throws IllegalArgumentException If the {@code date} is {@code null}
	 */
	public boolean hasEntry(LocalDate date) {
		Assert.notNull(date, "Date must not be null!");

		for (CalendarEntry entry : entries) {
			if (entry.getDate().equals(date)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets an entry from the {@link Calendar}
	 *
	 * @param date The {@link LocalDate} of the {@link Calendar}
	 *
	 * @return The {@link CalendarEntry} of the {@link Calendar} of that {@link LocalDate}
	 *
	 * @throws IllegalArgumentException If the {@code date} is {@code null}
	 */
	public Optional<CalendarEntry> getEntry(LocalDate date) {
		Assert.notNull(date, "Date must not be null!");

		for (CalendarEntry entry : entries) {
			if (entry.getDate().equals(date)) {
				return Optional.of(entry);
			}
		}

		return Optional.empty();
	}

	public long getId() {
		return id;
	}

	public List<CalendarEntry> getEntries() {
		return Collections.unmodifiableList(entries);
	}

}
