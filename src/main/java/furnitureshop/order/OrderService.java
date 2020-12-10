package furnitureshop.order;

import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemService;
import furnitureshop.lkw.LKW;
import furnitureshop.lkw.LKWService;
import furnitureshop.lkw.LKWType;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.order.*;
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
	 * @param userAccountManagement
	 * @param businessTime
	 * @param orderManagement
	 * @param itemService
	 * @param lkwService
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

	public boolean changeItemEntryStatus(ItemOrder order, long itemEntryId, OrderStatus newStatus) {
		final boolean success = order.changeStatus(itemEntryId, newStatus);

		if (!success) {
			return false;
		}

		orderManagement.save(order);

		return false;
	}

	public boolean cancelLKW(LKWCharter order) {
		final boolean success = lkwService.cancelOrder(order.getLkw(), order.getRentDate());

		orderManagement.delete(order);

		return success;
	}

	//Must be tested don't know if it works
	public void removeItemFromOrders(Item item) {
		for (ShopOrder order : findAll()) {
			if (!(order instanceof ItemOrder)) {
				continue;
			}

			final ItemOrder itemOrder = (ItemOrder) order;
			final List<ItemOrderEntry> entries = itemOrder.getOrderEntriesByItem(item);

			for (ItemOrderEntry entry : entries) {
				itemOrder.removeEntry(entry.getId());
			}

			final Totalable<OrderLine> lines = order.getOrderLines(item);

			for (OrderLine line : lines) {
				itemOrder.remove(line);
			}

			if (itemOrder.getOrderEntries().isEmpty()) {
				orderManagement.delete(order);
				continue;
			}

			if (!entries.isEmpty()) {
				orderManagement.save(order);
			}
		}
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

	public Optional<Item> findItemById(ProductIdentifier productId) {
		return itemService.findById(productId);
	}

	public Optional<UserAccount> getDummyUser() {
		return userAccountManagement.findByUsername("Dummy");
	}

	public Optional<LKW> findLKWById(ProductIdentifier productId) {
		return lkwService.findById(productId);
	}

}
