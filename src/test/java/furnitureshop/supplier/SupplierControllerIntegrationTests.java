package furnitureshop.supplier;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class SupplierControllerIntegrationTests {
	
	@Autowired
	MockMvc mvc;
	
	@Autowired
	SupplierRepository supplierRepository;
	
	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewOfSupplier() throws Exception {
		mvc.perform(get("/admin/suppliers").with(user("admin").roles("EMPLOYEE")))
		.andExpect(status().isOk())
		.andExpect(view().name("suppliers"));
	}
	
	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewOfMonthlyStatistic() throws Exception {
		mvc.perform(get("/admin/statistic").with(user("admin").roles("EMPLOYEE")))
		.andExpect(status().isOk())
		.andExpect(view().name("monthlyStatistic"));
	}
	
	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSuppliersWhenYouAddASupplier() throws Exception {
		mvc.perform(post("/admin/suppliers")
				.param("name", "test")
				.param("surcharge", String.valueOf(1)))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
	}
	
	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSuppliersWhenYouDeleteASupplier() throws Exception {
		mvc.perform(post("/admin/supplier/delete/{id}", supplierRepository.findAll().iterator().next().getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
	}
}
