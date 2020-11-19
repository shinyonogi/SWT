package furnitureshop.lkw;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@Entity
public class Calendar {

	@Id @GeneratedValue
	private int id;

	@OneToMany(cascade = CascadeType.ALL)
	private final List<CalendarEntry> entries;

	public Calendar() {
		this.entries = new LinkedList<>();
	}

	public boolean addEntry(CalendarEntry entry) {
		Assert.notNull(entry, "Entry must not be null!");

		if (hasEntry(entry.getDate())) {
			return false;
		}

		entries.add(entry);

		return true;
	}

	public boolean removeEntry(LocalDate date) {
		final CalendarEntry entry = getEntry(date);

		return entries.remove(entry);
	}

	public boolean hasEntry(LocalDate date) {
		return getEntry(date) != null;
	}

	public CalendarEntry getEntry(LocalDate date) {
		for (CalendarEntry entry : entries) {
			if (entry.getDate().equals(date)) {
				return entry;
			}
		}

		return null;
	}

	public int getId() {
		return id;
	}

	public Iterable<CalendarEntry> getEntries() {
		return entries;
	}

}
