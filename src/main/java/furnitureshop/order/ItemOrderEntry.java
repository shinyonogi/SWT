package furnitureshop.order;

import com.mysema.commons.lang.Assert;
import org.salespointframework.order.OrderLine;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class ItemOrderEntry {

	@Id @GeneratedValue
	private Long id;
	private OrderStatus status;
	@OneToOne
	private OrderLine orderline;

	public ItemOrderEntry(OrderStatus status, OrderLine orderline) {
		Assert.notNull(status, "OrderStatus must not be null!");
		Assert.notNull(orderline, "OrderLine must not be null!");

		this.status = status;
		this.orderline = orderline;
	}

	@SuppressWarnings({ "unused", "deprecation" })
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
