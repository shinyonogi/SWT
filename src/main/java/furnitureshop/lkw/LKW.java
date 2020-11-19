package furnitureshop.lkw;

import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.*;

@Entity
public class LKW {

	@Id @GeneratedValue
	private int id;
	private double weight;

	private MonetaryAmount price;

	@OneToOne(cascade = CascadeType.ALL)
	private Calendar calendar;

	protected LKW() {}

	public LKW(double weight, MonetaryAmount price) {
		Assert.isTrue(weight > 0, "Weight must be greater than 0!");
		Assert.notNull(price, "Price must not be null!");

		this.weight = weight;
		this.price = price;
		this.calendar = new Calendar();
	}

	public int getId() {
		return id;
	}

	public double getWeight() {
		return weight;
	}

	public MonetaryAmount getPrice() {
		return price;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public LKW setPrice(MonetaryAmount price) {
		Assert.notNull(price, "Price must not be null!");

		return this;
	}

}
