package furnitureshop.supplier;

import org.springframework.util.Assert;

import javax.persistence.*;

@Entity
public class Supplier {
	
	@Id @GeneratedValue
	private long id;
	
	private String name;
	private double surcharge;		// factor by which the price of furniture gets multiplied by

	protected Supplier() {}

	public Supplier(String name, double surcharge) {
		Assert.notNull(name, "Name must not be null!");
		Assert.isTrue(surcharge > 0, "Surcharge must greater than 0!");

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
