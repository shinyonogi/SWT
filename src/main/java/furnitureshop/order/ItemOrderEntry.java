package furnitureshop.order;

import furnitureshop.inventory.Item;
import org.springframework.util.Assert;

import javax.persistence.*;

/**
 * This class is used to mark an order as Open, Stored, Paid, Completed or Canceled.
 */

@Entity
public class ItemOrderEntry {

	@Id @GeneratedValue
	private long id;

	@ManyToOne
	private Item item;

	@Enumerated(EnumType.ORDINAL)
	private OrderStatus status;

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	protected ItemOrderEntry() {}

	/**
	 * Creates a new instance of {@link ItemOrderEntry}
	 *
	 * @param item   A specific {@link Item} that is ordered
	 * @param status {@link OrderStatus} is Open, Stored, Paid, Completed or Cancelled
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public ItemOrderEntry(Item item, OrderStatus status) {
		Assert.notNull(item, "Item must not be null!");
		Assert.notNull(status, "OrderStatus must not be null!");

		this.item = item;
		this.status = status;
	}

	public long getId() {
		return this.id;
	}

	public Item getItem() {
		return item;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		Assert.notNull(status, "OrderStatus must not be null!");

		this.status = status;
	}

}
