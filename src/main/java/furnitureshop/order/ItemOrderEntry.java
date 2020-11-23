package furnitureshop.order;

import org.salespointframework.order.OrderLine;
import org.springframework.util.Assert;

import javax.persistence.*;

@Entity
public class ItemOrderEntry {

	@Id @GeneratedValue
	private long id;

	@Enumerated(EnumType.ORDINAL)
	private OrderStatus status;

	@OneToOne(cascade = CascadeType.ALL)
	private OrderLine orderline;

	public ItemOrderEntry(OrderLine orderline, OrderStatus status) {
		Assert.notNull(orderline, "OrderLine must not be null!");
		Assert.notNull(status, "OrderStatus must not be null!");

		this.orderline = orderline;
		this.status = status;
	}

	protected ItemOrderEntry() { }

	public OrderLine getOrderline() {
		return orderline;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

}
