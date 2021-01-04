package furnitureshop.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for {@link AdminController}.
 *
 * @author Sebastian Jaster
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerIntegrationTests {

	@Autowired
	MockMvc mvc;

	/**
	 * Tests if you can access /admin when not logged in.
	 * Expects to be redirected to login.
	 */
	@Test // #1
	void redirectsToLoginPageWhenAccessingAdminResourceWhenNotAuthenticated() throws Exception {
		mvc.perform(get("/admin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", endsWith("/login")));
	}

	/**
	 * Tests if you can access Admin Overview when not logged in.
	 * Expects to be redirected to login.
	 */
	@Test // #2
	void redirectsToLoginPageWhenAccessingAdminOverviewWhenNotAuthenticated() throws Exception {
		mvc.perform(get("/admin/overview"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", endsWith("/login")));
	}

	/**
	 * Tests if you can access Logout when not logged in.
	 * Expects to be redirected to login.
	 */
	/*
	@Test // #3
	void redirectsToLoginPageWhenAccessingLogoutWhenNotAuthenticated() throws Exception {
		mvc.perform(logout("/logout"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", endsWith("/login")));
	}*/

	/**
	 * Tests if you receive the right view when accessing login page.
	 * Expects to receive login view.
	 */
	@Test // #4
	void returnsModelAndViewWhenAccessingLoginWhenNotAuthenticated() throws Exception {
		mvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"));
	}


	/**
	 * Tests if you stay unauthenticated and get redirected when using wron credentials.
	 * Expects to be redirected to login and unauthenticated.
	 */
	@Test // #5
	void redirectsToLoginAndIsUnauthenticatedOnWrongCredentials() throws Exception {
		mvc.perform(formLogin("/login").user("user").password("admin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(unauthenticated())
				.andExpect(header().string("Location", endsWith("/login?error")));
	}

	/**
	 * Tests if you get redirected to Admin Overview, get the correct roles assigned
	 * on login and can access the Admin Overview with the correct role.
	 */
	@Test // #6
	void redirectsToAdminOverviewOnSuccessfulLoginAndAssignsRole() throws Exception {
		mvc.perform(formLogin("/login").user("admin").password("admin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(authenticated().withRoles("EMPLOYEE"));
	}

	/**
	 * Tests if you can access Admin Overview when logged in.
	 * Expect to get admin view.
	 */
	@Test // #7
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewWhenAccessingAdminOverviewWhenAuthenticated() throws Exception {
		mvc.perform(get("/admin/overview").with(user("admin").roles("EMPLOYEE")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin"));
	}

	/**
	 * Tests if you can access the login page when already logged in.
	 * Expects to be redirected to Admin Overview.
	 */
	@Test // #8
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToAdminOverviewPageWhenAccessingLoginAfterAuthentication() throws Exception {
		mvc.perform(get("/login"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/overview"))
				.andExpect(view().name("redirect:/admin/overview"));
	}

	/**
	 * Tests if you get redirected to the logout page and lose authentication.
	 * Expects to be unauthenticated and redirected to login with logout param.
	 */
	@Test // #9
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToLoginWhenAccessingLogoutAfterAuthenticationAndRemovesRoles() throws Exception {
		mvc.perform(logout("/logout"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", endsWith("/login?logout")))
				.andExpect(unauthenticated());
	}

}
