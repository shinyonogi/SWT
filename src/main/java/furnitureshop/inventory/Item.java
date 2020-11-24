package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.salespointframework.catalog.Product;
import org.salespointframework.catalog.ProductIdentifier;
import org.springframework.util.Assert;

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

	@SuppressWarnings({"unused", "deprecation"})
	protected Item() {}

	public Item(int groupid, String name, MonetaryAmount customerPrice, String picture, String variant,
			String description, Supplier supplier, Category category) {
		super(name, customerPrice);

		Assert.notNull(name, "Name must not be null");
		Assert.notNull(customerPrice, "CustomerPrice must not be null");
		Assert.notNull(picture, "Picture must not be null");
		Assert.notNull(variant, "Varient must not be null");
		Assert.notNull(description, "Description must not be null");
		Assert.notNull(supplier, "Supplier must not be null");
		Assert.notNull(category, "Category must not be null");

		this.groupid = groupid;
		this.picture = picture;
		this.variant = variant;
		this.description = description;
		this.supplier = supplier;
		this.category = category;
	}

	public abstract int getWeight();

	public int getGroupid() {
		return groupid;
	}

	@Override
	@SuppressWarnings("NullableProblems")
	public MonetaryAmount getPrice() {
		//TODO Round to 2 decimals
		return super.getPrice().multiply(1 + supplier.getSurcharge());
	}

	public MonetaryAmount getSupplierPrice() {
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
