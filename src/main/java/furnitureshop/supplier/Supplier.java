package furnitureshop.supplier;

import javax.persistence.*;

@Entity
public class Supplier {
	
	@Id @GeneratedValue
	private long id;
	
	private String name;
	private Double surcharge;		// factor by which the price of furniture gets multiplied by
	
	
	protected Supplier() {}
	
	public Supplier(String name, Double surcharge) {
		this.name = name;
		this.surcharge = surcharge;
	}
	
	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public Double getSurcharge() {
		return surcharge;
	}
	
	// for website display
	public Double getSurchargeInPercent() {
		return surcharge * 100;
	}
}
