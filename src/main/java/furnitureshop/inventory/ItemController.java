package furnitureshop.inventory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ItemController {
	private final ItemManager itemManager;

	public ItemController(ItemManager itemManager) {
		this.itemManager = itemManager;
	}

	@GetMapping("/catalog")
	String getCatalog(Model model){
		model.addAttribute("items", itemManager.findAll());
		return "catalog";
	}
}
