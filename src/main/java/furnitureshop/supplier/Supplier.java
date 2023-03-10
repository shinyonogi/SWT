package furnitureshop.supplier;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.Objects;

/**
 * This class represents a supplier
 */
@Entity
public class Supplier {

	@Id @GeneratedValue
	private long id;

	private String name;
	private double surcharge;

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	protected Supplier() {}

	/**
	 * Creates a new instance of a {@link Supplier}
	 *
	 * @param name      Name of the Supplier
	 * @param surcharge Addional part of the price of the {@link furnitureshop.inventory.Item Item}s
	 *
	 * @throws IllegalArgumentException if the {@code name} is {@code null} or {@code surcharge} is negative
	 */
	public Supplier(String name, double surcharge) {
		Assert.notNull(name, "Name must not be null!");
		Assert.isTrue(surcharge >= 0, "Surcharge must greater than 0!");

		this.name = name;
		this.surcharge = surcharge;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public double getSurcharge() {
		return surcharge;
	}

	public void setSurcharge(double surcharge) {
		Assert.isTrue(surcharge >= 0, "Surcharge must greater than 0!");

		this.surcharge = surcharge;
	}

	// for website display
	public double getSurchargeInPercent() {
		return surcharge * 100;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Supplier)) {
			return false;
		}

		Supplier supplier = (Supplier) o;
		return id == supplier.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
