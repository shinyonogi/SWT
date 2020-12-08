package furnitureshop.order;

import org.salespointframework.order.Order;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public abstract class ShopOrder extends Order {

	@OneToOne(cascade = CascadeType.ALL)
	private ContactInformation contactInformation;

	@Deprecated
	@SuppressWarnings("DeprecatedIsStillUsed")
	protected ShopOrder() { }

	ShopOrder(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount);

		Assert.notNull(contactInformation, "ContactInformation must not be null!");

		this.contactInformation = contactInformation;
	}

	public ContactInformation getContactInformation() {
		return contactInformation;
	}

}
