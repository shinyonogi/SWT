package furnitureshop.lkw;

import org.springframework.util.Assert;

import javax.persistence.Entity;
import java.time.LocalDate;

/**
 * This class is used to mark a LKW as used for delivering {@link furnitureshop.inventory.Item Items} to customers
 */
@Entity
public class DeliveryEntry extends CalendarEntry {

	// The maximium amount of deliveries per day
	public static final int MAX_DELIVERY = 4;

	// The current amount of deliveries
	private int quantity;

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	protected DeliveryEntry() {}

	/**
	 * Creates a new instance of an {@link DeliveryEntry} for a calendar
	 *
	 * @param date The {@link LocalDate} of the entry
	 *
	 * @throws IllegalArgumentException If the {@code date} is {@code null}
	 */
	public DeliveryEntry(LocalDate date) {
		super(date);

		this.quantity = 0;
	}

	public int getQuantity() {
		return quantity;
	}

	/**
	 * Sets the new quantity of deliveries for a {@link LKW}
	 *
	 * @param quantity The new amount
	 *
	 * @throws IllegalArgumentException If the {@code quantity} is less than 0 or greater than {@link DeliveryEntry#MAX_DELIVERY}
	 */
	public void setQuantity(int quantity) {
		Assert.isTrue(quantity >= 0, "Quantity must be greater or equal than 0!");
		Assert.isTrue(quantity <= MAX_DELIVERY, "Quantity must be less or equal than " + MAX_DELIVERY + "!");

		this.quantity = quantity;
	}

}
