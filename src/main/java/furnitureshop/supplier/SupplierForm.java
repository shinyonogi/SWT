package furnitureshop.supplier;

public class SupplierForm {
	
	private String name;
	private Double surcharge;		// this value is stored in % and converted in the Controller
	
	public SupplierForm(String name, Double surcharge) {
		this.name = name;
		this.surcharge = surcharge;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Double getSurcharge() {
		return surcharge;
	}
	
	public void setSurcharge(Double surcharge) {
		this.surcharge = surcharge;
	}
}
