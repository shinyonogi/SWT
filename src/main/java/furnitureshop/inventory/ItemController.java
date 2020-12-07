package furnitureshop.inventory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

/**
 * This class manages all HTTP Requests for Items
 */
@Controller
public class ItemController {

	private final ItemService itemService;

	/**
	 * Creates a new instance of {@link ItemController}
	 *
	 * @param itemService The {@link ItemService} to access system information
	 */
	public ItemController(ItemService itemService) {
		Assert.notNull(itemService, "ItemService must not be null");

		this.itemService = itemService;
	}

	/**
	 * Handles all GET-Request for '/catalog'
	 * @return Returns the page with a list of all available {@link Item}s
	 */
	@GetMapping("/catalog")
	String getCatalog(Model model) {
		model.addAttribute("items", itemService.findAll());

		return "catalog";
	}

	/**
	 * Handles all GET-Request for '/catalog/{type}'
	 *
	 * @param category The Category of the Item
	 * @return Returns the page with a list of all available {@link Item}s of
	 * the given {@link Category} if the {@code category} is present. Otherwise it redirects to '/catalog'
	 */
	@GetMapping("/catalog/{type}")
	String getCategory(@PathVariable("type") String category, Model model) {
		final Optional<Category> cat = Category.getByName(category);

		if (cat.isPresent()) {
			model.addAttribute("items", itemService.findAllByCategory(cat.get()));
			return "catalog";
		}

		return "redirect:/catalog";
	}

	/**
	 * Handles all GET-Request for '/catalog/{category}/{itemId}'
	 *
	 * @param category The Category of the {@link Item}
	 * @param item {@link Item}
	 * @return Returns a page with details of the {@link Item} if all arguments are given. Otherwise it redirects either
	 * to '/catalog' if no {@code category} is given or to '/catalog/ + category' if the Item is missing.
	 */
	@GetMapping("/catalog/{category}/{itemId}")
	String getItemDetails(Model model, @PathVariable("category") String category, @PathVariable("itemId") Optional<Item> item) {
		if (item.isPresent()) {
			final Optional<Category> cat = Category.getByName(category);

			if (cat.isEmpty() || cat.get() != item.get().getCategory()) {
				return "redirect:/catalog";
			}

			model.addAttribute("item", item.get());
			model.addAttribute("variants", itemService.findAllByGroupId(item.get().getGroupid()));
			return "itemView";
		}

		return "redirect:/catalog/" + category;
	}

}
