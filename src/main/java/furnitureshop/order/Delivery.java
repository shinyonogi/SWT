package furnitureshop.order;

import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDate;

public class Delivery extends ItemOrder{
	private LocalDate deliveryDate;

	public Delivery(UserAccount userAccount, LocalDate deliveryDate, ContactInformation contactInformation) {
		super(userAccount, contactInformation);
		this.deliveryDate = deliveryDate;
	}
}
