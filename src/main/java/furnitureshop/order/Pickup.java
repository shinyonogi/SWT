package furnitureshop.order;

import org.salespointframework.useraccount.UserAccount;

import javax.persistence.Entity;

/**
 * This class represents the delivery type {@link Pickup}
 */

@Entity
public class Pickup extends ItemOrder {

	@Deprecated
	protected Pickup() {}

	/**
	 * Creates a new instance of {@link Pickup}
	 *
	 * @param userAccount
	 * @param contactInformation {@link ContactInformation} of the user
	 */

	public Pickup(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount, contactInformation);
	}

}
