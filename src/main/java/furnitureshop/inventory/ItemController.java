package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.salespointframework.core.Currencies;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
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

	@GetMapping("/admin/supplier/{id}/items/add")
	String getAddItemForSupplier(@PathVariable("id") long suppId, Model model) {
		model.addAttribute("itemForm", new ItemForm(0, 0, "", "https://via.placeholder.com/900", "", "", 0, null));
		model.addAttribute("suppId", suppId);
		model.addAttribute("categories", Category.values());
		model.addAttribute("edit", false);
		return "supplierItemform";
	}

	@PostMapping("/admin/supplier/{id}/items/add")
	String addItemForSupplier(@PathVariable("id") long suppId, @ModelAttribute("itemForm") ItemForm itemForm, Model model) {
		Optional<Item> newItem = itemService.createItemFromForm(itemForm, suppId);
		if (newItem.isEmpty()){
			model.addAttribute("itemForm", itemForm);
			return "supplierItemform";
		}
		itemService.addItem(newItem.get());
		return "redirect:/admin/supplier/" + suppId + "/items";
	}

	@GetMapping("/admin/supplier/{suppId}/items/edit/{itemId}")
	String getEditItemForSupplier(@PathVariable("suppId") long suppId, @PathVariable("itemId") Item item, Model model){
		if (suppId != item.getSupplier().getId()){
			return "redirect:/admin/supplier/" + suppId + "/items";
		}
		model.addAttribute("itemForm", new ItemForm(item.getGroupid(), item.getWeight(), item.getName(), item.getPicture(), item.getVariant(), item.getDescription(), item.getSupplierPrice().getNumber().doubleValue(), item.getCategory()));
		model.addAttribute("suppId", suppId);
		model.addAttribute("categories", Category.values());
		model.addAttribute("edit", true);
		return "supplierItemform";
	}

	@PostMapping("/admin/supplier/{suppId}/items/edit/{itemId}")
	String editItemForSupplier(@PathVariable("id") long suppId, @PathVariable("itemId") Item item, @ModelAttribute("itemForm") ItemForm itemForm) {
		itemService.editItemFromForm(item, itemForm);
		return "redirect:/admin/supplier/" + suppId + "/items";
	}

	@PostMapping("/admin/supplier/{suppId}/items/delete/{itemId}")
	String deleteItemForSupplier(@PathVariable("suppId") long suppId, @PathVariable("itemId") Item item, Model model){
		itemService.removeItem(item);
		return "redirect:/admin/supplier/" + suppId + "/items";
	}
}
