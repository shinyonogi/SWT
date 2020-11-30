package furnitureshop.order;

import org.salespointframework.order.Order;
import org.salespointframework.payment.Cash;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public abstract class ShopOrder extends Order {

	@OneToOne(cascade = CascadeType.ALL)
	private ContactInformation contactInformation;

	ShopOrder(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount);

		Assert.notNull(contactInformation, "ContactInformation must not be null!");

		this.contactInformation = contactInformation;
	}

	@SuppressWarnings({ "unused", "deprecation" })
	protected ShopOrder() { }

	public ContactInformation getContactInformation() {
		return contactInformation;
	}
}
