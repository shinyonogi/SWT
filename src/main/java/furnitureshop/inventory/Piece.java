package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;

@Entity
public class Piece extends Item{
	private final int weight;

	public Piece(int groupId, String name, MonetaryAmount customerPrice, String picture, String variant,
			   String description, Supplier supplier, int weight, Category category){

		super(groupId, name, customerPrice, picture, variant,
				description, supplier, category);

		this.weight = weight;
	}

	@Override
	public int getWeight() {
		return this.weight;
	}
}
