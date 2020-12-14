package furnitureshop.order;

import furnitureshop.lkw.LKW;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class LKWCharter extends ShopOrder {

	@ManyToOne
	private LKW lkw;

	private LocalDate rentDate;

	/**
	 * Empty constructor for {@code Spring}. Not in use.
	 *
	 * @deprecated
	 */
	@Deprecated
	protected LKWCharter() {}

	/**
	 * Creates a new instance of {@link LKWCharter}
	 *
	 * @param userAccount        The dummy {@link UserAccount}
	 * @param contactInformation {@link ContactInformation} of the user
	 * @param lkw                The charted {@link LKW}
	 * @param rentDate           The date the specific LKW is going to be rented
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	LKWCharter(UserAccount userAccount, ContactInformation contactInformation, LKW lkw, LocalDate rentDate) {
		super(userAccount, contactInformation);

		Assert.notNull(lkw, "LKW must not be null!");
		Assert.notNull(rentDate, "RentDate must not be null!");

		this.lkw = lkw;
		this.rentDate = rentDate;
	}

	public LKW getLkw() {
		return lkw;
	}

	public LocalDate getRentDate() {
		return rentDate;
	}

}
