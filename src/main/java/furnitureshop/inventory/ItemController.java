package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.javamoney.moneta.Money;
import org.salespointframework.core.Currencies;
import org.salespointframework.time.BusinessTime;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.money.MonetaryAmount;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * This class manages all HTTP Requests for Items
 */
@Controller
public class ItemController {

	private final ItemService itemService;

	// A refernce to the BusinessTime to access the current system time
	private final BusinessTime businessTime;

	/**
	 * Creates a new instance of {@link ItemController}
	 *
	 * @param itemService  The {@link ItemService} to access system information
	 * @param businessTime The {@link BusinessTime} to get the current time
	 *
	 * @throws IllegalArgumentException If any argument is {@code null}
	 */
	public ItemController(ItemService itemService, BusinessTime businessTime) {
		Assert.notNull(itemService, "ItemService must not be null");
		Assert.notNull(businessTime, "BusinessTime must not be null!");

		this.itemService = itemService;
		this.businessTime = businessTime;
	}

	/**
	 * Handles all GET-Requests for '/catalog'
	 *
	 * @param model The {@code Spring} Page {@link Model}
	 *
	 * @return Returns the page with a list of all available {@link Item}s
	 */
	@GetMapping("/catalog")
	String getCatalog(Model model) {
		model.addAttribute("items", itemService.findAllVisible());

		return "catalog";
	}

	/**
	 * Handles all GET-Requests for '/catalog/{type}'.
	 *
	 * @param category The Category of the Item
	 * @param model    The {@code Spring} Page {@link Model}
	 *
	 * @return Returns the page with a list of all available {@link Item}s of
	 * the given {@link Category} if the {@code category} is present. Otherwise it redirects to '/catalog'
	 */
	@GetMapping("/catalog/{type}")
	String getCategory(@PathVariable("type") String category, Model model) {
		final Optional<Category> cat = Category.getByName(category);

		if (cat.isPresent()) {
			model.addAttribute("items", itemService.findAllVisibleByCategory(cat.get()));
			return "catalog";
		}

		return "redirect:/catalog";
	}

	/**
	 * Handles all GET-Requests for '/catalog/{category}/{itemId}'.
	 *
	 * @param category The Category of the {@link Item}
	 * @param item     The requested {@link Item}
	 * @param model    The {@code Spring} Page {@link Model}
	 *
	 * @return Returns a page with details of the {@link Item} if all arguments are given. Otherwise it redirects either
	 * to '/catalog' if no {@code category} is given or to '/catalog/ + category' if the Item is missing.
	 */
	@GetMapping("/catalog/{category}/{itemId}")
	String getItemDetails(@PathVariable("category") String category, @PathVariable("itemId") Optional<Item> item, Model model) {
		if (item.isEmpty()) {
			return "redirect:/catalog/" + category;
		}

		final Optional<Category> cat = Category.getByName(category);

		if (cat.isEmpty() || cat.get() != item.get().getCategory()) {
			return "redirect:/catalog";
		}

		model.addAttribute("item", item.get());
		model.addAttribute("variants", itemService.findAllVisibleByGroupId(item.get().getGroupId()));

		return "itemView";
	}

	/**
	 * Handles all GET-Requests for '/admin/supplier/{id}/items/add'.
	 *
	 * @param suppId The id of a {@link Supplier}
	 * @param model  The {@code Spring} Page {@link Model}
	 *
	 * @return Returns a redirect to '/admin/supplier/{id}/sets/add' if the given supplier is the Set supplier.
	 * Otherwise it populates the model with an instance of itemForm {@link ItemForm} for the addition of new items
	 * and then returns the view for the item addition for normal suppliers.
	 */
	@GetMapping("/admin/supplier/{id}/items/add")
	String getAddItemForSupplier(@PathVariable("id") long suppId, Model model) {
		Optional<Supplier> supplier = itemService.findSupplierById(suppId);

		if (supplier.isEmpty()) {
			return "redirect:/admin/suppliers";
		}

		if (supplier.get().getName().equals("Set Supplier")) {
			return String.format("redirect:/admin/supplier/%s/sets/add", suppId);
		}

		model.addAttribute("itemForm", new ItemForm(0, 0, "", "", "", 0, null, new HashMap<>()));
		model.addAttribute("suppId", suppId);
		model.addAttribute("categories", Category.values());
		model.addAttribute("edit", false);

		return "supplierItemform";
	}

	/**
	 * Handles all POST-Requests for '/admin/supplier/{id}/items/add'.
	 * If the given {@link Supplier} is not present then the page will return the form page again.
	 * Otherwise a new piece will be constructed from the {@link ItemForm} and saved into the {@link ItemCatalog}
	 * via the {@link ItemService}.
	 *
	 * @param suppId The id of a {@link Supplier}
	 * @param form   The {@link ItemForm} with the information about new {@link Item}
	 * @return Returns a redirect to '/admin/supplier/{id}/items' when everything was successfully created. Otherwise
	 * returns the user created {@link ItemForm} and the corresponding view.
	 */
	@PostMapping(value = "/admin/supplier/{id}/items/add", consumes = {"multipart/form-data"})
	String addItemForSupplier(@PathVariable("id") long suppId, @ModelAttribute("itemForm") ItemForm form,
							  @RequestParam("image") MultipartFile file, Model model) {
		final Optional<Supplier> supplier = itemService.findSupplierById(suppId);

		if (supplier.isEmpty()) {
			return "redirect:/admin/suppliers";
		}

		if (supplier.get().getName().equals("Set Supplier")) {
			return String.format("redirect:/admin/supplier/%s/sets/add", suppId);
		}

		model.addAttribute("itemForm", form);
		model.addAttribute("suppId", suppId);
		model.addAttribute("categories", Category.values());

		if (!StringUtils.hasText(form.getName())) {
			// Display error message
			model.addAttribute("result", 1);
			return "supplierItemform";
		}

		if (!StringUtils.hasText(form.getVariant())) {
			// Display error message
			model.addAttribute("result", 2);
			return "supplierItemform";
		}

		if (!StringUtils.hasText(form.getDescription())) {
			// Display error message
			model.addAttribute("result", 3);
			return "supplierItemform";
		}

		if (form.getWeight() <= 0) {
			// Display error message
			model.addAttribute("result", 4);
			return "supplierItemform";
		}

		if (form.getPrice() < 0) {
			model.addAttribute("result", 5);
			return "supplierItemform";
		}

		if (form.getCategory() == null) {
			model.addAttribute("result", 6);
			return "supplierItemform";
		}

		byte[] image = new byte[0];
		try {
			if (file.getBytes().length == 0) {
				model.addAttribute("result", 7);
				return "supplierItemform";
			}
			image = file.getBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}

		final Piece piece = new Piece(
				form.getGroupId(),
				form.getName(),
				Money.of(form.getPrice(), Currencies.EURO),
				image,
				form.getVariant(),
				form.getDescription(),
				supplier.get(),
				form.getWeight(),
				form.getCategory()
		);

		itemService.addOrUpdateItem(piece);

		return String.format("redirect:/admin/supplier/%d/items", suppId);
	}

	/**
	 * Handles all GET-Requests for '/admin/supplier/{id}/sets/add'.
	 * Creates a {@link EnumMap} mapping a {@link Category} to a {@link Streamable} of {@link Item} and binds that to
	 * the model.
	 *
	 * @param suppId The id of a {@link Supplier}
	 * @param model  The {@code Spring} Page {@link Model}
	 *
	 * @return Returns the view for set addition with the proper selection of {@link Item} instances.
	 */
	@GetMapping("/admin/supplier/{suppId}/sets/add")
	String getDetailSetAddPage(@PathVariable("suppId") long suppId, Model model) {
		Optional<Supplier> supplier = itemService.findSupplierById(suppId);

		if (supplier.isEmpty()) {
			return "redirect:/admin/suppliers";
		}

		if (!supplier.get().getName().equals("Set Supplier")) {
			return String.format("redirect:/admin/supplier/%s/items/add", suppId);
		}

		final Map<Category, Streamable<Item>> itemCategoryMap = new EnumMap<>(Category.class);

		for (Category category : Category.values()) {
			itemCategoryMap.put(category, Streamable.of(itemService.findAllByCategory(category).stream().sorted().toArray(Item[]::new)));
		}

		final Map<Item, Integer> itemMap = new HashMap<>();

		for (Item item : itemService.findAll()) {
			itemMap.put(item, 0);
		}

		model.addAttribute("setForm", new ItemForm(0, 0, "", "", "", 0, Category.SET, itemMap));
		model.addAttribute("itemCategoryMap", itemCategoryMap);
		model.addAttribute("suppId", suppId);

		return "supplierSetitems";
	}

	/**
	 * Handles all POST-Requests for '/admin/supplier/{id}/sets/add'.
	 * Creates a new {@link ItemForm} with the given list of items and binds it to the {@link Model}.
	 *
	 * @param suppId   The id of a {@link Supplier}
	 * @param form   A {@link ItemForm} with the information about a new {@link Set}
	 * @param model    The {@code Spring} Page {@link Model}
	 *
	 * @return Either redirects to the supplier overview when {@link Supplier} is not found or the template for
	 * inserting the set information
	 */
	@PostMapping("/admin/supplier/{suppId}/sets/add")
	String getSetForm(@PathVariable("suppId") long suppId, @ModelAttribute("setForm") ItemForm form, Model model) {
		final Optional<Supplier> supplier = itemService.findSupplierById(suppId);

		form.getItems().entrySet().removeIf(entry -> entry.getValue() == 0);

		if (Collections.frequency(form.getItems().values(), 0) == form.getItems().size()) {
			final Map<Category, Streamable<Item>> itemCategoryMap = new EnumMap<>(Category.class);

			for (Category category : Category.values()) {
				itemCategoryMap.put(category, Streamable.of(itemService.findAllByCategory(category).stream().sorted().toArray(Item[]::new)));
			}

			model.addAttribute("lempty", true);
			model.addAttribute("itemCategoryMap", itemCategoryMap);
			model.addAttribute("setForm", form);
			model.addAttribute("suppId", suppId);

			return "supplierSetitems";
		}

		if (supplier.isEmpty()) {
			return "redirect:/admin/suppliers";
		}

		if (!supplier.get().getName().equals("Set Supplier")) {
			return String.format("redirect:/admin/supplier/%d/items", suppId);
		}

		MonetaryAmount maxPrice = Currencies.ZERO_EURO;
		for (Item item : form.getItems().keySet()) {
			int quant = form.getItems().get(item);
			maxPrice = maxPrice.add(item.getPrice()).multiply(quant);
		}

		model.addAttribute("image", null);
		model.addAttribute("maxPrice", maxPrice.getNumber());
		model.addAttribute("setForm", form);
		model.addAttribute("suppId", suppId);
		return "supplierSetform";
	}

	/**
	 * Handles all POST-Requests for '/admin/supplier/{id}/sets/add/set'.
	 * Creates a new {@link Set} and saves it to {@link ItemCatalog} if the given {@link Supplier} is the SetSupplier.
	 *
	 * @param suppId The id of a {@link Supplier}
	 * @param form   A {@link ItemForm} with the information about a new {@link Set}
	 *
	 * @return Either redirects to the supplier overview when {@link Supplier} is not found or to the item overview of
	 * the SetSupplier when everything was correctly created.
	 */
	@PostMapping(value = "/admin/supplier/{suppId}/sets/add/set", consumes = {"multipart/form-data"})
	String addSetForSupplier(@PathVariable("suppId") long suppId, @ModelAttribute("setForm") ItemForm form,
							 @RequestParam("image") MultipartFile file, Model model) {
		final Optional<Supplier> supplier = itemService.findSupplierById(suppId);

		if (supplier.isEmpty()) {
			return "redirect:/admin/suppliers";
		}

		if (!supplier.get().getName().equals("Set Supplier")) {
			return String.format("redirect:/admin/supplier/%d/items", suppId);
		}

		model.addAttribute("setForm", form);
		model.addAttribute("suppId", suppId);

		if (!StringUtils.hasText(form.getName())) {
			// Display error message
			model.addAttribute("result", 1);
			return "supplierSetform";
		}

		if (!StringUtils.hasText(form.getVariant())) {
			// Display error message
			model.addAttribute("result", 2);
			return "supplierSetform";
		}

		if (!StringUtils.hasText(form.getDescription())) {
			// Display error message
			model.addAttribute("result", 3);
			return "supplierSetform";
		}

		MonetaryAmount maxPrice = Currencies.ZERO_EURO;
		for (Item item : form.getItems()) {
			maxPrice = maxPrice.add(item.getPrice());
		}

		if (form.getPrice() < 0 || form.getPrice() > maxPrice.getNumber().doubleValue()) {
			model.addAttribute("result", 5);
			return "supplierSetform";
		}

		byte[] image = new byte[0];
		try {
			if (file.getBytes().length == 0) {
				model.addAttribute("result", 7);
				return "supplierSetform";
			}
			image = file.getBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Item> setItems = new ArrayList<>();

		for (Item item : form.getItems().keySet()) {
			int quantity = form.getItems().get(item);
			while (quantity > 0) {
				setItems.add(item);
				quantity--;
			}
		}

		final Set set = new Set(
				form.getGroupId(),
				form.getName(),
				Money.of(form.getPrice(), Currencies.EURO),
				image,
				form.getVariant(),
				form.getDescription(),
				supplier.get(),
				setItems
		);

		itemService.addOrUpdateItem(set);

		return String.format("redirect:/admin/supplier/%d/items", suppId);
	}

	/**
	 * Handles all GET-Requests for '/admin/supplier/{suppId}/items/edit/{itemId}'.
	 *
	 * @param suppId The id of a {@link Supplier}
	 * @param item   The {@link Item} to be edited
	 * @param model  The {@code Spring} Page {@link Model}
	 *
	 * @return Returns the edit page for a item with all attributes from the given item prefilled.
	 */
	@GetMapping("/admin/supplier/{suppId}/items/edit/{itemId}")
	String getEditItemForSupplier(@PathVariable("suppId") long suppId, @PathVariable("itemId") Item item, Model model) {
		Optional<Supplier> supplier = itemService.findSupplierById(suppId);

		if (supplier.isEmpty()) {
			return "redirect:/admin/suppliers";
		}

		if (supplier.get().getId() != item.getSupplier().getId()) {
			return String.format("redirect:/admin/supplier/%d/items", suppId);
		}

		if (item instanceof Set) {
			Set set = (Set) item;
			model.addAttribute("items", set.getItems());
			model.addAttribute("maxPrice", set.getPartTotal().getNumber());
		}

		model.addAttribute("itemForm", new ItemForm(item.getGroupId(), item.getWeight(), item.getName(), item.getVariant(), item.getDescription(), item.getSupplierPrice().getNumber().doubleValue(), item.getCategory(), new HashMap<>()));
		model.addAttribute("image", null);
		model.addAttribute("suppId", suppId);
		model.addAttribute("itemId", item.getId());
		model.addAttribute("categories", Category.values());
		model.addAttribute("edit", true);

		return "supplierItemform";
	}

	/**
	 * Handles all POST-Requests for '/admin/supplier/{suppId}/items/edit/{itemId}'.
	 * Sets the new information's of an {@link Item} from the {@link ItemForm} and updates the {@link ItemCatalog} via
	 * the {@link ItemService}, but only if the given {@link Supplier} is valid.
	 *
	 * @param suppId The id of a {@link Supplier}
	 * @param item   The {@link Item} to be edited
	 * @param form   The {@link ItemForm} with the information about new {@link Item}
	 *
	 * @return Redirects to the item overview of the given supplier.
	 */
	@PostMapping(value = "/admin/supplier/{suppId}/items/edit/{itemId}", consumes = {"multipart/form-data"})
	String editItemForSupplier(@PathVariable("suppId") long suppId, @PathVariable("itemId") Item item,
			@ModelAttribute("itemForm") ItemForm form, @RequestParam("image") MultipartFile file, Model model) {
		Optional<Supplier> supplier = itemService.findSupplierById(suppId);

		if (supplier.isEmpty()) {
			return "redirect:/admin/suppliers";
		}

		if (supplier.get().getId() != item.getSupplier().getId()) {
			return String.format("redirect:/admin/supplier/%d/items", suppId);
		}

		model.addAttribute("itemForm", form);
		model.addAttribute("edit", true);
		model.addAttribute("suppId", suppId);
		model.addAttribute("itemId", item.getId());
		model.addAttribute("categories", Category.values());

		if (!StringUtils.hasText(form.getName())) {
			// Display error message
			model.addAttribute("result", 1);
			return "supplierItemform";
		}

		if (!StringUtils.hasText(form.getDescription())) {
			// Display error message
			model.addAttribute("result", 3);
			return "supplierItemform";
		}

		if (form.getPrice() < 0) {
			model.addAttribute("result", 5);
			return "supplierItemform";
		}

		if (item instanceof Set) {
			MonetaryAmount maxPrice = Currencies.ZERO_EURO;
			for (Item i : ((Set) item).getItems()) {
				maxPrice = maxPrice.add(i.getPrice());
			}

			if (form.getPrice() > maxPrice.getNumber().doubleValue()) {
				model.addAttribute("result", 5);
				return "supplierItemform";
			}
		}

		item.setName(form.getName());
		item.setPrice(Money.of(form.getPrice(), Currencies.EURO));
		item.setDescription(form.getDescription());

		if (!file.isEmpty()) {
			try {
				item.setImage(file.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		itemService.addOrUpdateItem(item);

		return String.format("redirect:/admin/supplier/%d/items", suppId);
	}

	/**
	 * Handles all POST-Requests for '/admin/supplier/{suppId}/items/delete/{itemId}'.
	 * Deletes the given {@link Item} if the supplier is valid.
	 *
	 * @param suppId The id of a {@link Supplier}
	 * @param item   The {@link Item} to delete
	 *
	 * @return Redirects to the item overview of the given supplier.
	 */
	@PostMapping("/admin/supplier/{suppId}/items/delete/{itemId}")
	String deleteItemForSupplier(@PathVariable("suppId") long suppId, @PathVariable("itemId") Item item) {
		Optional<Supplier> supplier = itemService.findSupplierById(suppId);

		if (supplier.isEmpty()) {
			return "redirect:/admin/suppliers";
		}

		if (supplier.get().getId() != item.getSupplier().getId()) {
			return String.format("redirect:/admin/supplier/%d/items", suppId);
		}

		itemService.removeItem(item);

		return String.format("redirect:/admin/supplier/%d/items", suppId);
	}

	/**
	 * Handles all POST-Requests for '/admin/supplier/{suppId}/items/toggle/{itemId}'.
	 * Changes the visibility status of the given {@link Item} if the supplier is valid.
	 *
	 * @param suppId The id of a {@link Supplier}
	 * @param item   The {@link Item} to be toggle visibility
	 *
	 * @return Redirects to the item overview of the given supplier.
	 */
	@PostMapping("/admin/supplier/{suppId}/items/toggle/{itemId}")
	String toggleItemForSupplier(@PathVariable("suppId") long suppId, @PathVariable("itemId") Item item) {
		Optional<Supplier> supplier = itemService.findSupplierById(suppId);

		if (supplier.isEmpty()) {
			return "redirect:/admin/suppliers";
		}

		if (supplier.get().getId() != item.getSupplier().getId()) {
			return String.format("redirect:/admin/supplier/%d/items", suppId);
		}

		item.setVisible(!item.isVisible());

		itemService.addOrUpdateItem(item);

		return String.format("redirect:/admin/supplier/%d/items", suppId);
	}

	/**
	 * Handles all GET-Requests for '/admin/statistic'.
	 *
	 * @param model The {@code Spring} Page {@link Model}
	 *
	 * @return Returns the montly statistic page.
	 */
	@GetMapping("/admin/statistic")
	String getMonthlyStatistic(Model model) {
		final LocalDate firstOrderDate = itemService.getFirstOrderDate();
		final LocalDateTime time = businessTime.getTime();

		final List<LocalDate> months = getMonthListBetween(firstOrderDate, time.toLocalDate());

		final LocalDate initDate = LocalDate.of(time.getYear(), time.getMonth(), 1);
		final LocalDate compareDate = LocalDate.of(time.getYear(), time.getMonth(), 1).minusMonths(1);

		final List<StatisticEntry> statisticEntries = itemService.analyseProfits(initDate, compareDate);

		model.addAttribute("months", months);
		model.addAttribute("initDate", initDate);
		model.addAttribute("compareDate", compareDate);
		model.addAttribute("statistic", statisticEntries);

		return "monthlyStatistic";
	}


	/**
	 * Handles all POST-Requests for '/admin/statistic'.
	 *
	 * @param init    The String representing the month
	 * @param compare The String representing the month to compare
	 * @param model   The {@code Spring} Page {@link Model}
	 *
	 * @return Returns the montly statistic page with custom months.
	 */
	@PostMapping("/admin/statistic")
	String setMonthlyStatisticValue(@RequestParam("init") String init, @RequestParam("compare") String compare, Model model) {
		final TemporalAccessor initAccessor = DateTimeFormatter.ofPattern("MMMM yyyy").parse(init);
		final int initMonth = initAccessor.get(ChronoField.MONTH_OF_YEAR);
		final int initYear = initAccessor.get(ChronoField.YEAR);
		final LocalDate initDate = LocalDate.of(initYear, initMonth, 1);

		final TemporalAccessor compareAccessor = DateTimeFormatter.ofPattern("MMMM yyyy").parse(compare);
		final int compareMonth = compareAccessor.get(ChronoField.MONTH_OF_YEAR);
		final int compareYear = compareAccessor.get(ChronoField.YEAR);
		final LocalDate compareDate = LocalDate.of(compareYear, compareMonth, 1);

		final LocalDate firstOrderDate = itemService.getFirstOrderDate();
		final LocalDateTime time = businessTime.getTime();

		final List<LocalDate> months = getMonthListBetween(firstOrderDate, time.toLocalDate());

		final List<StatisticEntry> statisticEntries = itemService.analyseProfits(initDate, compareDate);

		model.addAttribute("months", months);
		model.addAttribute("initDate", initDate);
		model.addAttribute("compareDate", compareDate);
		model.addAttribute("statistic", statisticEntries);

		return "monthlyStatistic";
	}

	/**
	 * Handles all GET-Requests for '/catalog/image/{id}'.
	 * Displays the image of the {@link Item}.
	 *
	 * @param item     The {@link Item} from which you want the image
	 * @param response The Resonse written to
	 */
	@GetMapping("/catalog/image/{id}")
	public void getItemImage(@PathVariable("id") Optional<Item> item, HttpServletResponse response) throws IOException {
		if (item.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setContentType("image/jpeg");
		final InputStream is = new ByteArrayInputStream(item.get().getImage());
		IOUtils.copy(is, response.getOutputStream());
	}

	/**
	 * Creates a {@link List} of {@link LocalDate}s with the first day of the month.
	 * The {@link List} contains all months between the start and end date.
	 * If the start date is after the end date, the {@link List} will be empty.
	 *
	 * @param start The first {@link LocalDate}
	 * @param end   The last {@link LocalDate}
	 *
	 * @return A {@link List} with all months between
	 *
	 * @throws IllegalArgumentException If any argument is {@code null}
	 */
	private List<LocalDate> getMonthListBetween(LocalDate start, LocalDate end) {
		Assert.notNull(start, "StartDate must not be null!");
		Assert.notNull(end, "EndDate must not be null!");

		final List<LocalDate> months = new ArrayList<>();

		if (end.isBefore(start)) {
			return months;
		}

		start = LocalDate.of(start.getYear(), start.getMonth(), 1);
		while (!start.isAfter(end)) {
			months.add(start);
			start = start.plusMonths(1);
		}

		return months;
	}

}
