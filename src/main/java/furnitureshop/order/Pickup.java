package furnitureshop.order;

import org.salespointframework.useraccount.UserAccount;

import javax.persistence.Entity;

@Entity
public class Pickup extends ItemOrder{
	public Pickup(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount, contactInformation);
	}

	@SuppressWarnings({ "unused", "deprecation" })
	protected Pickup() {}
}
