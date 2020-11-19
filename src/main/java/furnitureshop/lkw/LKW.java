package furnitureshop.lkw;

import org.springframework.util.Assert;

import javax.persistence.*;

@Entity
public class LKW {

	@Id @GeneratedValue
	private long id;

	@Enumerated(EnumType.ORDINAL)
	private LKWType type;

	@OneToOne(cascade = CascadeType.ALL)
	private Calendar calendar;

	protected LKW() {}

	public LKW(LKWType type) {
		Assert.notNull(type, "Type must not be null!");

		this.type = type;
		this.calendar = new Calendar();
	}

	public long getId() {
		return id;
	}

	public LKWType getType() {
		return type;
	}

	public Calendar getCalendar() {
		return calendar;
	}

}
