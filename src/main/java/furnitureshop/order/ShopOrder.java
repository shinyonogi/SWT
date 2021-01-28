package furnitureshop.order;

import org.salespointframework.order.Order;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.time.LocalDateTime;

@Entity
public abstract class ShopOrder extends Order {

	@OneToOne(cascade = CascadeType.ALL)
	private ContactInformation contactInformation;

	private LocalDateTime created;
	private LocalDateTime updated;

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	@SuppressWarnings("DeprecatedIsStillUsed")
	protected ShopOrder() {}

	/**
	 * Creates new instance of {@link ShopOrder}
	 *
	 * @param userAccount        The dummy {@link UserAccount}
	 * @param contactInformation The {@link ContactInformation} of the customer
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	ShopOrder(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount);

		Assert.notNull(contactInformation, "ContactInformation must not be null!");

		this.contactInformation = contactInformation;
	}

	public ContactInformation getContactInformation() {
		return contactInformation;
	}

	/**
	 * Calculates the amount of money the customer still have to pay
	 *
	 * @return The calculated amount
	 */
	public abstract MonetaryAmount getMissingPayment();

	/**
	 * Calculates the amount of money the customer will get for canceling {@link furnitureshop.inventory.Item Items}
	 *
	 * @return The calculated amount
	 */
	public abstract MonetaryAmount getRefund();

	/**
	 * Calculates the amount of money the customer have to pay for canceling stored {@link furnitureshop.inventory.Item Items}
	 *
	 * @return The calculated amount
	 */
	public abstract MonetaryAmount getCancelFee();

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		Assert.notNull(created, "Date must not be null!");

		this.created = created;
	}

	public LocalDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(LocalDateTime updated) {
		Assert.notNull(updated, "Date must not be null!");

		this.updated = updated;
	}

}
