package furnitureshop.order;

import furnitureshop.inventory.Item;

import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.quantity.Quantity;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.util.Assert;


@Controller
@SessionAttributes("cart")
class OrderController {

	private final OrderManagement<ShopOrder> orderManagement;

	OrderController(OrderManagement<ShopOrder> orderManagement) {
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

	/* Bezahlen-Funktion */
	/*
	TODO: proper Implementation with Salespoint Order class
	@PostMapping("/checkout")
	String buy(@ModelAttribute Cart cart) {

		var order = new Order();

		cart.addItemsTo(order);

		orderManagement.payOrder(order);
		orderManagement.completeOrder(order);

		cart.clear();

		return "redirect:/";
	}*/

	@GetMapping("/orders")
	String getOrderPage(){
		return "orders";
	}

	@GetMapping("/customerOrders")
	String getCustomerOrders(){
		return  "customerOrders";
	}

}
