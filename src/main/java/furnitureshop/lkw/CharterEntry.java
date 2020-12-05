package furnitureshop.lkw;

import javax.persistence.Entity;
import java.time.LocalDate;

/**
 * This class is used to mark a LKW as rented by a customer for a day
 */
@Entity
public class CharterEntry extends CalendarEntry {

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	protected CharterEntry() {}

	/**
	 * Creates a new instance of an {@link CharterEntry} for a calendar
	 *
	 * @param date The {@link LocalDate} of the entry
	 *
	 * @throws IllegalArgumentException If the {@code date} is {@code null}
	 */
	public CharterEntry(LocalDate date) {
		super(date);
	}

}
