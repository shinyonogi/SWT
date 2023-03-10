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
	 * @param image         Byte array of the picture of the Item
	 * @param variant       Variant of the Item
	 * @param description   Description of the Item
	 * @param supplier      Supplier of the Item
	 * @param weight        Weight of the piece
	 * @param category      {@link Category} of the Item
	 */
	public Piece(int groupId, String name, MonetaryAmount customerPrice, byte[] image, String variant,
			String description, Supplier supplier, int weight, Category category) {
		super(groupId, name, customerPrice, image, variant,
				description, supplier, category);

		Assert.isTrue(category != Category.SET, "Category must not be Set");
		Assert.isTrue(weight > 0, "Weight muss be greater than 0");

		this.weight = weight;
	}

	@Override
	public MonetaryAmount getPartTotal() {
		return getPrice();
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

}
