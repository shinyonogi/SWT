package furnitureshop.inventory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class ItemController {
	private final ItemManager itemManager;

	public ItemController(ItemManager itemManager) {
		this.itemManager = itemManager;
	}

	@GetMapping("/catalog")
	String getCatalog(Model model) {
		model.addAttribute("items", itemManager.findAll());
		return "catalog";
	}

	@GetMapping("/catalog/chair")
	String getChair(Model model) {
		model.addAttribute("items", itemManager.findAllByCategory(Category.CHAIR));
		return "catalog";
	}

	@GetMapping("/catalog/table")
	String getTable(Model model) {
		model.addAttribute("items", itemManager.findAllByCategory(Category.TABLE));
		return "catalog";
	}

	@GetMapping("/catalog/couch")
	String getCouch(Model model) {
		model.addAttribute("items", itemManager.findAllByCategory(Category.COUCH));
		return "catalog";
	}

	@GetMapping("/catalog/{category}/{itemId}")
	String getItemDetails(Model model, @PathVariable("category") Category category, @PathVariable("itemId") Optional<Item> item) {
		if (item.isPresent()) {
			model.addAttribute("item", item.get());
			return "itemView";
		}
		return "catalog/" + category;
	}
}
