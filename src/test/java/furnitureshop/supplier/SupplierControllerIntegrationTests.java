package furnitureshop.supplier;

import furnitureshop.FurnitureShop;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = FurnitureShop.class)
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
	void redirectsToSuppliersWhenYouAddASupplier() throws Exception {
		mvc.perform(post("/admin/suppliers")
				.param("name", "test")
				.param("surcharge", "1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSuppliersWhenYouAddAnExistingSupplier() throws Exception {
		supplierRepository.save(new Supplier("test", 1));

		mvc.perform(post("/admin/suppliers")
				.param("name", "test")
				.param("surcharge", "1"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("result", is(3)))
				.andExpect(view().name("suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSuppliersWhenYouAddAnSupplierInvalidName() throws Exception {
		mvc.perform(post("/admin/suppliers")
				.param("name", "")
				.param("surcharge", "1"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("result", is(1)))
				.andExpect(view().name("suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void edgeCaseIntegrationTestForAddSupplierSurchargeInvalid() throws Exception {
		mvc.perform(post("/admin/suppliers")
				.param("name", "test")
				.param("surcharge", "-1"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("result", is(2)))
				.andExpect(view().name("suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSuppliersWhenYouDeleteASupplier() throws Exception {
		mvc.perform(post("/admin/supplier/delete/{id}", supplierRepository.findAll().iterator().next().getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewSupplierItemWhenYouTryToReachIt() throws Exception {
		final long id = supplierRepository.findAll().stream().findAny().orElseGet(null).getId();

		mvc.perform(get("/admin/supplier/{id}/items", id))
				.andExpect(status().isOk())
				.andExpect(view().name("supplierItem"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsAdminSuppliersWhenYouChangeTheSurcharge() throws Exception {
		final long id = supplierRepository.findAll().stream().findAny().orElseGet(null).getId();

		mvc.perform(post("/admin/supplier/{id}/surcharge/edit", id)
				.param("surcharge", "0.2"))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

}
