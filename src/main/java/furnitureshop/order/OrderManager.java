package furnitureshop.order;

import furnitureshop.lkw.LKW;
import org.salespointframework.order.Cart;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;

import org.springframework.data.util.Streamable;
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

	public boolean orderPickupItem(Cart cart, ContactInformation contactInformation) {
		final Optional<UserAccount> useraccount = userAccountManagement.findByUsername("Dummy");

		if (useraccount.isEmpty()) { return false; }

		Pickup order = new Pickup(useraccount.get(), contactInformation);
		cart.addItemsTo(order);
		orderManagement.save(order);
		return true;
	}

	public boolean orderDelieveryItem(Cart cart, ContactInformation contactInformation) {
		LocalDate deliveryDate = LocalDate.now().plusDays(2);
		//@ToDo  get real deliveryDate from LKWManager
		//@Todo  calculate total weight and get the resulting LKW
		//@Todo  split order or mass delivery
		final Optional<UserAccount> userAccount = userAccountManagement.findByUsername("Dummy");

		if (userAccount.isEmpty()) { return false; }

		Delivery order = new Delivery(userAccount.get(), deliveryDate, contactInformation);
		order.changeAllStatus(OrderStatus.PAID);
		cart.addItemsTo(order);
		orderManagement.save(order);
		return true;
	}

	public boolean orderLKW(LocalDate rentDate, LKW lkw, ContactInformation contactInformation) {
		final Optional<UserAccount> userAccount = userAccountManagement.findByUsername("Dummy");

		if (userAccount.isEmpty()) { return false; }

		LKWCharter order = new LKWCharter(userAccount.get(), rentDate, contactInformation, lkw);
		orderManagement.save(order);
		return true;
	}

	public boolean cancel() {
		return true;
	}

	public Optional<ShopOrder> search(String id) {
		final Optional<UserAccount> userAccount = userAccountManagement.findByUsername("Dummy");

		if (userAccount.isEmpty()) {
			return Optional.empty();
		}

		for (ShopOrder order : orderManagement.findBy(userAccount.get())) {
			if (order.getId().getIdentifier().equals(id)) {
				return Optional.of(order);
			}
		}

		return Optional.empty();
	}

	public Streamable<ShopOrder> findAll() {
		final Optional<UserAccount> userAccount = userAccountManagement.findByUsername("Dummy");

		if (userAccount.isEmpty()) {
			return Streamable.empty();
		}

		return orderManagement.findBy(userAccount.get());
	}

}
