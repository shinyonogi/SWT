package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.salespointframework.core.Currencies;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import java.math.BigDecimal;
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
	 *
	 * @throws IllegalArgumentException If any of the arguments is {@code null}
	 */
	public Set(int groupId, String name, MonetaryAmount customerPrice, String picture, String variant, String description, Supplier supplier, List<Item> items) {
		super(groupId, name, customerPrice, picture, variant, description, supplier, Category.SET);

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

	public List<Pair<Item, MonetaryAmount>> getItemPrices() {
		final List<Pair<Item, MonetaryAmount>> pieces = new ArrayList<>();

		final MonetaryAmount price = getPrice();

		for (Pair<Item, BigDecimal> part : getItemPriceParts()) {
			pieces.add(Pair.of(part.getFirst(), price.multiply(part.getSecond())));
		}

		return pieces;
	}

	private List<Pair<Item, BigDecimal>> getItemPriceParts() {
		final List<Pair<Item, BigDecimal>> parts = new ArrayList<>();

		//final MonetaryAmount total = getPieceTotal();
		MonetaryAmount total = Currencies.ZERO_EURO;
		for (Item item : items) {
			total = total.add(item.getPrice());
		}

		final MonetaryAmount price = getPrice();

		for (Item item : items) {
			final BigDecimal part = item.getPrice().divide(total.getNumber()).getNumber().numberValue(BigDecimal.class);

			if (item instanceof Set) {
				for (Pair<Item, BigDecimal> pair : ((Set) item).getItemPriceParts()) {
					parts.add(Pair.of(pair.getFirst(), pair.getSecond().multiply(part)));
				}
			} else {
				parts.add(Pair.of(item, part));
			}
		}

		return parts;
	}

}
