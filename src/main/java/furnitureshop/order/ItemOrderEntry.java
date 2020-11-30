package furnitureshop.order;

import furnitureshop.inventory.Item;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.OrderLine;
import org.springframework.util.Assert;

import javax.persistence.*;

@Entity
public class ItemOrderEntry {

	@Id @GeneratedValue
	private long id;

	@OneToOne(cascade = CascadeType.ALL)
	private Item item;

	@Enumerated(EnumType.ORDINAL)
	private OrderStatus status;

	public ItemOrderEntry(Item item, OrderStatus status) {
		Assert.notNull(item, "Item must not be null!");
		Assert.notNull(status, "OrderStatus must not be null!");

		this.item = item;
		this.status = status;
	}

	protected ItemOrderEntry() { }

	public Item getProduct() {
		return item;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

}
