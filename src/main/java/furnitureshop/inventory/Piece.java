package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;

/**
 * A piece of furniture
 */
@Entity
public class Piece extends Item {

	private int weight;

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	protected Piece() {}

	/**
	 * Creates a new instance of  {@link Piece}
	 *
	 * @param groupId       Group which contains all variants of this particular Item
	 * @param name          Name of the Item
	 * @param customerPrice Price of the Item
	 * @param picture       A path to the picture of the Item
	 * @param variant       Variant of the Item
	 * @param description   Description of the Item
	 * @param supplier      Supplier of the Item
	 * @param weight        Weight of the piece
	 * @param category      {@link Category} of the Item
	 */
	public Piece(int groupId, String name, MonetaryAmount customerPrice, String picture, String variant,
			String description, Supplier supplier, int weight, Category category) {
		super(groupId, name, customerPrice, picture, variant,
				description, supplier, category);

		Assert.isTrue(weight > 0, "Weight muss be greater than 0");

		this.weight = weight;
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

}
