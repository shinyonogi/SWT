package furnitureshop.supplier;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class SupplierController {

	private final SupplierService supplierService;

	SupplierController(SupplierService supplierService) {
		Assert.notNull(supplierService, "SupplierService must not be null!");

		this.supplierService = supplierService;
	}

	@GetMapping("/admin/suppliers")
	String getSupplierList(Model model) {
		model.addAttribute("suppliers", supplierService.findAll());
		model.addAttribute("supplierForm", new SupplierForm("", 5));
		model.addAttribute("result", 0);

		return "suppliers";
	}

	@PostMapping("/admin/suppliers")
	String addSupplier(@ModelAttribute("supplierForm") SupplierForm form, Model model) {
		final Optional<Supplier> suppliers = supplierService.findByName(form.getName());

		// checks if a supplier with the same name is already in the repository
		if (suppliers.isPresent()) {
			model.addAttribute("suppliers", supplierService.findAll());
			model.addAttribute("supplierForm", form);
			// display error message
			model.addAttribute("result", 1);

			return "suppliers";
		}

		// adds the created supplier to the repository while converting the surcharge value from percent to decimal
		supplierService.addSupplier(new Supplier(form.getName(), form.getSurcharge() / 100));

		return "redirect:/admin/suppliers";
	}

	@PostMapping("/admin/supplier/delete/{id}")
	String deleteSupplier(@PathVariable("id") long id){
		supplierService.deleteSupplierById(id);
		return "redirect:/admin/suppliers";
	}

	@GetMapping("/admin/statistic")
	String getMonthlyStatistic() {
		return "monthlyStatistic";
	}

}
