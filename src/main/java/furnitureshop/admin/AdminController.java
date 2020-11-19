package furnitureshop.admin;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdminController {

	public AdminController() {}

	@GetMapping("/")
	String index() { return "index"; }

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
