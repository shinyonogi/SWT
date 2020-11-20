package furnitureshop.lkw;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LKWController {

	private final LKWManager lkwManager;

	LKWController(LKWManager lkwManager) {
		this.lkwManager = lkwManager;
	}

	@GetMapping("/lkw")
	String getLKWList(Model model) {

		model.addAttribute("types", LKWType.values());

		return "lkw";
	}

	@GetMapping("/lkw/{type}")
	String getLKWCheckout(@PathVariable LKWType type, Model model) {
		return "lkwCheckout";
	}

}
