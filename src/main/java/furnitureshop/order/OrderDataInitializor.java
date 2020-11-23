package furnitureshop.order;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderDataInitializor implements DataInitializer {
	private UserAccountManagement userAccountManagement;

	OrderDataInitializor(UserAccountManagement userAccountManagement) {
		this.userAccountManagement = userAccountManagement;
	}

	@Override
	public void initialize() {
		if (!userAccountManagement.findByUsername("Dummy").isPresent()) {
			userAccountManagement.create("Dummy", Password.UnencryptedPassword.of("123"));
		}
	}
}
