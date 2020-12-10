package furnitureshop.order;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.stereotype.Component;

/**
 * This class initializes an OrderData
 */

@Component
public class OrderDataInitializor implements DataInitializer {

	private final UserAccountManagement userAccountManagement;

	/**
	 * Creates a new instance of {@link OrderDataInitializor}
	 *
	 * @param userAccountManagement The {@link UserAccountManagement} to access the dummy user
	 *
	 * @throws IllegalArgumentException if {@code userAccountManagement} argument is {@code null}
	 */
	OrderDataInitializor(UserAccountManagement userAccountManagement) {
		this.userAccountManagement = userAccountManagement;
	}

	/**
	 * This method initializes an (dummy-)user.
	 * It returns if a dummy user already exists, creates a new dummy user if it doesn't exist
	 */
	@Override
	public void initialize() {
		if (userAccountManagement.findByUsername("Dummy").isPresent()) {
			return;
		}

		userAccountManagement.create("Dummy", Password.UnencryptedPassword.of("123"));
	}

}
