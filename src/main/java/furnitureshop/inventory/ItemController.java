package furnitureshop.inventory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class ItemController {

	private final ItemManager itemManager;

	public ItemController(ItemManager itemManager) {
		Assert.notNull(itemManager, "ItemManager must not be null");

		this.itemManager = itemManager;
	}

	@GetMapping("/catalog")
	String getCatalog(Model model) {
		model.addAttribute("items", itemManager.findAll());

		return "catalog";
	}

	@GetMapping("/catalog/{type}")
	String getCategory(@PathVariable("type") String category, Model model) {
		final Optional<Category> cat = Category.getByName(category);

		if (cat.isPresent()) {
			model.addAttribute("items", itemManager.findAllByCategory(cat.get()));
			return "catalog";
		}

		return "redirect:/catalog";
	}

	@GetMapping("/catalog/{category}/{itemId}")
	String getItemDetails(Model model, @PathVariable("category") Category category, @PathVariable("itemId") Optional<Item> item) {
		if (item.isPresent()) {
			model.addAttribute("item", item.get());
			return "itemView";
		}

		return "redirect:/catalog/" + category;
	}

}
