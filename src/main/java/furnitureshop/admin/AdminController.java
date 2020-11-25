package furnitureshop.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

	public AdminController() {}

	@GetMapping("/login")
	String getLogin() {
		return "login";
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping("/admin/overview")
	String getAdminInterface() {
		return "admin";
	}
}
