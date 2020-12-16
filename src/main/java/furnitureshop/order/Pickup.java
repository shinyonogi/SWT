package furnitureshop.order;

import org.salespointframework.useraccount.UserAccount;

import javax.persistence.Entity;

/**
 * This class represents the delivery type {@link Pickup}
 */
@Entity
public class Pickup extends ItemOrder {

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	protected Pickup() {}

	/**
	 * Creates a new instance of {@link Pickup}
	 *
	 * @param userAccount        The dummy {@link UserAccount}
	 * @param contactInformation The {@link ContactInformation} of the customer
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public Pickup(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount, contactInformation);
	}

}
