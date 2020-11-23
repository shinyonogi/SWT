package furnitureshop.lkw;

import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
public class CharterEntry extends CalendarEntry {

	protected CharterEntry() {}

	public CharterEntry(LocalDate date) {
		super(date);
	}

}
