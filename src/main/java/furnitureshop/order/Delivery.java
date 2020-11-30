package furnitureshop.order;

import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
public class Delivery extends ItemOrder {

	private LocalDate deliveryDate;

	public Delivery(UserAccount userAccount, ContactInformation contactInformation, LocalDate deliveryDate) {
		super(userAccount, contactInformation);

		Assert.notNull(deliveryDate, "DeliveryDate must not be null!");

		this.deliveryDate = deliveryDate;
	}

	protected Delivery() {}

	public LocalDate getDeliveryDate() {
		return deliveryDate;
	}

}
