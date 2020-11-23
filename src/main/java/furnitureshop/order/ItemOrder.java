package furnitureshop.order;

import org.salespointframework.catalog.Product;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public abstract class ItemOrder extends ShopOrder {

	@OneToMany
	private List<ItemOrderEntry> orderWithStatus;

	public ItemOrder(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount, contactInformation);
		this.orderWithStatus = new ArrayList<>();
	}

	protected ItemOrder() {}

	@Override
	public OrderLine addOrderLine(Product product, Quantity quantity) {
		OrderLine orderLine = super.addOrderLine(product, quantity);
		orderWithStatus.add(new ItemOrderEntry(OrderStatus.OPEN, orderLine));
		return orderLine;
	}

	public boolean changeAllStatus(OrderStatus status) {
		for (ItemOrderEntry entry : orderWithStatus) {
			entry.setStatus(status);
		}
		return true;
	}

	/*
	public boolean changeStatus(OrderLine orderLine, OrderStatus status) {
		if (orderWithStatus.containsKey(orderLine)) {
			orderWithStatus.replace(orderLine, status);
			return true;
		} else {
			throw new IllegalArgumentException("Orderline ist nicht in der Order enthalten");
		}
	}*/
}
