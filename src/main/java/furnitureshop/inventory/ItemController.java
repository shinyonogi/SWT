package furnitureshop.inventory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class ItemController {

	private final ItemService itemService;

	public ItemController(ItemService itemService) {
		Assert.notNull(itemService, "ItemService must not be null");

		this.itemService = itemService;
	}

	@GetMapping("/catalog")
	String getCatalog(Model model) {
		model.addAttribute("items", itemService.findAll());

		return "catalog";
	}

	@GetMapping("/catalog/{type}")
	String getCategory(@PathVariable("type") String category, Model model) {
		final Optional<Category> cat = Category.getByName(category);

		if (cat.isPresent()) {
			model.addAttribute("items", itemService.findAllByCategory(cat.get()));
			return "catalog";
		}

		return "redirect:/catalog";
	}

	@GetMapping("/catalog/{category}/{itemId}")
	String getItemDetails(Model model, @PathVariable("category") Category category, @PathVariable("itemId") Optional<Item> item) {
		if (item.isPresent()) {
			model.addAttribute("item", item.get());
			model.addAttribute("variants", itemService.findAllByGroupId(item.get().getGroupid()));
			return "itemView";
		}

		return "redirect:/catalog/" + category;
	}

}
