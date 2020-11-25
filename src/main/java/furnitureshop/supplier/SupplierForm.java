package furnitureshop.supplier;

public class SupplierForm {

	private final String name;
	private final double surcharge;        // this value is stored in % and converted in the Controller

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
