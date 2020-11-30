package furnitureshop.order;

import furnitureshop.inventory.Item;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public abstract class ItemOrder extends ShopOrder {

	@OneToMany(cascade = CascadeType.ALL)
	private List<ItemOrderEntry> orderWithStatus;

	public ItemOrder(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount, contactInformation);
		this.orderWithStatus = new ArrayList<>();
	}

	protected ItemOrder() {}

	public OrderLine addOrderLine(Item item, Quantity quantity) {
		final OrderLine orderLine = super.addOrderLine(item, quantity);

		final int amount = quantity.getAmount().intValue();
		for (int i = 0; i < amount; i++) {
			orderWithStatus.add(new ItemOrderEntry(item, OrderStatus.OPEN));
		}

		return orderLine;
	}

	public boolean changeAllStatus(OrderStatus status) {
		for (ItemOrderEntry entry : orderWithStatus) {
			entry.setStatus(status);
		}

		return true;
	}

	public boolean changeStatus(Product product, OrderStatus oldStatus, OrderStatus newStatus) {
		for (ItemOrderEntry order : orderWithStatus) {
			if (order.getProduct().equals(product) && order.getStatus() == oldStatus) {
				order.setStatus(newStatus);
				return true;
			}
		}

		return false;
	}

}
