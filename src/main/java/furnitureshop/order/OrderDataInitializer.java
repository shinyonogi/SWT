package furnitureshop.order;

import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemService;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.order.Cart;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Order(25)
@Component
public class OrderDataInitializer implements DataInitializer {

	private final UserAccountManagement userAccountManagement;
	private final ItemService itemService;
	private final OrderService orderService;
	private final BusinessTime businessTime;

	/**
	 * Creates a new instance of {@link OrderDataInitializer}
	 *
	 * @param userAccountManagement The {@link UserAccountManagement} to access the dummy user
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	OrderDataInitializer(UserAccountManagement userAccountManagement, ItemService itemService,
			OrderService orderService, BusinessTime businessTime) {
		Assert.notNull(userAccountManagement, "UserAccountManagement must not be null!");
		Assert.notNull(itemService, "ItemService must not be null!");
		Assert.notNull(orderService, "OrderService must not be null!");
		Assert.notNull(businessTime, "BusinessTime must not be null!");

		this.userAccountManagement = userAccountManagement;
		this.itemService = itemService;
		this.orderService = orderService;
		this.businessTime = businessTime;
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

		final Random random = new Random();
		final List<Item> items = itemService.findAll().toList();

		final ContactInformation[] infos = {
				new ContactInformation("Hans", "Albertplatz 7", "hans@wurst.de"),
				new ContactInformation("Herbert", "Luisenhof 7", "hans@wurst.de"),
				new ContactInformation("Fred", "Dorfteich 7", "hans@wurst.de")
		};

		for (int i = 0; i < 15; i++) {
			final Cart cart = new Cart();
			final int max = random.nextInt(5) + 2;

			for (int j = 0; j < max; j++) {
				cart.addOrUpdateItem(items.get(random.nextInt(items.size())), random.nextInt(2) + 1L);
			}

			final ContactInformation temp = infos[random.nextInt(infos.length)];
			final ContactInformation info = new ContactInformation(temp.getName(), temp.getAddress(), temp.getEmail());

			final Optional<? extends ItemOrder> order;
			if (random.nextBoolean()) {
				order = orderService.orderPickupItem(cart, info);
			} else {
				order = orderService.orderDelieveryItem(cart, info);
			}

			order.ifPresent(o -> {
				for (ItemOrderEntry entry : new ArrayList<>(o.getOrderEntries())) {
					final OrderStatus status = OrderStatus.values()[random.nextInt(OrderStatus.values().length - 2) + 2];
					orderService.changeItemEntryStatus(o, entry.getId(), status);
				}
			});

			businessTime.forward(Duration.of(-80, ChronoUnit.HOURS));
		}

		businessTime.forward(businessTime.getOffset().negated());
	}
}
