package furnitureshop.order;

import org.salespointframework.useraccount.UserAccount;

import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
public class Delivery extends ItemOrder{
	private LocalDate deliveryDate;

	public Delivery(UserAccount userAccount, LocalDate deliveryDate, ContactInformation contactInformation) {
		super(userAccount, contactInformation);
		this.deliveryDate = deliveryDate;
	}

	protected Delivery() {}
}
