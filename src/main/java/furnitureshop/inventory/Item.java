package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.salespointframework.catalog.Product;
import org.salespointframework.catalog.ProductIdentifier;

import javax.money.MonetaryAmount;
import javax.persistence.*;

@Entity
public abstract class Item extends Product {
	private int groupid;
	private String picture;
	private String variant;
	private String description;

	@OneToOne(cascade = CascadeType.ALL)
	private Supplier supplier;

	@Enumerated(EnumType.ORDINAL)
	private Category category;

	@SuppressWarnings({ "unused", "deprecation" })
	protected Item() {}

	public Item(int groupid, String name, MonetaryAmount customerPrice, String picture, String variant,
				String description, Supplier supplier, Category category){

		super(name, customerPrice);

		this.groupid = groupid;
		this.picture = picture;
		this.variant = variant;
		this.description = description;
		this.supplier = supplier;
		this.category = category;
	}

	abstract public int getWeight();

	public ProductIdentifier getItemId() {
		return super.getId();
	}

	public int getGroupid() {
		return groupid;
	}

	public String getItemName() {
		return super.getName();
	}

	public MonetaryAmount getCustomerPrice() {
		return super.getPrice();
	}

	public String getPicture() {
		return picture;
	}

	public String getVariant() {
		return variant;
	}

	public String getDescription() {
		return description;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public Category getCategory() {
		return category;
	}
}
