package furnitureshop.supplier;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import furnitureshop.FurnitureShop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = FurnitureShop.class)
public class SupplierControllerIntegrationTests {

	@Autowired
	MockMvc mvc;

	@Autowired
	SupplierRepository supplierRepository;

	SupplierForm supplierForm;


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

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void edgeCaseIntegrationTestForAddSupplierSurchargeInvalid() throws Exception {
		supplierForm = new SupplierForm("Test Supplier", -0.1);
		mvc.perform(post("/admin/suppliers")
				.flashAttr("supplierForm", supplierForm))
				.andExpect(status().isOk())
				.andExpect(view().name("suppliers"));
	}

	/*
	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void edgeCaseIntegrationTestForAddSupplierSupplierAlreadyPresent() throws Exception {
		supplierForm = new SupplierForm("Müller Möbel", 0.1);
		mvc.perform(post("/admin/suppliers")
				.flashAttr("supplierForm", supplierForm))
				.andExpect(status().isOk())
				.andExpect(view().name("suppliers"));
	}
	*/

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewSupplierItemWhenYouTryToReachIt() throws Exception {
		mvc.perform(get("/admin/supplier/{id}/items", supplierRepository.findAll().stream().findAny().get().getId()))
				.andExpect(status().isOk())
				.andExpect(view().name("supplierItem"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsAdminSuppliersWhenYouChangeTheSurcharge() throws Exception {
		mvc.perform(post("/admin/supplier/{id}/surcharge/edit", supplierRepository.findAll().stream().findAny().get().getId())
				.param("surcharge", String.valueOf(0.2)))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

}
