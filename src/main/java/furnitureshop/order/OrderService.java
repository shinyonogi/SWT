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
	 * @param businessTime          The {@link BusinessTime} to get the current time
	 * @param orderManagement       The {@link UserAccountManagement} to manage user accounts
	 * @param itemService           The {@link ItemService} to access all functions regarding {@link Item}
	 * @param lkwService            The {@link LKWService} to access all functions regarding {@link LKW}
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
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
	 * This function is used to save orders with pick up service.
	 *
	 * @param cart               The {@link Cart} of the customer with {@link Item}
	 * @param contactInformation The {@link ContactInformation} of the user
	 *
	 * @return The order with its information ( {@link Item} and {@link ContactInformation} )
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public Optional<Pickup> orderPickupItem(Cart cart, ContactInformation contactInformation) {
		Assert.notNull(cart, "Cart must not be null");
		Assert.notNull(contactInformation, "ContactInformation must not be null");

		final Optional<UserAccount> useraccount = getDummyUser();

		if (useraccount.isEmpty()) {
			return Optional.empty();
		}

		final Pickup order = new Pickup(useraccount.get(), contactInformation);
		order.setCreated(businessTime.getTime());
		order.setUpdated(businessTime.getTime());

		cart.addItemsTo(order);
		orderManagement.save(order);

		return Optional.of(order);
	}

	/**
	 * This function is used to save orders with delivery service.
	 * It also calculates the sum of the weight of {@link Item} in the {@link Cart}
	 * and suggests the right {@link LKWType} which has a limited availability and affects the delivery date.
	 *
	 * @param cart               The {@link Cart} of the customer with {@link Item}
	 * @param contactInformation The {@link ContactInformation} of the user
	 *
	 * @return The Order with its information ( {@link Item}, {@link ContactInformation}, {@link LKWType}, {@link LocalDate} )
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public Optional<Delivery> orderDelieveryItem(Cart cart, ContactInformation contactInformation) {
		Assert.notNull(cart, "Cart must not be null");
		Assert.notNull(contactInformation, "ContactInformation must not be null");

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
		order.setCreated(businessTime.getTime());
		order.setUpdated(businessTime.getTime());

		cart.addItemsTo(order);
		order.changeAllStatus(OrderStatus.PAID);
		orderManagement.save(order);

		return Optional.of(order);
	}

	/**
	 * This function is used to save LKW orders.
	 *
	 * @param lkw                The {@link LKW} that is going to be rated
	 * @param rentDate           The {@link LocalDate} the LKW is going tobe rented
	 * @param contactInformation The {@link ContactInformation} of the user
	 *
	 * @return {@link LKWCharter} with its information
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public Optional<LKWCharter> orderLKW(LKW lkw, LocalDate rentDate, ContactInformation contactInformation) {
		Assert.notNull(lkw, "LKW must not be null");
		Assert.notNull(rentDate, "RentDate must not be null");
		Assert.notNull(contactInformation, "ContactInformation must not be null");

		final Optional<UserAccount> userAccount = getDummyUser();

		if (userAccount.isEmpty()) {
			return Optional.empty();
		}

		final LKWCharter order = new LKWCharter(userAccount.get(), contactInformation, lkw, rentDate);
		order.setCreated(businessTime.getTime());
		order.setUpdated(businessTime.getTime());

		order.addOrderLine(lkw, Quantity.of(1));
		orderManagement.save(order);

		return Optional.of(order);
	}

	/**
	 * This function is used to change the {@link OrderStatus} for an {@link Item}
	 *
	 * @param order       The {@link ItemOrder} which should be updated
	 * @param itemEntryId The id of the {@link ItemOrderEntry} where {@link Item} and {@link OrderStatus} are saved
	 * @param newStatus   The new {@link OrderStatus}
	 *
	 * @return true if successful
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public boolean changeItemEntryStatus(ItemOrder order, long itemEntryId, OrderStatus newStatus) {
		Assert.notNull(order, "ItemOrder must not be null");
		Assert.notNull(newStatus, "OrderStatus must not be null");

		final boolean success = order.changeStatus(itemEntryId, newStatus);

		if (!success) {
			return false;
		}

		order.setUpdated(businessTime.getTime());
		orderManagement.save(order);

		return true;
	}

	/**
	 * This function is used to cancel a LKW order
	 *
	 * @param order The {@link LKWCharter} which needs to be cancelled
	 *
	 * @return boolean whether the order cancellation is successful or not
	 *
	 * @throws IllegalArgumentException if {@code order} is {@code null}
	 */
	public boolean cancelLKW(LKWCharter order) {
		Assert.notNull(order, "LKWCharter must not be null");

		final boolean success = lkwService.cancelOrder(order.getLkw(), order.getRentDate());

		orderManagement.delete(order);

		return success;
	}

	/**
	 * This function is used to remove certain items from {@link ItemOrder}s
	 *
	 * @param item The {@link Item} which needs to be removed from {@link ItemOrder}s
	 *
	 * @throws IllegalArgumentException if {code item} is {@code null}
	 */
	public void removeItemFromOrders(Item item) {
		Assert.notNull(item, "Item must not be null");

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
				order.setUpdated(businessTime.getTime());
				orderManagement.save(order);
			}
		}
	}

	public OrderStatus getStatus(ShopOrder order) {
		Assert.notNull(order, "Order must not be null");

		OrderStatus status = OrderStatus.OPEN;

		if (order instanceof LKWCharter) {
			if (((LKWCharter) order).getRentDate().isAfter(businessTime.getTime().toLocalDate())) {
				status = OrderStatus.PAID;
			} else {
				status = OrderStatus.COMPLETED;
			}
		} else if (order instanceof ItemOrder) {
			final List<ItemOrderEntry> entries = ((ItemOrder) order).getOrderEntries();

			if (entries.isEmpty()) {
				return status;
			}

			status = OrderStatus.CANCELLED;

			for (ItemOrderEntry entry : entries) {
				// Normal Status order
				if (entry.getStatus().ordinal() < status.ordinal()) {
					status = entry.getStatus();
				}
			}
		}

		return status;
	}

	/**
	 * This function is used to find {@link ShopOrder} and its information by its identifier
	 *
	 * @param id The identifier of the {@link ShopOrder}
	 *
	 * @return The matching {@link ShopOrder} with its information
	 *
	 * @throws IllegalArgumentException if {code id} is {@code null}
	 */
	public Optional<ShopOrder> findById(String id) {
		Assert.hasText(id, "Id must not be null");

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
	 * This function is used to find every possible {@link ShopOrder}
	 *
	 * @return Every existing {@link ShopOrder}
	 */
	public Streamable<ShopOrder> findAll() {
		final Optional<UserAccount> userAccount = getDummyUser();

		if (userAccount.isEmpty()) {
			return Streamable.empty();
		}

		return orderManagement.findBy(userAccount.get());
	}

	/**
	 * This function is used to find every possible {@link ItemOrder}
	 *
	 * @return Every existing {@link ItemOrder}
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
	 * This function is used to find a certain {@link Item} by its identifier
	 *
	 * @param productId The id of the {@link Item}
	 *
	 * @return The matching {@link Item}
	 */
	public Optional<Item> findItemById(ProductIdentifier productId) {
		return itemService.findById(productId);
	}

	/**
	 * This function is used to get the dummy {@link UserAccount}
	 *
	 * @return The dummy {@link UserAccount}
	 */
	public Optional<UserAccount> getDummyUser() {
		return userAccountManagement.findByUsername("Dummy");
	}

	/**
	 * This function is used to find a certain {@link LKW} by its identifier
	 *
	 * @param productId of the {@link LKW}
	 *
	 * @return The matching {@link LKW}
	 */
	public Optional<LKW> findLKWById(ProductIdentifier productId) {
		return lkwService.findById(productId);
	}

}
