package furnitureshop.order;

import org.salespointframework.catalog.Product;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;

import javax.persistence.Entity;
import java.util.HashMap;
import java.util.Map;

@Entity
public abstract class ItemOrder extends ShopOrder {
	Map<OrderLine, OrderStatus> orderWithStatus;

	public ItemOrder(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount, contactInformation);
		this.orderWithStatus = new HashMap<>();
	}

	@Override
	public OrderLine addOrderLine(Product product, Quantity quantity) {
		OrderLine orderLine = super.addOrderLine(product, quantity);
		orderWithStatus.put(orderLine, OrderStatus.OPEN);
		return orderLine;
	}

	public boolean changeAllStatus(OrderStatus status) {
		for (OrderLine orderLine : orderWithStatus.keySet()) {
			orderWithStatus.replace(orderLine, status);
		}
		return true;
	}

	public boolean changeStatus(OrderLine orderLine, OrderStatus status) {
		if (orderWithStatus.containsKey(orderLine)) {
			orderWithStatus.replace(orderLine, status);
			return true;
		} else {
			throw new IllegalArgumentException("Orderline ist nicht in der Order enthalten");
		}
	}
}
