package furnitureshop.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * Spring MVC Controller to manage the login and admin routes.
 * Admin route is secured by {@link PreAuthorize} clause.
 *
 * @author Sebastian Jaster
 */
@Controller
public class AdminController {

	/**
	 * Creates a new {@link AdminController}
	 */
	public AdminController() {
		// Do nothing because AdminController has no dependencies regarding other services.
	}

	/**
	 * Gets the login view except when user is already authenticated
	 * then it redirects to {@code getAdminInterface()}
	 *
	 * @param authentication provides the current authentication context
	 * @return login view or redirect to admin overview when logged in
	 */
	@GetMapping("/login")
	String getLogin(Authentication authentication) {
		if(authentication != null && authentication.isAuthenticated()) {
			return "redirect:/admin/overview";
		}
		return "login";
	}

	/**
	 * Returns the admin view only for authenticated employees
	 * hence the usage of {@code PreAuthorize}
	 *
	 * @return Admin view
	 */
	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping("/admin/overview")
	String getAdminInterface() {
		return "admin";
	}
}
