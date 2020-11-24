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

@Controller
@SessionAttributes("cart")
class OrderController {

	private final OrderManager orderManager;

	OrderController(OrderManager orderManager) {
		Assert.notNull(orderManager, "OrderManager must not be null");

		this.orderManager = orderManager;
	}

	@ModelAttribute("cart")
	Cart initializeCart() {
		return new Cart();
	}

	@GetMapping("/cart")
	String basket() {
		return "cart";
	}

	/* In den Warenkorb hinzufügen-Funktion */
	@PostMapping("/cart/{id}")
	String addItem(@PathVariable("id") Item item, @RequestParam("number") int number, @ModelAttribute Cart cart) {
		cart.addOrUpdateItem(item, Quantity.of(number));

		return "redirect:/";
	}

	/* Warenkorb bearbeiten-Funktion */
	@PostMapping("/cart/change/{id}")
	String editItem(@PathVariable("id") String chartItemId, @RequestParam("amount") int amount, @ModelAttribute Cart cart) {
		cart.getItem(chartItemId).map(it -> {
			if (amount <= 0){
				cart.removeItem(chartItemId);
			} else {
				final int newValue = amount - it.getQuantity().getAmount().intValue();
				cart.addOrUpdateItem(it.getProduct(), newValue);
			}
			return "redirect:/cart";
		});

		return "redirect:/cart";
	}

	/*Item löschen-Funktion */
	@PostMapping("/cart/delete/{id}")
	String deleteItem(@PathVariable("id") String chartItemId, @ModelAttribute Cart cart) {
		cart.removeItem(chartItemId);

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
			final Optional<Delivery> delivery = orderManager.orderDelieveryItem(cart, contactInformation);

			if (delivery.isEmpty()) {
				model.addAttribute("orderform", orderForm);
				return "orderCheckout";
			}

			model.addAttribute("deliveryDate", delivery.get().getDeliveryDate());
			stopOrder = delivery.get();
		}
		// Pickup
		else if (orderForm.getIndex() == 0) {
			final Optional<Pickup> pickup = orderManager.orderPickupItem(cart, contactInformation);

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
		final Optional<ShopOrder> shopOrder = orderManager.findById(orderId);

		if (shopOrder.isEmpty()) {
			return "redirect:/checkOrder";
		}

		final List<Item> itemList = new ArrayList<>();

		if (shopOrder.get() instanceof ItemOrder) {
			for (OrderLine orderline : shopOrder.get().getOrderLines()) {
				final Optional<Item> item = orderManager.findItemById(orderline.getProductIdentifier());

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

	@GetMapping("/customerOrders")
	String getCustomerOrders(Model model) {
		model.addAttribute("orders", orderManager.findAll());
		return "customerOrders";
	}

}
