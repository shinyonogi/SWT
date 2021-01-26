package furnitureshop.lkw;

import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

/**
 * The class represents an entry for a calendar.
 * It can be a {@link DeliveryEntry} or {@link CharterEntry}.
 */
@Entity
public abstract class CalendarEntry {

	@Id @GeneratedValue
	private long id;

	// Date of the entry
	private LocalDate date;

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	protected CalendarEntry() {}

	/**
	 * Creates a new instance of a {@link CalendarEntry} for a calendar
	 *
	 * @param date The {@link LocalDate} of the entry
	 *
	 * @throws IllegalArgumentException If the {@code date} is {@code null}
	 */
	public CalendarEntry(LocalDate date) {
		Assert.notNull(date, "Date must not be null!");

		this.date = date;
	}

	public long getId() {
		return id;
	}

	public LocalDate getDate() {
		return date;
	}

}
