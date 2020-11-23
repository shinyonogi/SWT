package furnitureshop.order;

import furnitureshop.lkw.LKW;
import org.salespointframework.order.Cart;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;

import java.time.LocalDate;

public class OrderManager {
	private final UserAccountManagement userAccountManagement;
	private final BusinessTime businessTime;
	private final OrderManagement<ShopOrder> orderManagement;
	private final UserAccount useraccount;

	OrderManager(UserAccountManagement userAccountManagement, BusinessTime businessTime, OrderManagement<ShopOrder> orderManagement) {
		this.userAccountManagement = userAccountManagement;
		this.businessTime = businessTime;
		this.orderManagement = orderManagement;
		this.useraccount = userAccountManagement.findByUsername("Dummy").orElseGet(null);
	}

	public boolean orderPickupItem(Cart cart, UserAccount userAccount, ContactInformation contactInformation) {
		Pickup order = new Pickup(userAccount, contactInformation);
		cart.addItemsTo(order);
		return true;
	}

	public boolean orderDelieveryItem(Cart cart, UserAccount userAccount, ContactInformation contactInformation) {
		LocalDate deliveryDate = LocalDate.now().plusDays(2);
		//@ToDo  get real deliveryDate from LKWManager
		//@Todo  calculate total weight and get the resulting LKW
		//@Todo  split order or mass delivery
		Delivery order = new Delivery(userAccount, deliveryDate, contactInformation);
		order.changeAllStatus(OrderStatus.PAID);
		cart.addItemsTo(order);
		return true;
	}

	public boolean orderLKW(UserAccount userAccount, LocalDate rentDate, LKW lkw, ContactInformation contactInformation) {
		LKWCharter order = new LKWCharter(userAccount, rentDate, contactInformation, lkw);
		return true;
	}

	public boolean cancel() {
		return true;
	}

	public Order search(String id) {
		for (Order order : orderManagement.findBy(useraccount)) {
			if (order.getId().getIdentifier().equals(id)) {
				return order;
			}
		}
		return null;
	}
}
