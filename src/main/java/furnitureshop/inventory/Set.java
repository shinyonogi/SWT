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
 * A set of {@link Item}s ({@link Piece}s or {@link Set}s)
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
	 * @param image         Byte array of the picture of the Item
	 * @param variant       Variant of the Item
	 * @param description   Description of the Item
	 * @param supplier      Supplier of the Item
	 * @param items         A List of all {@link Item}s in this Set
	 *
	 * @throws IllegalArgumentException If any of the arguments is {@code null}
	 */
	public Set(int groupId, String name, MonetaryAmount customerPrice, byte[] image, String variant,
			String description, Supplier supplier, List<Item> items) {
		super(groupId, name, customerPrice, image, variant, description, supplier, Category.SET);

		Assert.notNull(items, "Items must not be null!");

		this.items = new ArrayList<>(items);
	}

	/**
	 * Calulates the total weight of this {@link Set} with the weight of its {@link Item}parts.
	 *
	 * @return The total weight
	 */
	@Override
	public int getWeight() {
		return items.stream().mapToInt(Item::getWeight).sum();
	}

	/**
	 * Calulates the sum of every Price of all {@link Item}s in this {@link Set}.
	 * The total Price is the sum of the Prices of every direct child.
	 * If a {@link Set} is inside of another {@link Set}, it will only use the the {@link Set} Price
	 * and not like {@link Set#getPieceTotal()} the Prices of every {@link Piece} in the Sub{@link Set}.
	 *
	 * @return The total Price of the {@link Set}
	 */
	@Override
	public MonetaryAmount getPartTotal() {
		MonetaryAmount singlePrice = Currencies.ZERO_EURO;
		for (Item item : items) {
			singlePrice = singlePrice.add(item.getPrice());
		}
		return singlePrice;
	}

	/**
	 * Calculates the sum of every Price of all {@link Piece}s in this {@link Set}.
	 * The total Price is the sum of the prices of all pieces not like {@link Item#getPartTotal()}
	 * where only the direct children prices are combined.
	 *
	 * @return The total Price of the {@link Set}
	 */
	public MonetaryAmount getPieceTotal() {
		MonetaryAmount singlePrice = Currencies.ZERO_EURO;
		for (Item item : items) {
			if (item instanceof Set) {
				singlePrice = singlePrice.add(((Set) item).getPieceTotal());
			} else {
				singlePrice = singlePrice.add(item.getPrice());
			}
		}
		return singlePrice;
	}

	/**
	 * Provide an unmodifiable {@link List} with all {@link Item}s of this {@link Set}.
	 *
	 * @return A {@link List} with all Item in this {@link Set}
	 */
	public List<Item> getItems() {
		return Collections.unmodifiableList(items);
	}

	/**
	 * Calculates the part price of every piece in the {@link Set} and Sub{@link Set}s.
	 * It uses the {@link Set} Price to calculate the relative price of the {@link Piece} from the tolal price in this {@link Set} and
	 * translate this to the {@link Set} Price.
	 *
	 * @return {@link List} with every {@link Piece} in this {@link Set} with the part price
	 */
	public List<Pair<Piece, MonetaryAmount>> getPiecePrices() {
		final List<Pair<Piece, MonetaryAmount>> pieces = new ArrayList<>();

		final MonetaryAmount price = getPrice();

		for (Pair<Piece, BigDecimal> part : getPiecePriceParts()) {
			pieces.add(Pair.of(part.getFirst(), price.multiply(part.getSecond())));
		}

		return pieces;
	}

	/**
	 * Calculates the relative price part auf every {@link Piece} in this {@link Set} and Sub{@link Set}s.
	 *
	 * @return {@link List} with every {@link Piece} in this {@link Set} with the relative Price part
	 */
	private List<Pair<Piece, BigDecimal>> getPiecePriceParts() {
		final List<Pair<Piece, BigDecimal>> parts = new ArrayList<>();

		final MonetaryAmount total = getPartTotal();
		final MonetaryAmount price = getPrice();

		for (Item item : items) {
			final BigDecimal part = item.getPrice().divide(total.getNumber()).getNumber().numberValue(BigDecimal.class);

			if (item instanceof Set) {
				for (Pair<Piece, BigDecimal> pair : ((Set) item).getPiecePriceParts()) {
					parts.add(Pair.of(pair.getFirst(), pair.getSecond().multiply(part)));
				}
			} else if (item instanceof Piece) {
				parts.add(Pair.of((Piece) item, part));
			}
		}

		return parts;
	}

}
