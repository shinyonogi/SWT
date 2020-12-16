package furnitureshop.supplier;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * This class manages all HTTP requests for suppliers
 */
@Controller
public class SupplierController {

	private final SupplierService supplierService;

	/**
	 * Creates a new instance of {@link SupplierController}
	 *
	 * @param supplierService The {@link SupplierService} to access system information
	 *
	 * @throws IllegalArgumentException If any argument is {@code null}
	 */
	SupplierController(SupplierService supplierService) {
		Assert.notNull(supplierService, "SupplierService must not be null!");

		this.supplierService = supplierService;
	}

	/**
	 * Handles all GET-requests for '/admin/suppliers'.
	 * Displays a page with all suppliers
	 *
	 * @param model The {@code Spring} Page {@link Model}
	 *
	 * @return Opens the supplier page
	 */
	@GetMapping("/admin/suppliers")
	String getSupplierList(Model model) {
		model.addAttribute("suppliers", supplierService.findAll());
		model.addAttribute("supplierForm", new SupplierForm("", 5.0));
		model.addAttribute("result", 0);

		return "suppliers";
	}

	/**
	 * Handles all POST-requests for '/admin/suppliers'.
	 * Adds a new {@link Supplier} to the System
	 *
	 * @param form  The {@link SupplierForm} to check user input when adding a {@link Supplier}
	 * @param model The {@code Spring} Page {@link Model}
	 *
	 * @return Updates the supplier page
	 */
	@PostMapping("/admin/suppliers")
	String addSupplier(@ModelAttribute("supplierForm") SupplierForm form, Model model) {
		final Optional<Supplier> suppliers = supplierService.findByName(form.getName());

		model.addAttribute("suppliers", supplierService.findAll());
		model.addAttribute("supplierForm", form);

		// Check if name is invalid
		if (!StringUtils.hasText(form.getName())) {
			// Display error message
			model.addAttribute("result", 1);
			return "suppliers";
		}
		// Check if address is invalid
		if (form.getSurcharge() < 0) {
			// Display error message
			model.addAttribute("result", 2);
			return "suppliers";
		}
		// Check if a supplier with the same name is already in the repository
		if (suppliers.isPresent()) {
			// Display error message
			model.addAttribute("result", 3);
			return "suppliers";
		}

		// adds the created supplier to the repository while converting the surcharge value from percent to decimal
		supplierService.addSupplier(new Supplier(form.getName(), form.getSurcharge() / 100));

		return "redirect:/admin/suppliers";
	}

	/**
	 * Handles all POST-requests for '/admin/supplier/delete/{id}'.
	 * Temporary page to delete a {@link Supplier}
	 *
	 * @param id id of the {@link Supplier} to be deleted
	 *
	 * @return Redirect to the supplier page
	 */
	@PostMapping("/admin/supplier/delete/{id}")
	String deleteSupplier(@PathVariable("id") long id) {
		final Optional<Supplier> setSupplier = supplierService.findByName("Set Supplier");

		if (setSupplier.isPresent() && setSupplier.get().getId() == id) {
			return "redirect:/admin/suppliers";
		}

		supplierService.deleteSupplierById(id);

		return "redirect:/admin/suppliers";
	}

	/**
	 * Handles all GET-requests for '/admin/supplier/{id}/items'.
	 * Displays a page with all {@link furnitureshop.inventory.Item Item}s associated with the {@link Supplier}
	 *
	 * @param id    id of the {@link Supplier}
	 * @param model The {@code Spring} Page {@link Model}
	 *
	 * @return A the supplier item list page
	 */
	@GetMapping("/admin/supplier/{id}/items")
	String getItemPageForSupplier(@PathVariable("id") long id, Model model) {
		final Optional<Supplier> supplier = supplierService.findById(id);

		supplier.ifPresent(value -> {
			model.addAttribute("items", supplierService.findItemsBySupplier(value));
			model.addAttribute("supplier", value);
		});

		return "supplierItem";
	}

	/**
	 * Handles all POST-requests for '/admin/supplier/{id}/surcharge/edit'.
	 * Changes the supplier surcharge for the given {@link Supplier}
	 *
	 * @param id    id of the {@link Supplier}
	 * @param surcharge value of new surcharge
	 *
	 * @return redirects to supplier overview
	 */
	@PostMapping("/admin/supplier/{id}/surcharge/edit")
	String editSurchargeForSupplier(@PathVariable("id") long id, @RequestParam("surcharge") double surcharge) {
		supplierService.changeSupplierSurcharge(id, surcharge);

		return "redirect:/admin/suppliers";
	}

}
