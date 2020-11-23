package furnitureshop.lkw;

import org.salespointframework.time.BusinessTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class LKWController {

	private final LKWManager lkwManager;
	private final BusinessTime businessTime;

	LKWController(LKWManager lkwManager, BusinessTime businessTime) {
		Assert.notNull(lkwManager, "LKWManager must not be null!");
		Assert.notNull(businessTime, "BusinessTime must not be null!");

		this.lkwManager = lkwManager;
		this.businessTime = businessTime;
	}

	@GetMapping("/lkws")
	String getLKWList(Model model) {
		// Add all types to the model to be displayed
		model.addAttribute("types", LKWType.values());

		return "lkws";
	}

	@GetMapping("/lkw/checkout/{lkwtype}")
	String getLKWCheckout(@PathVariable("lkwtype") String name, Model model) {
		// Get type by name, used to ensure case insensitivity
		final Optional<LKWType> type = LKWType.getByName(name);

		// Check if no type is found -> redirect to type overview
		if (type.isEmpty()) {
			return "redirect:/lkws";
		}

		// Add attributes to model to display form with default values and no errors
		model.addAttribute("lkwform", new LKWCharterForm("", "", "", businessTime.getTime().toLocalDate()));
		model.addAttribute("type", type.get());
		model.addAttribute("result", 0);

		return "lkwCheckout";
	}

	@PostMapping(value = "/lkw/checkout/{lkwtype}", params = "check")
	String checkLKWDate(@PathVariable("lkwtype") String name, @ModelAttribute("lkwform") LKWCharterForm form, Model model) {
		// Get type by name, used to ensure case insensitivity
		final Optional<LKWType> type = LKWType.getByName(name);

		// Check if no type is found -> redirect to type overview
		if (type.isEmpty()) {
			return "redirect:/lkws";
		}

		// Add old attributes to model to keep input
		model.addAttribute("lkwform", form);
		model.addAttribute("type", type.get());

		// Check if date is invalid or before current date
		if (form.getDate() == null || !form.getDate().isAfter(businessTime.getTime().toLocalDate())) {
			// Display error message
			model.addAttribute("result", 4);
			return "lkwCheckout";
		}
		// Check if LKW with given type is available on the date
		if (!lkwManager.isCharterAvailable(form.getDate(), type.get())) {
			// Display error message
			model.addAttribute("result", 5);
			return "lkwCheckout";
		}

		// Display success message
		model.addAttribute("result", -1);

		return "lkwCheckout";
	}

	@PostMapping(value = "/lkw/checkout/{lkwtype}", params = "buy")
	String checkoutLKW(@PathVariable("lkwtype") String name, @ModelAttribute("lkwform") LKWCharterForm form, Model model) {
		// Get type by name, used to ensure case insensitivity
		final Optional<LKWType> type = LKWType.getByName(name);

		// Check if no type is found -> redirect to type overview
		if (type.isEmpty()) {
			return "redirect:/lkws";
		}

		// Add old attributes to model to keep input
		model.addAttribute("lkwform", form);
		model.addAttribute("type", type.get());

		// Check if name is invalid
		if (!StringUtils.hasText(form.getName())) {
			// Display error message
			model.addAttribute("result", 1);
			return "lkwCheckout";
		}
		// Check if address is invalid
		if (!StringUtils.hasText(form.getAddress())) {
			// Display error message
			model.addAttribute("result", 2);
			return "lkwCheckout";
		}
		// Check if email is invalid
		if (!StringUtils.hasText(form.getEmail()) || !form.getEmail().matches(".+@.+")) {
			// Display error message
			model.addAttribute("result", 3);
			return "lkwCheckout";
		}
		// Check if date is invalid or before current date
		if (form.getDate() == null || !form.getDate().isAfter(businessTime.getTime().toLocalDate())) {
			// Display error message
			model.addAttribute("result", 4);
			return "lkwCheckout";
		}

		// Create Calender entry and get used LKW
		final Optional<LKW> lkw = lkwManager.createCharterOrder(form.getDate(), type.get());

		// Check if lkw with given type is available on the date
		if (lkw.isEmpty()) {
			// Display error message
			model.addAttribute("result", 5);
			return "lkwCheckout";
		}

		//TODO Create and display Order

		return "redirect:/lkws";
	}

}
