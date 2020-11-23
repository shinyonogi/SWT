package furnitureshop.order;

import furnitureshop.inventory.Item;

import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.quantity.Quantity;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.Assert;

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
	String basket(){
		return "cart";
	}

	/* In den Warenkorb hinzufügen-Funktion */
	@PostMapping("/cart/{id}")
	String addItem(@PathVariable("id") Item item, @RequestParam("number") int number, @ModelAttribute Cart cart) {
		cart.addOrUpdateItem(item, Quantity.of(number));
		return "redirect:/";
	}

	/* Warenkorb leer machen-Funktion */
	@PostMapping("/cart/clear")
	String clearCart(@ModelAttribute Cart cart) {
		cart.clear();

		return "redirect:/cart";
	}

	/*Items löschen-Funktion */
	@PostMapping("/cart/delete/{id}")
	String deleteItem(@PathVariable("id") CartItem item, @ModelAttribute Cart cart) {
		cart.removeItem(item.getId());
		return "redirect:/cart";
	}

	@GetMapping("/checkout")
	String checkout(Model model) {
		model.addAttribute("orderform", new OrderForm("", "", "", 0));
		return "orderCheckout";
	}

	/* Bezahlen-Funktion */
	@PostMapping("/checkout/completeOrder")
	String buy(@ModelAttribute("cart") Cart cart, @ModelAttribute("orderform") OrderForm orderForm, Model model) {
		ContactInformation contactInformation = new ContactInformation(orderForm.getName(), orderForm.getAddress(), orderForm.getEmail());
		ItemOrder itemOrder;

		// Delivery
		if (orderForm.getIndex() == 1) {
			final Optional<Delivery> delivery = orderManager.orderDelieveryItem(cart, contactInformation);

			if (delivery.isEmpty()){
				model.addAttribute("orderform", orderForm);
				return "orderCheckout";
			}
			model.addAttribute("deliveryDate", delivery.get().getDeliveryDate());
			itemOrder = delivery.get();

		} else if (orderForm.getIndex() == 0) { // Pickup
			final Optional<Pickup> pickup = orderManager.orderPickupItem(cart, contactInformation);

			if (pickup.isEmpty()) {
				model.addAttribute("orderform", orderForm);
				return "orderCheckout";
			}
			model.addAttribute("deliveryDate", null);
			itemOrder = pickup.get();

		} else {
			model.addAttribute("orderform", orderForm);
			return "orderCheckout";
		}

		model.addAttribute("order", itemOrder);
		cart.clear();
		return "orderSummary";
	}

	@GetMapping("/checkOrder")
	String getOrderPage(){
		return "checkOrder";
	}

	@PostMapping("/checkOrder")
	String getOrderOverview(@RequestParam("orderId") String orderId) {
		return "";
	}

	@GetMapping("/customerOrders")
	String getCustomerOrders(Model model){
		if (orderManager.findAll().isEmpty()){
			System.out.println("EMPTY");
		}
		for (ShopOrder order : orderManager.findAll().toList()){
			System.out.println(order);
		}
		model.addAttribute("orders", orderManager.findAll());
		return  "customerOrders";
	}

}
