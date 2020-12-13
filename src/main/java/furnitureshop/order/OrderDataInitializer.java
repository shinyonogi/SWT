package furnitureshop.order;

import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemService;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.order.Cart;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Order(25)
@Component
public class OrderDataInitializer implements DataInitializer {

	private final UserAccountManagement userAccountManagement;
	private final ItemService itemService;
	private final OrderService orderService;

	/**
	 * Creates a new instance of {@link OrderDataInitializer}
	 *
	 * @param userAccountManagement The {@link UserAccountManagement} to access the dummy user
	 * @throws IllegalArgumentException if {@code userAccountManagement} argument is {@code null}
	 */
	OrderDataInitializer(UserAccountManagement userAccountManagement, ItemService itemService,
						 OrderService orderService) {
		Assert.notNull(userAccountManagement, "UserAccountManagement must not be null!");
		Assert.notNull(itemService, "ItemService must not be null!");
		Assert.notNull(orderService, "OrderService must not be null!");

		this.userAccountManagement = userAccountManagement;
		this.itemService = itemService;
		this.orderService = orderService;
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

		if (!orderService.findAll().isEmpty()) {
			return;
		}

		userAccountManagement.create("Dummy", Password.UnencryptedPassword.of("123"));

		Random random = new Random();

		Cart cart1 = new Cart();
		Cart cart2 = new Cart();
		Cart cart3 = new Cart();
		List<Cart> carts = Arrays.asList(cart1, cart2, cart3);

		List<Item> items = itemService.findAll().toList();

		for (Cart cart: carts) {
			for (int i = 0; i < 5; i++) {
				cart.addOrUpdateItem(items.get(random.nextInt(items.size())), random.nextInt(2) + 1);
			}
		}

		orderService.orderPickupItem(cart1, new ContactInformation("Hans", "Albertplatz 7", "hans@wurst.de"));
		orderService.orderPickupItem(cart2, new ContactInformation("Herbert", "Luisenhof 7", "hans@wurst.de"));
		orderService.orderDelieveryItem(cart3, new ContactInformation("Fred", "Dorfteich 7", "hans@wurst.de"));
	}
}
