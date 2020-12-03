package furnitureshop.order;

import furnitureshop.inventory.Item;
import furnitureshop.lkw.LKWType;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
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

	/**
	 * Creates a new {@link OrderController} with the given {@link OrderService}.
	 *
	 * @param orderService must not be {@literal null}.
	 */

	OrderController(OrderService orderService) {
		Assert.notNull(orderService, "OrderService must not be null");

		this.orderService = orderService;
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
	 * @param item the disc that should be added to the cart (may be {@literal null}).
	 * @param number number of items that should be added to the cart.
	 * @param cart must not be {@literal null}.
	 * @return the view index
	 */

	@PostMapping("/cart/{id}")
	String addItem(@PathVariable("id") Item item, @RequestParam("number") int number, @ModelAttribute Cart cart) {
		cart.addOrUpdateItem(item, Quantity.of(number));

		return "redirect:/";
	}

	/**
	 * Changes the quantity of {@link Item} in the {@link Cart}
	 *
	 * @param cartItemId the product identifier of the item in the cart
	 * @param amount the new quantity of the item in the cart
	 * @param cart can be null/empty if every product is going to be removed, cannot be null if a product is going to be added
	 * @return the view cart
	 */

	@PostMapping("/cart/change/{id}")
	String editItem(@PathVariable("id") String cartItemId, @RequestParam("amount") int amount, @ModelAttribute Cart cart) {
		return cart.getItem(cartItemId).map(it -> {
			if (amount <= 0){
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
	 * @param cart can be null/empty if every product is going to be removed
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

		final int weight = cart.get()
				.map(CartItem::getProduct)
				.filter(product -> product instanceof Item)
				.map(product -> ((Item) product))
				.mapToInt(Item::getWeight)
				.sum();

		final Optional<LKWType> type = LKWType.getByWeight(weight);

		if (type.isEmpty()) {
			return "redirect:/cart";
		}

		model.addAttribute("lkwtype", type.get());

		return "orderCheckout";
	}

	/* Bezahlen-Funktion */
	@PostMapping("/checkout/completeOrder")
	String buy(@ModelAttribute("cart") Cart cart, @ModelAttribute("orderform") OrderForm orderForm, Model model) {
		final ContactInformation contactInformation = new ContactInformation(orderForm.getName(), orderForm.getAddress(), orderForm.getEmail());

		final ShopOrder stopOrder;

		// Delivery
		if (orderForm.getIndex() == 1) {
			final Optional<Delivery> delivery = orderService.orderDelieveryItem(cart, contactInformation);

			if (delivery.isEmpty()) {
				model.addAttribute("orderform", orderForm);
				return "orderCheckout";
			}

			model.addAttribute("deliveryDate", delivery.get().getDeliveryDate());
			stopOrder = delivery.get();
		}
		// Pickup
		else if (orderForm.getIndex() == 0) {
			final Optional<Pickup> pickup = orderService.orderPickupItem(cart, contactInformation);

			if (pickup.isEmpty()) {
				model.addAttribute("orderform", orderForm);
				return "orderCheckout";
			}

			model.addAttribute("deliveryDate", null);
			stopOrder = pickup.get();
		} else {
			model.addAttribute("orderform", orderForm);
			return "orderCheckout";
		}

		model.addAttribute("charterDate", null);
		model.addAttribute("order", stopOrder);

		cart.clear();

		return "orderSummary";
	}

	@GetMapping("/checkOrder")
	String getOrderPage() {
		return "orderSearch";
	}

	@PostMapping("/checkOrder")
	String getOrderOverview(@RequestParam("orderId") String orderId, Model model) {
		final Optional<ShopOrder> shopOrder = orderService.findById(orderId);

		if (shopOrder.isEmpty()) {
			return "redirect:/checkOrder";
		}

		final List<Item> itemList = new ArrayList<>();

		if (shopOrder.get() instanceof ItemOrder) {
			for (OrderLine orderline : shopOrder.get().getOrderLines()) {
				final Optional<Item> item = orderService.findItemById(orderline.getProductIdentifier());

				item.ifPresent(itemList::add);
			}
			if (shopOrder.get() instanceof Delivery) {
				model.addAttribute("deliveryDate", ((Delivery) shopOrder.get()).getDeliveryDate());
			}
		} else if (shopOrder.get() instanceof LKWCharter) {
			// LKW Charter
		}

		model.addAttribute("contactInfo", shopOrder.get().getContactInformation());
		model.addAttribute("items", itemList);
		model.addAttribute("order", shopOrder.get());

		return "orderOverview";
	}

	@GetMapping("/admin/orders")
	String getCustomerOrders(Model model) {
		model.addAttribute("orders", orderService.findAll());
		return "customerOrders";
	}

}
