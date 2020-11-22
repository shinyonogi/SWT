package furnitureshop.order;

import org.salespointframework.order.Order;
import org.salespointframework.useraccount.UserAccount;

import javax.persistence.Entity;

@Entity
public class ShopOrder extends Order {
	private final ContactInformation contactInformation;

	ShopOrder(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount);
		this.contactInformation = contactInformation;
	}
}
