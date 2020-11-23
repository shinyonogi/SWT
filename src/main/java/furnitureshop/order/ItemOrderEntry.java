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

	@OneToOne
	private OrderLine orderline;

	public ItemOrderEntry(OrderStatus status, OrderLine orderline) {
		Assert.notNull(status, "OrderStatus must not be null!");
		Assert.notNull(orderline, "OrderLine must not be null!");

		this.status = status;
		this.orderline = orderline;
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
