package furnitureshop.order;

import furnitureshop.inventory.Item;
import furnitureshop.lkw.LKWType;
import org.salespointframework.order.Cart;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.time.BusinessTime;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A Spring MVC controller to manage the {@link Cart}
 *
 * @author Shintaro Onogi
 * @version 1.0
 */

@Controller
@SessionAttributes("cart")
class OrderController {

	private final OrderService orderService;
	private final BusinessTime businessTime;

	/**
	 * Creates a new {@link OrderController} with the given {@link OrderService}.
	 *
	 * @param orderService must not be {@literal null}.
	 */

	OrderController(OrderService orderService, BusinessTime businessTime) {
		Assert.notNull(orderService, "OrderService must not be null");
		Assert.notNull(businessTime, "OrderService must not be null");

		this.orderService = orderService;
		this.businessTime = businessTime;
	}

	/**
	 * Creates a new {@link Cart} instance to be stored in the session
	 * annotation).
	 *
	 * @return a new {@link Cart} instance.
	 */

	@ModelAttribute("cart")
	Cart initializeCart() {
		return new Cart();
	}

	/**
	 * User can be directed to the view cart
	 *
	 * @return the view cart
	 */

	@GetMapping("/cart")
	String basket() {
		return "cart";
	}

	/**
	 * Adds a {@link Item} to the {@link Cart}.
	 *
	 * @param item   the disc that should be added to the cart (may be {@literal null}).
	 * @param number number of items that should be added to the cart.
	 * @param cart   must not be {@literal null}.
	 *
	 * @return the view index
	 */

	@PostMapping("/cart/add/{id}")
	String addItem(@PathVariable("id") Item item, @RequestParam("number") int number, @ModelAttribute Cart cart) {
		cart.addOrUpdateItem(item, Quantity.of(number));

		return "redirect:/";
	}

	/**
	 * Changes the quantity of {@link Item} in the {@link Cart}
	 *
	 * @param cartItemId the product identifier of the item in the cart
	 * @param amount     the new quantity of the item in the cart
	 * @param cart       can be null/empty if every product is going to be removed, cannot be null if a product is going to be added
	 *
	 * @return the view cart
	 */

	@PostMapping("/cart/change/{id}")
	String editItem(@PathVariable("id") String cartItemId, @RequestParam("amount") int amount, @ModelAttribute Cart cart) {
		return cart.getItem(cartItemId).map(it -> {
			if (amount <= 0) {
				cart.removeItem(cartItemId);
			} else {
				final int newValue = amount - it.getQuantity().getAmount().intValue();
				cart.addOrUpdateItem(it.getProduct(), newValue);
			}
			return "redirect:/cart";
		}).orElse("redirect:/cart");
	}

	/**
	 * Deletes a certain {@link Item} in the {@link Cart}
	 *
	 * @param cartItemId the product identifier of the item in the cart
	 * @param cart       can be null/empty if every product is going to be removed
	 *
	 * @return the view cart
	 */
	@PostMapping("/cart/delete/{id}")
	String deleteItem(@PathVariable("id") String cartItemId, @ModelAttribute Cart cart) {
		cart.removeItem(cartItemId);

		return "redirect:/cart";
	}

	@GetMapping("/checkout")
	String checkout(Model model, @ModelAttribute("cart") Cart cart) {
		model.addAttribute("orderform", new OrderForm("", "", "", 0));

		if (cart.isEmpty()) {
			return "redirect:/cart";
		}

		final int weight = cart.get()
				.filter(c -> c.getProduct() instanceof Item)
				.mapToInt(c -> ((Item) c.getProduct()).getWeight() * c.getQuantity().getAmount().intValue())
				.sum();

		final LKWType type = LKWType.getByWeight(weight).orElse(LKWType.LARGE);

		model.addAttribute("result", 0);
		model.addAttribute("lkwtype", type);

		return "orderCheckout";
	}

	/* Bezahlen-Funktion */
	@PostMapping("/checkout")
	String buy(@ModelAttribute("cart") Cart cart, @ModelAttribute("orderform") OrderForm orderForm, Model model) {
		final ContactInformation contactInformation = new ContactInformation(orderForm.getName(), orderForm.getAddress(), orderForm.getEmail());

		final int weight = cart.get()
				.filter(c -> c.getProduct() instanceof Item)
				.mapToInt(c -> ((Item) c.getProduct()).getWeight() * c.getQuantity().getAmount().intValue())
				.sum();

		final LKWType type = LKWType.getByWeight(weight).orElse(LKWType.LARGE);

		model.addAttribute("orderform", orderForm);
		model.addAttribute("lkwtype", type);

		// Check if name is invalid
		if (!StringUtils.hasText(orderForm.getName())) {
			// Display error message
			model.addAttribute("result", 1);
			return "orderCheckout";
		}
		// Check if address is invalid
		if (!StringUtils.hasText(orderForm.getAddress())) {
			// Display error message
			model.addAttribute("result", 2);
			return "orderCheckout";
		}
		// Check if email is invalid
		if (!StringUtils.hasText(orderForm.getEmail()) || !orderForm.getEmail().matches(".+@.+")) {
			// Display error message
			model.addAttribute("result", 3);
			return "orderCheckout";
		}

		final ItemOrder order;

		if (orderForm.getIndex() == 0) {
			final Optional<Pickup> pickup = orderService.orderPickupItem(cart, contactInformation);

			if (pickup.isEmpty()) {
				model.addAttribute("result", 4);
				return "orderCheckout";
			}

			order = pickup.get();
		}
		// Delivery
		else if (orderForm.getIndex() == 1) {
			final Optional<Delivery> delivery = orderService.orderDelieveryItem(cart, contactInformation);

			if (delivery.isEmpty()) {
				model.addAttribute("result", 4);
				return "orderCheckout";
			}

			model.addAttribute("lkw", delivery.get().getLkw());
			model.addAttribute("deliveryDate", delivery.get().getDeliveryDate());
			order = delivery.get();
		}
		// Pickup
		else {
			// Display error message
			model.addAttribute("result", 4);
			return "orderCheckout";
		}

		final List<Pair<Item, Integer>> items = new ArrayList<>();

		outer:
		for (ItemOrderEntry entry : order.getOrderEntries()) {
			for (Pair<Item, Integer> pair : items) {
				if (entry.getItem().equals(pair.getFirst())) {
					items.remove(pair);
					items.add(Pair.of(pair.getFirst(), pair.getSecond() + 1));
					continue outer;
				}
			}
			items.add(Pair.of(entry.getItem(), 1));
		}

		model.addAttribute("items", items);
		model.addAttribute("order", order);

		cart.clear();

		return "orderSummary";
	}

	@GetMapping("/order")
	String getOrderPage(Model model) {
		model.addAttribute("result", 0);

		return "orderSearch";
	}

	@PostMapping("/order")
	String getOrderOverview(@RequestParam("orderId") String orderId, Model model) {
		final Optional<ShopOrder> shopOrder = orderService.findById(orderId);

		if (shopOrder.isEmpty()) {
			model.addAttribute("result", 1);
			return "orderSearch";
		}

		final ShopOrder order = shopOrder.get();

		if (order instanceof ItemOrder) {
			model.addAttribute("items", ((ItemOrder) order).getOrderEntries());
			model.addAttribute("statuses", OrderStatus.values());

			if (order instanceof Delivery) {
				model.addAttribute("lkw", ((Delivery) order).getLkw());
				model.addAttribute("deliveryDate", ((Delivery) order).getDeliveryDate());
			}
		} else if (order instanceof LKWCharter) {
			model.addAttribute("lkw", ((LKWCharter) order).getLkw());
			model.addAttribute("cancelable", ((LKWCharter) order).getRentDate().isAfter(businessTime.getTime().toLocalDate()));
			model.addAttribute("charterDate", ((LKWCharter) order).getRentDate());
		} else {
			model.addAttribute("result", 1);
			return "orderSearch";
		}

		model.addAttribute("order", order);

		return "orderOverview";
	}

	@PostMapping("/cancelOrder")
	String cancelOrder(@RequestParam("orderId") String orderId, @RequestParam("item") String item, Model model) {
		ItemOrder order = (ItemOrder) orderService.findById(orderId).get();
		if (orderService.changeStatus(order, item, OrderStatus.CANCELLED))
			return getOrderOverview(orderId, model);
		else
			return "orderSearch";
	}

	@GetMapping("/admin/orders")
	String getCustomerOrders(Model model) {
		model.addAttribute("orders", orderService.findAll());
		return "customerOrders";
	}

}
