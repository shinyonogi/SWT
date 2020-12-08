package furnitureshop.order;

import furnitureshop.inventory.Item;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public abstract class ItemOrder extends ShopOrder {

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<ItemOrderEntry> orderWithStatus;

	@Deprecated
	@SuppressWarnings("DeprecatedIsStillUsed")
	protected ItemOrder() {}

	public ItemOrder(UserAccount userAccount, ContactInformation contactInformation) {
		super(userAccount, contactInformation);

		this.orderWithStatus = new ArrayList<>();
	}

	@Override
	@SuppressWarnings("NullableProblems")
	public OrderLine addOrderLine(Product product, Quantity quantity) {
		final OrderLine orderLine = super.addOrderLine(product, quantity);

		if (product instanceof Item) {
			final int amount = quantity.getAmount().intValue();
			for (int i = 0; i < amount; i++) {
				orderWithStatus.add(new ItemOrderEntry((Item) product, OrderStatus.OPEN));
			}
		}

		return orderLine;
	}

	public List<ItemOrderEntry> getOrderEntries() {
		return Collections.unmodifiableList(orderWithStatus);
	}

}
