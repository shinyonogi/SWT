package furnitureshop.order;

import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemService;
import furnitureshop.lkw.LKW;

import furnitureshop.lkw.LKWService;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.order.Cart;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;

import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

	private final UserAccountManagement userAccountManagement;
	private final BusinessTime businessTime;
	private final OrderManagement<ShopOrder> orderManagement;
	private final ItemService itemService;
	private final LKWService lkwService;

	OrderService(UserAccountManagement userAccountManagement, BusinessTime businessTime, OrderManagement<ShopOrder> orderManagement,
				 ItemService itemService, LKWService lkwService) {
		Assert.notNull(userAccountManagement, "UserAccountManagement must not be null");
		Assert.notNull(businessTime, "BusinessTime must not be null");
		Assert.notNull(orderManagement, "OrderManagement must not be null");
		Assert.notNull(itemService, "ItemService must not be null");
		Assert.notNull(lkwService, "LKWService must not be null");

		this.userAccountManagement = userAccountManagement;
		this.businessTime = businessTime;
		this.orderManagement = orderManagement;
		this.itemService = itemService;
		this.lkwService = lkwService;
	}

	public Optional<Pickup> orderPickupItem(Cart cart, ContactInformation contactInformation) {
		final Optional<UserAccount> useraccount = getDummyUser();

		if (useraccount.isEmpty()) { return Optional.empty(); }

		Pickup order = new Pickup(useraccount.get(), contactInformation);
		cart.addItemsTo(order);
		orderManagement.save(order);
		return Optional.of(order);
	}

	public Optional<Delivery> orderDelieveryItem(Cart cart, ContactInformation contactInformation) {
		LocalDate deliveryDate = LocalDate.now().plusDays(2);
		//@ToDo  get real deliveryDate from LKWManager
		//@Todo  calculate total weight and get the resulting LKW
		//@Todo  split order or mass delivery
		final Optional<UserAccount> userAccount = getDummyUser();

		if (userAccount.isEmpty()) { return Optional.empty(); }

		Delivery order = new Delivery(userAccount.get(), deliveryDate, contactInformation);
		order.changeAllStatus(OrderStatus.PAID);
		cart.addItemsTo(order);
		orderManagement.save(order);
		return Optional.of(order);
	}

	public Optional<LKWCharter> orderLKW(LocalDate rentDate, LKW lkw, ContactInformation contactInformation) {
		final Optional<UserAccount> userAccount = getDummyUser();

		if (userAccount.isEmpty()) { return Optional.empty(); }

		LKWCharter order = new LKWCharter(userAccount.get(), rentDate, contactInformation, lkw);
		orderManagement.save(order);
		return Optional.of(order);
	}

	public boolean cancel() {
		return true;
	}

	public Optional<ShopOrder> findById(String id) {
		final Optional<UserAccount> userAccount = getDummyUser();

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
		final Optional<UserAccount> userAccount = getDummyUser();

		if (userAccount.isEmpty()) {
			return Streamable.empty();
		}

		return orderManagement.findBy(userAccount.get());
	}

	public Optional<UserAccount> getDummyUser() {
		return userAccountManagement.findByUsername("Dummy");
	}

	public Optional<Item> findItemById(ProductIdentifier productId) {
		return itemService.findById(productId);
	}

	public Optional<LKW> findLKWById(ProductIdentifier productId) {
		return lkwService.findById(productId);
	}

}
