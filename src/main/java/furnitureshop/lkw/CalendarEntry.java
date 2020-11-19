package furnitureshop.lkw;

import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public abstract class CalendarEntry {

	@Id @GeneratedValue
	private int id;

	private LocalDate date;

	protected CalendarEntry() {}

	public CalendarEntry(LocalDate date) {
		Assert.notNull(date, "Date must not be null!");

		this.date = date;
	}

	public int getId() {
		return id;
	}

	public LocalDate getDate() {
		return date;
	}

}
