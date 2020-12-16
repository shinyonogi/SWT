package furnitureshop.supplier;

public class SupplierForm {

	private final String name;
	private final double surcharge;

	/**
	 * Creates a new instance of a {@link SupplierForm}
	 *
	 * @param name      Name of the Supplier
	 * @param surcharge Addional part of the price of the {@link furnitureshop.inventory.Item Item}s
	 */
	public SupplierForm(String name, double surcharge) {
		this.name = name;
		this.surcharge = surcharge;
	}

	public String getName() {
		return name;
	}

	public double getSurcharge() {
		return surcharge;
	}
}
