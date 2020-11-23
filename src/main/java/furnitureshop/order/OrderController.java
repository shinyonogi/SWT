package furnitureshop.order;

import furnitureshop.inventory.Item;

import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.quantity.Quantity;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.util.Assert;


@Controller
@SessionAttributes("cart")
class OrderController {

	private final OrderManagement<Order> orderManagement;

	OrderController(OrderManagement<Order> orderManagement) {
		Assert.notNull(orderManagement, "OrderManagement must not be null");
		this.orderManagement = orderManagement;
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

	@PostMapping("/cart")
	String addItem(@RequestParam("item") Item item, @RequestParam("number") int number, @ModelAttribute Cart cart) {

		cart.addOrUpdateItem(item, Quantity.of(number));

		return "redirect:/";
	}

	/* Warenkorb leer machen-Funktion */

	@PostMapping("/cart")
	String clearCart(@ModelAttribute Cart cart) {

		cart.clear();

		return "redirect:/cart";
	}

	/*Items löschen-Funktion */

	@PostMapping("/cart{id}")
	String deleteItem(@PathVariable("id") Item item, @ModelAttribute Cart cart) {

		cart.removeItem(item.getId()); //there should be a getId function in item class??
	}

	/* Preis-Summe berechnen-Funktion */

	public double calculatePrice(@ModelAttribute Cart cart) {

		double price = 0;

		for(CartItem cartitem: cart) {
			price =  price + cartitem.getId().getPrice();
		}

		return price;
	}


	/* Bezahlen-Funktion */

	@PostMapping("/checkout")
	String buy(@ModelAttribute Cart cart) {

		var order = new Order();

		cart.addItemsTo(order);

		orderManagement.payOrder(order);
		orderManagement.completeOrder(order);

		cart.clear();

		return "redirect:/";
	}

}
