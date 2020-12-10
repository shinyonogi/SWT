package furnitureshop.order;

import furnitureshop.lkw.LKW;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

/**
 * This class represents the delivery type {@link Delivery} by {@link LKW}
 */
@Entity
public class Delivery extends ItemOrder {

	@ManyToOne
	private LKW lkw;

	private LocalDate deliveryDate;

	@Deprecated
	protected Delivery() {}

	/**
	 * Creates a new instance of {@link Delivery}
	 *
	 * @param userAccount        The dummy {@link UserAccount}
	 * @param contactInformation {@link ContactInformation} of the user
	 * @param lkw                The {@link LKW} which will be used
	 * @param deliveryDate       The delivery date of the order
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public Delivery(UserAccount userAccount, ContactInformation contactInformation, LKW lkw, LocalDate deliveryDate) {
		super(userAccount, contactInformation);

		Assert.notNull(lkw, "LKW must not be null!");
		Assert.notNull(deliveryDate, "DeliveryDate must not be null!");

		this.lkw = lkw;
		this.deliveryDate = deliveryDate;
	}

	public LKW getLkw() {
		return lkw;
	}

	public LocalDate getDeliveryDate() {
		return deliveryDate;
	}

	@Override
	@SuppressWarnings("NullableProblems")
	public MonetaryAmount getTotal() {
		return super.getTotal().add(lkw.getType().getDelieveryPrice());
	}

}
