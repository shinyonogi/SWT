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
		
		model.addAttribute("suppliers", supplierManager.getSupplierRepository().findAll());
		model.addAttribute("supplierForm", supplierForm);
		
		return "suppliers";
	}
	
	@PostMapping("/suppliers")
	public String addSupplier(@ModelAttribute SupplierForm supplierForm, Model model) {
		
		model.addAttribute("supplierForm", supplierForm);
		
		supplierManager.addSupplier(new Supplier(supplierForm.getName(), 0));
		
		return "redirect:/suppliers";
	}
	
	@GetMapping("/deleteSupplier/{id}")
	public String deleteBuyer(@PathVariable Long id){
		supplierManager.deleteSupplier(id);
		return "redirect:/suppliers";
	}
}
