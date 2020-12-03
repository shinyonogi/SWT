package furnitureshop.lkw;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
	 * Adds an entry to the {@link Calendar}
	 *
	 * @param entry The new {@link CalendarEntry}
	 *
	 * @return {@code true} if no entry was found on that {@link LocalDate}
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
	 * Removes an entry from the {@link Calendar}
	 *
	 * @param date The {@link LocalDate} which should be removed
	 *
	 * @return {@code true} if the {@link Calendar} contains an entry on that {@link LocalDate}
	 */
	public boolean removeEntry(LocalDate date) {
		final Optional<CalendarEntry> entry = getEntry(date);

		return entry.map(entries::remove).orElse(false);
	}

	/**
	 * Checks if an entry exists in the {@link Calendar}
	 *
	 * @param date The {@link LocalDate} which should be checked
	 *
	 * @return {@code true} if the {@link Calendar} contains an entry on that {@link LocalDate}
	 */
	public boolean hasEntry(LocalDate date) {
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
	 */
	public Optional<CalendarEntry> getEntry(LocalDate date) {
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
