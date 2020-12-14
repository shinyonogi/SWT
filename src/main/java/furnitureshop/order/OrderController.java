package furnitureshop.order;

import furnitureshop.inventory.Item;
import furnitureshop.lkw.LKWType;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.Cart;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.time.BusinessTime;
import org.springframework.data.util.Pair;
import org.springframework.data.util.Streamable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.money.MonetaryAmount;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * This class manages all HTTP Requests for Order and Cart
 */
@Controller
@SessionAttributes("cart")
class OrderController {

	private final OrderService orderService;

	private final BusinessTime businessTime;

	/**
	 * Creates a new {@link OrderController}.
	 *
	 * @param orderService The {@link OrderService} to access system information
	 * @param businessTime The {@link BusinessTime} to get the current time
	 *
	 * @throws IllegalArgumentException If any argument is {@code null}
	 */
	OrderController(OrderService orderService, BusinessTime businessTime) {
		Assert.notNull(orderService, "OrderService must not be null");
		Assert.notNull(businessTime, "OrderService must not be null");

		this.orderService = orderService;
		this.businessTime = businessTime;
	}

	/**
	 * Creates a new {@link Cart} instance to be stored in the session.
	 *
	 * @return a new {@link Cart} instance.
	 */
	@ModelAttribute("cart")
	Cart initializeCart() {
		return new Cart();
	}

	/**
	 * Handles all GET-Request for '/cart'.
	 * User can be directed to the view cart
	 *
	 * @return the view cart
	 */
	@GetMapping("/cart")
	String basket() {
		return "cart";
	}

	/**
	 * Handles all POST-Request for '/cart/add/{id}'.
	 * Adds a {@link Item} to the {@link Cart}.
	 *
	 * @param item     The {@link Item} that should be added to the cart.
	 * @param quantity The amount of {@link Item}s that should be added to the cart.
	 * @param cart     The {@link Cart} of the customer.
	 *
	 * @return the view index
	 */
	@PostMapping("/cart/add/{id}")
	String addItem(@PathVariable("id") Item item, @RequestParam("number") int quantity, @ModelAttribute Cart cart) {
		cart.addOrUpdateItem(item, Quantity.of(quantity));

		return "redirect:/";
	}

	/**
	 * Handles all POST-Request for '/cart/change/{id}'.
	 * Changes the quantity of {@link Item} in the {@link Cart}
	 *
	 * @param cartItemId The product identifier of the item in the cart
	 * @param amount     The new quantity of the item in the cart
	 * @param cart       The {@link Cart} of the customer
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
	 * Handles all POST-Request for '/cart/delete/{id}'.
	 * Deletes a certain {@link Item} in the {@link Cart}
	 *
	 * @param cartItemId The product identifier of the item in the cart
	 * @param cart       The {@link Cart} of the customer.
	 *
	 * @return the view cart
	 */
	@PostMapping("/cart/delete/{id}")
	String deleteItem(@PathVariable("id") String cartItemId, @ModelAttribute Cart cart) {
		cart.removeItem(cartItemId);

		return "redirect:/cart";
	}

	/**
	 * Handles all GET-Request for '/checkout'.
	 * Displays the checkout page for an Itemorder.
	 * Calculates the weight of all {@link Item} in the {@link Cart} and determines the right {@link LKWType} for the order.
	 *
	 * @param cart  The {@link Cart} of the customer.
	 * @param model The {@code Spring} Page {@link Model}
	 *
	 * @return redirects to cart if the cart is empty
	 */
	@GetMapping("/checkout")
	String checkout(@ModelAttribute("cart") Cart cart, Model model) {
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

	/**
	 * Handles all POST-Request for '/checkout'.
	 * Processes the Itemorder checkout
	 *
	 * @param cart  The {@link Cart} of the customer
	 * @param form  The {@link OrderForm} with the information about customer
	 * @param model The {@code Spring} Page {@link Model}
	 *
	 * @return The checkout Page for an ItemOrder
	 */
	@PostMapping("/checkout")
	String buy(@ModelAttribute("cart") Cart cart, @ModelAttribute("orderform") OrderForm form, Model model) {
		final ContactInformation contactInformation = new ContactInformation(form.getName(), form.getAddress(), form.getEmail());

		final int weight = cart.get()
				.filter(c -> c.getProduct() instanceof Item)
				.mapToInt(c -> ((Item) c.getProduct()).getWeight() * c.getQuantity().getAmount().intValue())
				.sum();

		final LKWType type = LKWType.getByWeight(weight).orElse(LKWType.LARGE);

		model.addAttribute("orderform", form);
		model.addAttribute("lkwtype", type);

		// Check if name is invalid
		if (!StringUtils.hasText(form.getName())) {
			// Display error message
			model.addAttribute("result", 1);
			return "orderCheckout";
		}
		// Check if address is invalid
		if (!StringUtils.hasText(form.getAddress())) {
			// Display error message
			model.addAttribute("result", 2);
			return "orderCheckout";
		}
		// Check if email is invalid
		if (!StringUtils.hasText(form.getEmail()) || !form.getEmail().matches(".+@.+")) {
			// Display error message
			model.addAttribute("result", 3);
			return "orderCheckout";
		}

		final ItemOrder order;

		if (form.getIndex() == 0) {
			final Optional<Pickup> pickup = orderService.orderPickupItem(cart, contactInformation);

			if (pickup.isEmpty()) {
				model.addAttribute("result", 4);
				return "orderCheckout";
			}

			order = pickup.get();
		}
		// Delivery
		else if (form.getIndex() == 1) {
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

	/**
	 * Handles all GET-Request for '/order'.
	 * User will be directed to orderSearch page
	 *
	 * @param model The {@code Spring} Page {@link Model}
	 *
	 * @return the view OrderSearch
	 */
	@GetMapping("/order")
	String getOrderPage(Model model) {
		model.addAttribute("result", 0);

		return "orderSearch";
	}

	/**
	 * Handles all POST-Request for '/order'.
	 * User gets either directed to Orderoverview page
	 *
	 * @param id    The Identifier of the order
	 * @param model The {@code Spring} Page {@link Model}
	 *
	 * @return The view orderSearch if there aren't any order, redirects to order/%s page if there is an order
	 */
	@PostMapping("/order")
	String getCheckOrder(@RequestParam("orderId") String id, Model model) {
		final Optional<ShopOrder> shopOrder = orderService.findById(id);

		if (shopOrder.isEmpty()) {
			model.addAttribute("result", 1);
			return "orderSearch";
		}

		return String.format("redirect:/order/%s", id);
	}

	/**
	 * Handles all GET-Request for '/order/{orderId}'.
	 * Displays the Overoverview of an existing Order
	 *
	 * @param id    The Identifier of the order
	 * @param model The {@code Spring} Page {@link Model}
	 *
	 * @return The Orderoverview Page
	 */
	@GetMapping("/order/{orderId}")
	String getOrderOverview(@PathVariable("orderId") String id, Model model) {
		final Optional<ShopOrder> shopOrder = orderService.findById(id);

		if (shopOrder.isEmpty()) {
			return "redirect:/order";
		}

		final ShopOrder order = shopOrder.get();

		if (order instanceof ItemOrder) {
			model.addAttribute("items", ((ItemOrder) order).getOrderEntries());

			MonetaryAmount cancel = Currencies.ZERO_EURO;
			for (ItemOrderEntry entry : ((ItemOrder) order).getOrderEntries()) {
				if (entry.getStatus() == OrderStatus.CANCELLED) {
					cancel = cancel.subtract(entry.getItem().getPrice());
				}
			}

			if (order instanceof Delivery) {
				model.addAttribute("lkw", ((Delivery) order).getLkw());
				model.addAttribute("deliveryDate", ((Delivery) order).getDeliveryDate());
			}
		} else if (order instanceof LKWCharter) {
			model.addAttribute("lkw", ((LKWCharter) order).getLkw());
			model.addAttribute("cancelable", ((LKWCharter) order).getRentDate().isAfter(businessTime.getTime().toLocalDate()));
			model.addAttribute("charterDate", ((LKWCharter) order).getRentDate());
			model.addAttribute("cancel", Currencies.ZERO_EURO);
		} else {
			model.addAttribute("result", 1);
			return "redirect:/order";
		}

		model.addAttribute("order", order);

		return "orderOverview";
	}

	/**
	 * Handles all POST-Request for '/order/{orderId}/cancelItem'.
	 * Cancel an {@link ItemOrderEntry} from an {@link ItemOrder}.
	 *
	 * @param orderId        The Identifier of the {@link ItemOrder}
	 * @param itemEntryId    The Identifier of the {@link ItemOrderEntry}
	 * @param model          The {@code Spring} Page {@link Model}
	 * @param authentication The {@code Spring} {@link Authentication}
	 *
	 * @return The updates Orderoverview Page
	 */
	@PostMapping("/order/{orderId}/cancelItem")
	String cancelItemOrder(@PathVariable("orderId") String orderId, @RequestParam("itemEntryId") long itemEntryId,
			Model model, Authentication authentication) {
		final Optional<ShopOrder> order = orderService.findById(orderId);

		if (order.isEmpty() || !(order.get() instanceof ItemOrder)) {
			if (authentication != null && authentication.isAuthenticated()) {
				return "redirect:/admin/orders";
			}
			return "redirect:/order";
		}

		final ItemOrder itemOrder = ((ItemOrder) order.get());

		orderService.changeItemEntryStatus(itemOrder, itemEntryId, OrderStatus.CANCELLED);

		return String.format("redirect:/order/%s", orderId);
	}

	/**
	 * Handles all POST-Request for '/order/{orderId}/changeStatus'.
	 * Changes the {@link OrderStatus} of an {@link ItemOrderEntry}
	 *
	 * @param orderId        The Identifier of the order
	 * @param status         The new {@link OrderStatus} of the order
	 * @param itemEntryId    The Identifier of the {@link ItemOrderEntry}
	 * @param model          The {@code Spring} Page {@link Model}
	 * @param authentication The {@code Spring} {@link Authentication}
	 *
	 * @return The updates Orderoverview Page
	 */
	@PreAuthorize("hasRole('EMPLOYEE')")
	@PostMapping("/order/{orderId}/changeStatus")
	String changeOrder(@PathVariable("orderId") String orderId, @RequestParam("status") OrderStatus status,
			@RequestParam("itemEntryId") long itemEntryId, Model model, Authentication authentication) {
		final Optional<ShopOrder> order = orderService.findById(orderId);

		if (order.isEmpty() || !(order.get() instanceof ItemOrder)) {
			if (authentication != null && authentication.isAuthenticated()) {
				return "redirect:/admin/orders";
			}
			return "redirect:/order";
		}

		final ItemOrder itemOrder = ((ItemOrder) order.get());

		orderService.changeItemEntryStatus(itemOrder, itemEntryId, status);

		return String.format("redirect:/order/%s", orderId);
	}

	/**
	 * Handles all POST-Request for '/order/{orderId}/cancelLkw'.
	 * Cancel a LKWCharter and deletes the Order.
	 *
	 * @param orderId        The Identifier of the order
	 * @param model          The {@code Spring} Page {@link Model}
	 * @param authentication The {@code Spring} {@link Authentication}
	 *
	 * @return Redirects to Orderlist (Employee) or Index
	 */
	@PostMapping("/order/{orderId}/cancelLkw")
	String cancelLkwOrder(@PathVariable("orderId") String orderId, Model model, Authentication authentication) {
		final Optional<ShopOrder> order = orderService.findById(orderId);

		if (order.isEmpty() || !(order.get() instanceof LKWCharter)) {
			if (authentication != null && authentication.isAuthenticated()) {
				return "redirect:/admin/orders";
			}
			return "redirect:/order";
		}

		final LKWCharter charter = (LKWCharter) order.get();

		orderService.cancelLKW(charter);

		if (authentication != null && authentication.isAuthenticated()) {
			return "redirect:/admin/orders";
		}
		return "redirect:/";
	}

	/**
	 * Handles all GET-Request for '/admin/orders'.
	 * Displays all availble Orders in a {@link List} with Price and {@link OrderStatus}
	 *
	 * @param model The {@code Spring} Page {@link Model}
	 *
	 * @return The Orderslist page
	 */
	@GetMapping("/admin/orders")
	String getCustomerOrders(Model model) {
		final Streamable<Pair<ShopOrder, OrderStatus>> orders = orderService.findAll().map(o -> {
			if (o instanceof ItemOrder) {
				OrderStatus min = OrderStatus.CANCELLED;
				for (ItemOrderEntry entry : ((ItemOrder) o).getOrderEntries()) {
					if (entry.getStatus().ordinal() < min.ordinal()) {
						min = entry.getStatus();
					}
				}
				return Pair.of(o, min);
			} else if (o instanceof LKWCharter) {
				if (((LKWCharter) o).getRentDate().isAfter(businessTime.getTime().toLocalDate())) {
					return Pair.of(o, OrderStatus.PAID);
				} else {
					return Pair.of(o, OrderStatus.COMPLETED);
				}
			}
			return null;
		});

		model.addAttribute("orders", orders.stream()
				.sorted((p1, p2) -> p2.getFirst().getDateCreated().compareTo(p1.getFirst().getDateCreated()))
				.toArray()
		);
		return "customerOrders";
	}

}
