package furnitureshop.order;

import furnitureshop.lkw.LKW;
import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDate;

public class LKWCharter extends ShopOrder {
	private LocalDate rentDate;
	private LKW lkw;

	LKWCharter(UserAccount userAccount, LocalDate rentDate, ContactInformation contactInformation, LKW lkw) {
		super(userAccount, contactInformation);
		this.rentDate = rentDate;
		this.lkw = lkw;
	}
}
