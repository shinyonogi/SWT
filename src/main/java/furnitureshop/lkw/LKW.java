package furnitureshop.lkw;

import org.salespointframework.catalog.Product;
import org.salespointframework.core.Currencies;
import org.springframework.util.Assert;

import javax.persistence.*;

@Entity
public class LKW extends Product {

	@Enumerated(EnumType.ORDINAL)
	private final LKWType type;

	@OneToOne(cascade = CascadeType.ALL)
	private final Calendar calendar;

	public LKW(LKWType type) {
		super("lkw", Currencies.ZERO_EURO);

		Assert.notNull(type, "Type must not be null!");

		this.type = type;
		this.calendar = new Calendar();
	}

	public LKWType getType() {
		return type;
	}

	public Calendar getCalendar() {
		return calendar;
	}

}
