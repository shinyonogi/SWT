package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;

@Entity
public class Piece extends Item {

	private int weight;

	protected Piece() {}

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
