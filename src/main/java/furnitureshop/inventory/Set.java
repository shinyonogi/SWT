package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Set extends Item {

	@OneToMany(fetch = FetchType.EAGER)
	private List<Item> items;

	protected Set() {}

	public Set(int groupid, String name, MonetaryAmount customerPrice, String picture, String variant, String description, Supplier supplier, Category category, List<Item> items) {
		super(groupid, name, customerPrice, picture, variant, description, supplier, category);

		Assert.notNull(items, "Items must not be null!");

		this.items = items;
	}

	@Override
	public int getWeight() {
		return items.stream().mapToInt(Item::getWeight).sum();
	}

}
