package furnitureshop.lkw;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@Entity
public class Calendar {

	@Id @GeneratedValue
	private long id;

	@OneToMany(cascade = CascadeType.ALL)
	private final List<CalendarEntry> entries;

	public Calendar() {
		this.entries = new LinkedList<>();
	}

	/**
	 * Adds an entry to the {@code Calender}
	 *
	 * @param entry The new {@code CalenderEntry}
	 *
	 * @return {@code true} if no entry was found on that {@code Date}
	 */
	public boolean addEntry(CalendarEntry entry) {
		Assert.notNull(entry, "Entry must not be null!");

		if (hasEntry(entry.getDate())) {
			return false;
		}

		entries.add(entry);

		return true;
	}

	/**
	 * Removes an entry from the {@code Calender}
	 *
	 * @param date The date which should be removed
	 *
	 * @return {@code true} if the {@code Calender} contains an entry on that {@code Date}
	 */
	public boolean removeEntry(LocalDate date) {
		final CalendarEntry entry = getEntry(date);

		return entries.remove(entry);
	}

	/**
	 * Checks if an entry exists in the {@code Calender}
	 *
	 * @param date The date which should be checked
	 *
	 * @return {@code true} if the {@code Calender} contains an entry on that {@code Date}
	 */
	public boolean hasEntry(LocalDate date) {
		return getEntry(date) != null;
	}

	/**
	 * Gets an entry from the {@code Calender}
	 *
	 * @param date The date of the {@code Calender}
	 *
	 * @return The entry of the {@code Calender} of that {@code Date} or {@code null} if no entry exists
	 */
	public CalendarEntry getEntry(LocalDate date) {
		for (CalendarEntry entry : entries) {
			if (entry.getDate().equals(date)) {
				return entry;
			}
		}

		return null;
	}

	public long getId() {
		return id;
	}

	public Iterable<CalendarEntry> getEntries() {
		return entries;
	}

}
