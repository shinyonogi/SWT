package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.salespointframework.core.Currencies.EURO;

@Entity
public abstract class Item extends Product {

	private int groupid;

	private String picture;
	private String variant;
	private String description;

	@ManyToOne
	private Supplier supplier;

	@Enumerated(EnumType.ORDINAL)
	private Category category;

	@SuppressWarnings("deprecation")
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

	@Override
	@SuppressWarnings("NullableProblems")
	public MonetaryAmount getPrice() {
		final MonetaryAmount temp = getSupplierPrice().multiply(1.0 + supplier.getSurcharge());

		final BigDecimal price = temp.getNumber().numberValue(BigDecimal.class)
				.setScale(2, RoundingMode.HALF_EVEN);

		return Money.of(price, EURO);
	}

	public MonetaryAmount getSupplierPrice() {
		return super.getPrice();
	}

	public int getGroupid() {
		return groupid;
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

	public abstract int getWeight();

}
