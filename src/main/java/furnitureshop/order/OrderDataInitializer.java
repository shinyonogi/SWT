package furnitureshop.order;

import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemCatalog;
import furnitureshop.inventory.ItemService;
import furnitureshop.lkw.LKWService;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.order.Cart;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Order(21)
@Component
public class OrderDataInitializer implements DataInitializer {

	private final UserAccountManagement userAccountManagement;
	private final LKWService lkwService;
	private final ItemService itemService;
	private final OrderService orderService;
	private final ItemCatalog itemCatalog;

	@Autowired
	private OrderManagement<ShopOrder> orderManagement;

	/**
	 * Creates a new instance of {@link OrderDataInitializer}
	 *
	 * @param userAccountManagement The {@link UserAccountManagement} to access the dummy user
	 * @throws IllegalArgumentException if {@code userAccountManagement} argument is {@code null}
	 */
	OrderDataInitializer(UserAccountManagement userAccountManagement, LKWService lkwService, ItemService itemService,
						 OrderService orderService, ItemCatalog itemCatalog) {
		this.userAccountManagement = userAccountManagement;
		this.lkwService = lkwService;
		this.itemService = itemService;
		this.orderService = orderService;
		this.itemCatalog = itemCatalog;
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

		UserAccount account = userAccountManagement.create("Dummy", Password.UnencryptedPassword.of("123"));
		userAccountManagement.save(account);

		Random random = new Random();
		List<Item> items = itemService.findAll().toList();

		Cart exampleCart1 = new Cart();
		/*Cart exampleCart2 = new Cart();
		Cart exampleCart3 = new Cart();
		List<Cart> carts = Arrays.asList(exampleCart1, exampleCart2, exampleCart3);
		for (Cart cart: carts) {*/
		for (int i = 0; i < 5; i++) {
			exampleCart1.addOrUpdateItem(items.get(random.nextInt(items.size())), random.nextInt(2) + 1);
		}
		//}
		Optional<Delivery> order = orderService.orderDelieveryItem(exampleCart1, new ContactInformation("Heinz Erhardt", "Lindenstraße 14"
				, "heinz.erhardt67@gmail.com"));
		/*orderService.orderPickupItem(exampleCart2, new ContactInformation("Max Mustermann", "Hauptstraße 1",
				"max-mustermann@gmx.de"));
		orderService.orderDelieveryItem(exampleCart3, new ContactInformation("Jane Doe", "Goethestraße 24",
				"doe.jane24@web.de"));
		*/
		if (!exampleCart1.isEmpty())
			throw new IllegalArgumentException("1");
		if (order.isEmpty())
			throw new IllegalArgumentException("2");
		Assert.isTrue(!exampleCart1.isEmpty());
		Assert.isTrue(orderManagement.findBy(orderService.getDummyUser().get()).stream().count() > 0);
		Assert.isTrue(order.isEmpty(), "Beim Initialisieren der OrderDaten konnte eine Bestellung nicht ausgeführt werden");
		Assert.isInstanceOf(order.getClass(), orderService.findById(order.get().getId().getIdentifier()));
	}

}
