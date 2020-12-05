package furnitureshop.lkw;

import org.salespointframework.catalog.Product;
import org.springframework.util.Assert;

import javax.persistence.*;

/**
 * This class represents a physical LKW. It stores a {@link Calendar} with {@link CalendarEntry} to manage its uses.
 */
@Entity
public class LKW extends Product {

	// The type of the LKW -> Stores price, max. weight and more
	@Enumerated(EnumType.ORDINAL)
	private LKWType type;

	// The LKW specific Calendar with its entries
	@OneToOne(cascade = CascadeType.ALL)
	private Calendar calendar;

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	protected LKW() {}

	/**
	 * Creates a new instance of an {@link LKW} with a new {@link Calendar}
	 *
	 * @param type The {@link LKWType} of the LKW
	 *
	 * @throws NullPointerException If the {@code type} is {@code null}
	 */
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