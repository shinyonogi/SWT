package furnitureshop.supplier;

import javax.persistence.*;

@Entity
public class Supplier {
	
	@Id @GeneratedValue
	private long id;
	
	private String name;
	private double surcharge;		// factor by which the price of furniture gets multiplied by

	protected Supplier() {}

	public Supplier(String name, double surcharge) {
		this.name = name;
		this.surcharge = surcharge;
	}

	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public double getSurcharge() {
		return surcharge;
	}
	
	// for website display
	public double getSurchargeInPercent() {
		return surcharge * 100;
	}

}
