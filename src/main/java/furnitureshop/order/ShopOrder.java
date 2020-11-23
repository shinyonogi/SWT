package furnitureshop.order;

import org.salespointframework.order.Order;
import org.salespointframework.useraccount.UserAccount;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class ShopOrder extends Order {
	@OneToOne
	private ContactInformation contactInformation;

	ShopOrder(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount);
		this.contactInformation = contactInformation;
	}

	@SuppressWarnings({ "unused", "deprecation" })
	protected ShopOrder() { }
}
