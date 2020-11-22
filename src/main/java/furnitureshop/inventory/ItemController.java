package furnitureshop.inventory;

import org.springframework.stereotype.Controller;

@Controller
public class ItemController {
	private final ItemManager itemManager;

	public ItemController(ItemManager itemManager) {
		this.itemManager = itemManager;
	}
}
