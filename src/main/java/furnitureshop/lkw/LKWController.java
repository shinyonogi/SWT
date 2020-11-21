package furnitureshop.lkw;

import org.salespointframework.time.BusinessTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LKWController {

	private final LKWManager lkwManager;
	private final BusinessTime businessTime;

	LKWController(LKWManager lkwManager, BusinessTime businessTime) {
		this.lkwManager = lkwManager;
		this.businessTime = businessTime;
	}

	@GetMapping("/lkws")
	String getLKWList(Model model) {
		model.addAttribute("types", LKWType.values());

		return "lkws";
	}

	@GetMapping("/lkw/checkout/{name}")
	String getLKWCheckout(@PathVariable(name = "name") String name, Model model) {
		final LKWType type = LKWType.getByName(name);

		if (type == null) {
			return "redirect:/lkws";
		}

		model.addAttribute("lkwform", new LKWCharterForm("", "", businessTime.getTime().toLocalDate()));
		model.addAttribute("type", type);
		model.addAttribute("result", 0);

		return "lkwCheckout";
	}

	@PostMapping(value = "/lkw/checkout/{name}", params = "check")
	String checkLKW(@PathVariable(name = "name") String name, @ModelAttribute LKWCharterForm form, Model model) {
		final LKWType type = LKWType.getByName(name);

		if (type == null) {
			return "redirect:/lkws";
		}

		model.addAttribute("lkwform", form);
		model.addAttribute("type", type);

		if (form.getDate() == null || !form.getDate().isAfter(businessTime.getTime().toLocalDate())) {
			model.addAttribute("result", 3);
			return "lkwCheckout";
		}
		if (!lkwManager.isCharterAvailable(form.getDate(), type)) {
			model.addAttribute("result", 4);
			return "lkwCheckout";
		}

		model.addAttribute("result", -1);

		return "lkwCheckout";
	}


	@PostMapping(value = "/lkw/checkout/{name}", params = "buy")
	String checkoutLKW(@PathVariable(name = "name") String name, @ModelAttribute LKWCharterForm form, Model model) {
		final LKWType type = LKWType.getByName(name);

		if (type == null) {
			return "redirect:/lkws";
		}

		model.addAttribute("lkwform", form);
		model.addAttribute("type", type);

		if (!StringUtils.hasText(form.getName())) {
			model.addAttribute("result", 1);
			return "lkwCheckout";
		}
		if (!StringUtils.hasText(form.getEmail()) || !form.getEmail().matches(".+@.+")) {
			model.addAttribute("result", 2);
			return "lkwCheckout";
		}
		if (form.getDate() == null || !form.getDate().isAfter(businessTime.getTime().toLocalDate())) {
			model.addAttribute("result", 3);
			return "lkwCheckout";
		}
		if (!lkwManager.isCharterAvailable(form.getDate(), type)) {
			model.addAttribute("result", 4);
			return "lkwCheckout";
		}

		final LKW lkw = lkwManager.createCharterOrder(form.getDate(), type);
		if (lkw == null) {
			model.addAttribute("result", 4);
			return "lkwCheckout";
		}

		//TODO Create and display Order

		return "redirect:/lkws";
	}

}
