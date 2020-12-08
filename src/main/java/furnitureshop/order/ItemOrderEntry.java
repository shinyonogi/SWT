package furnitureshop.order;

import furnitureshop.inventory.Item;
import org.salespointframework.catalog.ProductIdentifier;
import org.springframework.util.Assert;

import javax.persistence.*;

@Entity
public class ItemOrderEntry {

	@Id @GeneratedValue
	private long id;

	@ManyToOne
	private Item item;

	@Enumerated(EnumType.ORDINAL)
	private OrderStatus status;

	@Deprecated
	protected ItemOrderEntry() {}

	public ItemOrderEntry(Item item, OrderStatus status) {
		Assert.notNull(item, "Item must not be null!");
		Assert.notNull(status, "OrderStatus must not be null!");

		this.item = item;
		this.status = status;
	}

	public Item getItem() {
		return item;
	}

	public ProductIdentifier getId() {
		return getItem().getId();
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		Assert.notNull(status, "OrderStatus must not be null!");

		this.status = status;
	}

}
