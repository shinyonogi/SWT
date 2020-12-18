package furnitureshop.order;

import furnitureshop.inventory.Item;
import org.salespointframework.catalog.Product;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.data.util.Streamable;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public abstract class ItemOrder extends ShopOrder {

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<ItemOrderEntry> orderWithStatus;

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	@SuppressWarnings("DeprecatedIsStillUsed")
	protected ItemOrder() {}

	/**
	 * Creates a new instance of {@link ItemOrder}
	 *
	 * @param userAccount        The dummy {@link UserAccount}
	 * @param contactInformation The {@link ContactInformation} of the customer
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public ItemOrder(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount, contactInformation);

		this.orderWithStatus = new ArrayList<>();
	}

	/**
	 * This function is used to create an order line with order entries.
	 *
	 * @param product  {@link Item} that is going to be added to order line
	 * @param quantity The quantity of the product
	 *
	 * @return the created order line
	 */
	@Override
	@SuppressWarnings("NullableProblems")
	public OrderLine addOrderLine(Product product, Quantity quantity) {
		final OrderLine orderLine = super.addOrderLine(product, quantity);

		if (product instanceof Item) {
			final int amount = quantity.getAmount().intValue();
			for (int i = 0; i < amount; i++) {
				orderWithStatus.add(new ItemOrderEntry((Item) product, OrderStatus.OPEN));
			}
		}

		return orderLine;
	}

	/**
	 * This function is used to remove a certain order entry with status
	 *
	 * @param entryId The Identifier of the entry that needs to be removed
	 *
	 * @return boolean true if the removal is successful, boolean false if unsuccessful
	 */
	public boolean removeEntry(long entryId) {
		for (ItemOrderEntry entry : orderWithStatus) {
			if (entry.getId() == entryId) {
				orderWithStatus.remove(entry);
				return true;
			}
		}
		return false;
	}

	/**
	 * This function is used to change the order status of certain order entry.
	 *
	 * @param entryId   The Identifier of the entry that needs to be changed
	 * @param newStatus The new {@link OrderStatus} of the {@link ItemOrderEntry}
	 *
	 * @return boolean true if the change of status is successful, boolean false if unsuccessful
	 */
	public boolean changeStatus(long entryId, OrderStatus newStatus) {
		Assert.notNull(newStatus, "OrderStatus must not be null!");

		for (ItemOrderEntry orderEntry : orderWithStatus) {
			if (orderEntry.getId() == entryId) {
				final OrderStatus old = orderEntry.getStatus();

				orderEntry.setCancelFee(newStatus == OrderStatus.CANCELLED && old == OrderStatus.STORED);
				orderEntry.setStatus(newStatus);

				return true;
			}
		}

		return false;
	}

	/**
	 * This function is used to change the order status of all order entries.
	 *
	 * @param newStatus The new {@link OrderStatus}
	 */
	public void changeAllStatus(OrderStatus newStatus) {
		Assert.notNull(newStatus, "OrderStatus must not be null!");

		for (ItemOrderEntry orderEntry : orderWithStatus) {
			final OrderStatus old = orderEntry.getStatus();

			orderEntry.setCancelFee(newStatus == OrderStatus.CANCELLED && old == OrderStatus.STORED);
			orderEntry.setStatus(newStatus);
		}
	}

	@Override
	public MonetaryAmount getRefund() {
		MonetaryAmount amount = Currencies.ZERO_EURO;

		for (ItemOrderEntry entry : orderWithStatus) {
			if (entry.getStatus() == OrderStatus.CANCELLED) {
				amount = amount.add(entry.getItem().getPrice());
			}
		}

		return amount;
	}

	@Override
	public MonetaryAmount getMissingPayment() {
		MonetaryAmount amount = Currencies.ZERO_EURO;

		for (ItemOrderEntry entry : orderWithStatus) {
			if (entry.getStatus() == OrderStatus.OPEN) {
				amount = amount.add(entry.getItem().getPrice());
			} else if (this instanceof Pickup && entry.getStatus() == OrderStatus.STORED) {
				amount = amount.add(entry.getItem().getPrice());
			}
		}

		return amount;
	}

	@Override
	public MonetaryAmount getCancelFee() {
		MonetaryAmount price = Currencies.ZERO_EURO;

		for (ItemOrderEntry entry : orderWithStatus) {
			if (entry.getStatus() == OrderStatus.CANCELLED && entry.hasCancelFee()) {
				price = price.add(entry.getItem().getPrice()).multiply(0.2);
			}
		}

		return price;
	}

	public MonetaryAmount getItemTotal() {
		return super.getTotal();
	}

	@Override
	@SuppressWarnings("NullableProblems")
	public MonetaryAmount getTotal() {
		return getItemTotal().add(getCancelFee()).subtract(getRefund());
	}

	public List<ItemOrderEntry> getOrderEntries() {
		return Collections.unmodifiableList(orderWithStatus);
	}

	public List<ItemOrderEntry> getOrderEntriesByItem(Item item) {
		return Streamable.of(orderWithStatus).filter(e -> e.getItem().equals(item)).toList();
	}

}
