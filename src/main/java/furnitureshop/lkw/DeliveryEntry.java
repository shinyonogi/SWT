package furnitureshop.lkw;

import org.springframework.util.Assert;

import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
public class DeliveryEntry extends CalendarEntry {

	public static final int MAX_DELIVERY = 4;

	private int quantity;

	protected DeliveryEntry() {}

	public DeliveryEntry(LocalDate date) {
		super(date);

		this.quantity = 0;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		Assert.isTrue(quantity > 0, "Quantity must be greater than 0!");

		this.quantity = quantity;
	}

}
