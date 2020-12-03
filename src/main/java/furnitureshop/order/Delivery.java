package furnitureshop.order;

import furnitureshop.lkw.LKW;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class Delivery extends ItemOrder {

	@ManyToOne
	private LKW lkw;

	private LocalDate deliveryDate;

	public Delivery(UserAccount userAccount, ContactInformation contactInformation, LKW lkw, LocalDate deliveryDate) {
		super(userAccount, contactInformation);

		Assert.notNull(deliveryDate, "LKW must not be null!");
		Assert.notNull(deliveryDate, "DeliveryDate must not be null!");

		this.deliveryDate = deliveryDate;
	}

	protected Delivery() {}

	public LKW getLkw() {
		return lkw;
	}

	public LocalDate getDeliveryDate() {
		return deliveryDate;
	}

}
