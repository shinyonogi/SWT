package furnitureshop.order;

import furnitureshop.lkw.LKW;
import org.salespointframework.order.Cart;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
public class OrderManager {

	private final UserAccountManagement userAccountManagement;
	private final BusinessTime businessTime;
	private final OrderManagement<ShopOrder> orderManagement;

	OrderManager(UserAccountManagement userAccountManagement, BusinessTime businessTime, OrderManagement<ShopOrder> orderManagement) {
		this.userAccountManagement = userAccountManagement;
		this.businessTime = businessTime;
		this.orderManagement = orderManagement;
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

	public Optional<Order> search(String id) {
		final Optional<UserAccount> useraccount = userAccountManagement.findByUsername("Dummy");

		if (useraccount.isEmpty()) {
			return Optional.empty();
		}

		for (Order order : orderManagement.findBy(useraccount.get())) {
			if (order.getId().getIdentifier().equals(id)) {
				return Optional.of(order);
			}
		}

		return Optional.empty();
	}

}
