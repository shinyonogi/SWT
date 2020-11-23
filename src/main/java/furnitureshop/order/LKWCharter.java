package furnitureshop.order;

import furnitureshop.lkw.LKW;
import org.salespointframework.useraccount.UserAccount;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.time.LocalDate;

@Entity
public class LKWCharter extends ShopOrder {

	private LocalDate rentDate;

	@OneToOne
	private LKW lkw;

	LKWCharter(UserAccount userAccount, LocalDate rentDate, ContactInformation contactInformation, LKW lkw) {
		super(userAccount, contactInformation);
		this.rentDate = rentDate;
		this.lkw = lkw;
	}

	protected LKWCharter() {}

	public LKW getLkw() {
		return lkw;
	}

	public LocalDate getRentDate() {
		return rentDate;
	}

}
