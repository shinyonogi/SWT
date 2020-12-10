package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.salespointframework.core.Currencies;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A set of pieces
 */
@Entity
public class Set extends Item {

	@ManyToMany(fetch = FetchType.EAGER)
	private List<Item> items;

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	protected Set() {}

	/**
	 * Creates a new instance of an {@link Item}
	 *
	 * @param groupId       Group which contains all variants of this particular Item
	 * @param name          Name of the Item
	 * @param customerPrice Price of the Item
	 * @param picture       A path to the picture of the Item
	 * @param variant       Variant of the Item
	 * @param description   Description of the Item
	 * @param supplier      Supplier of the Item
	 * @param items         A List of all {@link Item}s in this Set
	 * @param category      {@link Category} of the Item
	 *
	 * @throws IllegalArgumentException If any of the arguments is {@code null}
	 */
	public Set(int groupId, String name, MonetaryAmount customerPrice, String picture, String variant, String description, Supplier supplier, Category category, List<Item> items) {
		super(groupId, name, customerPrice, picture, variant, description, supplier, category);

		Assert.notNull(items, "Items must not be null!");

		this.items = new ArrayList<>(items);
	}

	@Override
	public int getWeight() {
		return items.stream().mapToInt(Item::getWeight).sum();
	}

	@Override
	public MonetaryAmount getPieceTotal() {
		MonetaryAmount singlePrice = Currencies.ZERO_EURO;
		for (Item item : items) {
			singlePrice = singlePrice.add(item.getPieceTotal());
		}
		return singlePrice;
	}

	public List<Item> getItems() {
		return Collections.unmodifiableList(items);
	}

}
