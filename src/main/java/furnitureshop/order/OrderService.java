package furnitureshop.order;

import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemService;
import furnitureshop.lkw.LKW;
import furnitureshop.lkw.LKWService;
import furnitureshop.lkw.LKWType;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * This class manages all methods of order
 */

@Service
@Transactional
public class OrderService {

	private final UserAccountManagement userAccountManagement;
	private final BusinessTime businessTime;
	private final OrderManagement<ShopOrder> orderManagement;
	private final ItemService itemService;
	private final LKWService lkwService;

	/**
	 * Creates a new instanfe of {@link OrderService}
	 *
	 * @param userAccountManagement The {@link UserAccountManagement} to access the dummy user
	 * @param businessTime The {@link BusinessTime} to get the current time
	 * @param orderManagement The {@link UserAccountManagement} to manage user accounts
	 * @param itemService The {@link ItemService} to access all functions regarding {@link Item}
	 * @param lkwService The {@link LKWService} to access all functions regarding {@link LKW}
	 */
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

	/**
	 * This function is used to save orders with pick up service
	 *
	 * @param cart with {@link Item} that user want to order
	 * @param contactInformation of the user
	 * @return The order with its information ( {@link Item} and {@link ContactInformation} )
	 */

	public Optional<Pickup> orderPickupItem(Cart cart, ContactInformation contactInformation) {
		final Optional<UserAccount> useraccount = getDummyUser();

		if (useraccount.isEmpty()) {
			return Optional.empty();
		}

		final Pickup order = new Pickup(useraccount.get(), contactInformation);
		cart.addItemsTo(order);
		orderManagement.save(order);

		return Optional.of(order);
	}

	/**
	 * This function is used to save orders with delivery service
	 *
	 * It also calculates the sum of the weight of {@link Item} in the {@link Cart}
	 * and suggests the right {@link LKWType} which has a limited availability and affects the delivery date
	 *
	 * @param cart with {@link Item} that user want to order
	 * @param contactInformation of the user
	 * @return The Order with its information ( {@link Item}, {@link ContactInformation}, {@link LKWType}, {@link LocalDate} )
	 */

	public Optional<Delivery> orderDelieveryItem(Cart cart, ContactInformation contactInformation) {
		final Optional<UserAccount> userAccount = getDummyUser();

		if (userAccount.isEmpty()) {
			return Optional.empty();
		}

		int weight = 0;
		for (CartItem cartItem : cart) {
			if (!(cartItem.getProduct() instanceof Item)) {
				continue;
			}

			final Item item = (Item) cartItem.getProduct();
			weight += item.getWeight() * cartItem.getQuantity().getAmount().intValue();
		}

		final LKWType type = LKWType.getByWeight(weight).orElse(LKWType.LARGE);

		LocalDate deliveryDate = businessTime.getTime().toLocalDate().plusDays(2);
		deliveryDate = lkwService.findNextAvailableDeliveryDate(deliveryDate, type);

		final Optional<LKW> lkw = lkwService.createDeliveryLKW(deliveryDate, type);
		if (lkw.isEmpty()) {
			return Optional.empty();
		}

		final Delivery order = new Delivery(userAccount.get(), contactInformation, lkw.get(), deliveryDate);
		cart.addItemsTo(order);
		order.changeAllStatus(OrderStatus.PAID);
		orderManagement.save(order);

		return Optional.of(order);
	}

	/**
	 * This function is used to save LKW orders
	 *
	 * @param lkw The {@link LKW} that is going to be rated
	 * @param rentDate The {@link LocalDate} the LKW is going tobe rented
	 * @param contactInformation of the user
	 * @return {@link LKWCharter} with its information
	 */

	public Optional<LKWCharter> orderLKW(LKW lkw, LocalDate rentDate, ContactInformation contactInformation) {
		final Optional<UserAccount> userAccount = getDummyUser();

		if (userAccount.isEmpty()) {
			return Optional.empty();
		}

		final LKWCharter order = new LKWCharter(userAccount.get(), contactInformation, lkw, rentDate);
		order.addOrderLine(lkw, Quantity.of(1));
		orderManagement.save(order);

		return Optional.of(order);
	}

	/**
	 * This function is used to change the {@link OrderStatus}
	 *
	 * @param order which its status needs to be changed
	 * @param itemEntryId
	 * @param newStatus new {@link OrderStatus}
	 * @return the boolean false
	 */

	public boolean changeItemEntryStatus(ItemOrder order, long itemEntryId, OrderStatus newStatus) {
		final boolean success = order.changeStatus(itemEntryId, newStatus);

		if (!success) {
			return false;
		}

		orderManagement.save(order);

		return false;
	}

	/**
	 * This function is used to cancel a LKW order
	 *
	 * @param order which needs to be cancelled
	 * @return boolean whether the order cancellation is successful or not
	 */

	public boolean cancelLKW(LKWCharter order) {
		final boolean success = lkwService.cancelOrder(order.getLkw(), order.getRentDate());

		orderManagement.delete(order);

		return success;
	}

	/**
	 * This function is used to remove certain items from orders
	 *
	 * @param item The {@link Item} which needs to be removed from order
	 */

	public void removeItemFromOrders(Item item) {
		for (ItemOrder order : findAllItemOrders()) {
			final List<ItemOrderEntry> entries = order.getOrderEntriesByItem(item);
			if (entries.isEmpty()) {
				continue;
			}

			for (ItemOrderEntry entry : entries) {
				order.removeEntry(entry.getId());
			}

			for (OrderLine line : order.getOrderLines(item).toList()) {
				order.remove(line);
			}

			if (order.getOrderEntries().isEmpty()) {
				orderManagement.delete(order);
			} else {
				orderManagement.save(order);
			}
		}
	}

	/**
	 * This function is used to find order and its information by its identifier
	 *
	 * @param id Identifier of the order
	 * @return Order with its information
	 */

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

	/**
	 * This function is used to find every possible orders
	 *
	 * @return every Order
	 */

	public Streamable<ShopOrder> findAll() {
		final Optional<UserAccount> userAccount = getDummyUser();

		if (userAccount.isEmpty()) {
			return Streamable.empty();
		}

		return orderManagement.findBy(userAccount.get());
	}

	/**
	 * This function is used to find every possible {@link Item} order
	 *
	 * @return every item order
	 */

	public Streamable<ItemOrder> findAllItemOrders() {
		final Optional<UserAccount> userAccount = getDummyUser();

		if (userAccount.isEmpty()) {
			return Streamable.empty();
		}

		return orderManagement.findBy(userAccount.get())
				.filter(order -> order instanceof ItemOrder)
				.map(order -> (ItemOrder) order);
	}

	/**
	 * This function is used to find a certain item by its identifier
	 *
	 * @param productId of the {@link Item}
	 * @return item
	 */

	public Optional<Item> findItemById(ProductIdentifier productId) {
		return itemService.findById(productId);
	}

	/**
	 * This function is used to get the dummy {@link UserAccount}
	 *
	 * @return dummy user
	 */

	public Optional<UserAccount> getDummyUser() {
		return userAccountManagement.findByUsername("Dummy");
	}

	/**
	 * This function is used to find a certain {@link LKW} by its identifier
	 *
	 * @param productId of the {@link LKW}
	 * @return lkw
	 */

	public Optional<LKW> findLKWById(ProductIdentifier productId) {
		return lkwService.findById(productId);
	}

}
