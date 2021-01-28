package furnitureshop.order;

import furnitureshop.inventory.Item;
import furnitureshop.inventory.Piece;
import furnitureshop.inventory.Set;
import org.salespointframework.catalog.Product;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.data.util.Pair;
import org.springframework.data.util.Streamable;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.*;
import java.util.Map.Entry;

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
	 *
	 * @throws IllegalArgumentException if {@link Product} isn't an {@link Item}
	 */
	@Override
	@SuppressWarnings("NullableProblems")
	public OrderLine addOrderLine(Product product, Quantity quantity) {
		Assert.isTrue(product instanceof Item, "Product must be an Item");

		final OrderLine orderLine = super.addOrderLine(product, quantity);

		final int amount = quantity.getAmount().intValue();
		for (int i = 0; i < amount; i++) {
			orderWithStatus.add(new ItemOrderEntry((Item) product, OrderStatus.OPEN));
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
	 *
	 * @throws IllegalArgumentException if {@code newStatus} is {@code null}
	 */
	public boolean changeStatus(long entryId, OrderStatus newStatus) {
		Assert.notNull(newStatus, "OrderStatus must not be null!");

		for (ItemOrderEntry orderEntry : orderWithStatus) {
			if (orderEntry.getId() == entryId) {
				final OrderStatus old = orderEntry.getStatus();

				if (newStatus == OrderStatus.CANCELLED && old == OrderStatus.STORED) {
					orderEntry.setCancelFee(true);
				} else if (newStatus != OrderStatus.CANCELLED && old == OrderStatus.CANCELLED) {
					orderEntry.setCancelFee(false);
				}
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
	 *
	 * @throws IllegalArgumentException if {@code newStatus} is {@code null}
	 */
	public void changeAllStatus(OrderStatus newStatus) {
		Assert.notNull(newStatus, "OrderStatus must not be null!");

		for (ItemOrderEntry orderEntry : orderWithStatus) {
			final OrderStatus old = orderEntry.getStatus();

			if (newStatus == OrderStatus.CANCELLED && old == OrderStatus.STORED) {
				orderEntry.setCancelFee(true);
			} else if (old == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
				orderEntry.setCancelFee(false);
			}

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
				price = price.add(entry.getItem().getPrice().multiply(0.2));
			}
		}

		return price;
	}

	/**
	 * Calulates the total cost of all {@link Item}s
	 *
	 * @return The calculated amount
	 */
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

	/**
	 * Creates a {@link List} of all Entries with the specific {@link Item}
	 *
	 * @return The created {@link List}
	 *
	 * @throws IllegalArgumentException if {@code Item} is {@code null}
	 */
	public List<ItemOrderEntry> getOrderEntriesByItem(Item item) {
		Assert.notNull(item, "Item must not be null!");

		return Streamable.of(orderWithStatus).filter(e -> e.getItem().equals(item)).toList();
	}

	/**
	 * Calulates the total amount of profit of this {@link ItemOrder} per {@link Item}.
	 * It only uses {@link ItemOrderEntry}s which are completed.
	 * {@link Set}s will be split up into {@link Piece}s and the profit will be calculated.
	 *
	 * @return A {@link Map} with all profits per {@link Item}
	 */
	public Map<Item, MonetaryAmount> getProfits() {
		HashMap<Item, MonetaryAmount> itemAmountMap = new HashMap<>();

		for (ItemOrderEntry entry : orderWithStatus) {
			if (entry.getStatus() != OrderStatus.COMPLETED) {
				continue;
			}

			if (entry.getItem() instanceof Set) {
				final Set set = (Set) entry.getItem();

				for (Pair<Piece, MonetaryAmount> pair : set.getPiecePrices()) {
					if (itemAmountMap.containsKey(pair.getFirst())) {
						itemAmountMap.put(pair.getFirst(), pair.getSecond().add(itemAmountMap.get(pair.getFirst())));
					} else {
						itemAmountMap.put(pair.getFirst(), pair.getSecond());
					}
				}
			} else if (itemAmountMap.containsKey(entry.getItem())) {
				itemAmountMap.put(entry.getItem(), entry.getItem().getPrice().add(itemAmountMap.get(entry.getItem())));
			} else {
				itemAmountMap.put(entry.getItem(), entry.getItem().getPrice());
			}
		}

		return itemAmountMap;
	}

	/**
	 * Creates the body of an email to notify the customer about the items which are currently in stock
	 * and may if {@link ItemOrder Order} is {@link Pickup} able to be picked up.
	 *
	 * @return The body of email
	 */
	public String createMailContent() {
		final StringBuilder builder = new StringBuilder();

		final Map<Item, Integer> items = new HashMap<>();
		for (ItemOrderEntry entry : orderWithStatus) {
			if (entry.getStatus() != OrderStatus.STORED) {
				continue;
			}

			if (items.containsKey(entry.getItem())) {
				items.put(entry.getItem(), items.get(entry.getItem()) + 1);
			} else {
				items.put(entry.getItem(), 1);
			}
		}

		if (items.isEmpty()) {
			return null;
		}

		// Build page part
		for (Entry<Item, Integer> entry : items.entrySet()) {
			builder.append(" > ").append(entry.getKey().getName()).append(" (").append(entry.getValue()).append("x)\n");
		}

		// Preset
		String message = "Sehr geehrte(r) %t,\n\n" +
				"wir möchten Ihnen mitteilen, dass folgende Artikel ihrer Bestellung (%o) " +
				"bei uns im Hauptlager eingetroffen%f sind:\n\n" +
				"%s\n" +
				"Mit freundlichen Grüßen Ihr\n" +
				"Möbel-Hier Mitarbeiter";

		// Replace message and return
		message = message.replace("%t", getContactInformation().getName())
				.replace("%o", getId().getIdentifier())
				.replace("%s", builder.toString());

		if (this instanceof Pickup) {
			message = message.replace("%f", " und nun abholbereit");
		} else {
			message = message.replace("%f", "");
		}

		return message;
	}

}
