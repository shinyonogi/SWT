package furnitureshop.lkw;

import org.salespointframework.catalog.Product;
import org.springframework.util.Assert;

import javax.persistence.*;

@Entity
public class LKW extends Product {

	@Enumerated(EnumType.ORDINAL)
	private LKWType type;

	@OneToOne(cascade = CascadeType.ALL)
	private Calendar calendar;

	@SuppressWarnings("deprecation")
	protected LKW() {}

	public LKW(LKWType type) {
		super("lkw", type.getCharterPrice());

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