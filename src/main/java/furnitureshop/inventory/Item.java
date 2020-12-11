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

/**
 * This class represents an Item.
 */
@Entity
public abstract class Item extends Product {

	private int groupId;

	private String picture;
	private String variant;
	private String description;

	@ManyToOne
	private Supplier supplier;

	@Enumerated(EnumType.ORDINAL)
	private Category category;

	private boolean visible;

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	protected Item() {}

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
	 * @param category      {@link Category} of the Item
	 *
	 * @throws IllegalArgumentException If any of the arguments is {@code null}
	 */
	public Item(int groupId, String name, MonetaryAmount customerPrice, String picture, String variant,
			String description, Supplier supplier, Category category) {
		super(name, customerPrice);

		Assert.notNull(name, "Name must not be null");
		Assert.notNull(customerPrice, "CustomerPrice must not be null");
		Assert.notNull(picture, "Picture must not be null");
		Assert.notNull(variant, "Varient must not be null");
		Assert.notNull(description, "Description must not be null");
		Assert.notNull(supplier, "Supplier must not be null");
		Assert.notNull(category, "Category must not be null");

		this.groupId = groupId;
		this.picture = "/resources/img/" + picture;
		this.variant = variant;
		this.description = description;
		this.supplier = supplier;
		this.category = category;

		this.visible = true;
	}

	/**
	 * Calculates the Customerprice of this {@link Item}.
	 * It contains the {@link Supplier} Price and the surcharge of it.
	 *
	 * @return The Price of this {@link Item}
	 */
	@Override
	@SuppressWarnings("NullableProblems")
	public MonetaryAmount getPrice() {
		final MonetaryAmount temp = getSupplierPrice().multiply(1.0 + supplier.getSurcharge());

		final BigDecimal price = temp.getNumber().numberValue(BigDecimal.class)
				.setScale(2, RoundingMode.HALF_EVEN);

		return Money.of(price, EURO);
	}

	/**
	 * Calculates the Supplierprice of this {@link Item}.
	 * It contains the {@link Supplier} Price with no surcharge.
	 *
	 * @return The Price of this {@link Item}
	 */
	public MonetaryAmount getSupplierPrice() {
		return super.getPrice();
	}

	public int getGroupId() {
		return groupId;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		Assert.notNull(picture, "Picture must not be null");

		this.picture = picture;
	}

	public String getVariant() {
		return variant;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		Assert.notNull(description, "Description must not be null");

		this.description = description;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public Category getCategory() {
		return category;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public abstract MonetaryAmount getPartTotal();

	public abstract int getWeight();

}
