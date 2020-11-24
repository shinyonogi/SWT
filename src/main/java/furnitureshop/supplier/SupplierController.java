package furnitureshop.supplier;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SupplierController {
	
	private final SupplierManager supplierManager;
	
	public SupplierController(SupplierManager supplierManager) {
		Assert.notNull(supplierManager, "supplierManager must not be null!");
		this.supplierManager = supplierManager;
	}
	
	@GetMapping("/suppliers")
	public String getSupplierList(@ModelAttribute SupplierForm supplierForm, Model model) {
		
		model.addAttribute("suppliers", supplierManager.findAll());
		model.addAttribute("supplierForm", supplierForm);
		model.addAttribute("result", 0);
		
		return "suppliers";
	}
	
	@PostMapping("/suppliers")
	public String addSupplier(@ModelAttribute SupplierForm supplierForm, Model model) {
		
		model.addAttribute("supplierForm", supplierForm);
		
		// checks if a supplier with the same name is already in the repository
		Iterable<Supplier> suppliers = supplierManager.findAll();
		for(Supplier supplier : suppliers) {
			if (supplier.getName().contentEquals(supplierForm.getName())) {
				// display error message
				model.addAttribute("result", 1);
				return "suppliers";
			}
		}
		
		// adds the created supplier to the repository while converting the surcharge value from percent to decimal
		supplierManager.addSupplier(new Supplier(supplierForm.getName(), supplierForm.getSurcharge() / 100));
		
		return "redirect:/suppliers";
	}
	
	@GetMapping("/deleteSupplier/{id}")
	public String deleteBuyer(@PathVariable Long id){
		supplierManager.deleteSupplier(id);
		return "redirect:/suppliers";
	}

	@GetMapping("/statistic")
	String getMonthlyStatistic(){
		return "monthlyStatistic";
	}
}
