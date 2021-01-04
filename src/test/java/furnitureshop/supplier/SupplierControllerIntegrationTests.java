package furnitureshop.supplier;

import furnitureshop.FurnitureShop;
import furnitureshop.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

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

	Supplier defaultSupplier, setSupplier, testSupplier;

	@BeforeEach
	void setUp() {
		Utils.clearRepositories();

		defaultSupplier = new Supplier("default", 0);
		setSupplier = new Supplier("Set Supplier", 0);

		testSupplier = new Supplier("test", 1);

		supplierRepository.saveAll(Arrays.asList(defaultSupplier, setSupplier));
	}

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
		mvc.perform(post("/admin/suppliers")
				.param("name", "default")
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
		mvc.perform(post("/admin/supplier/delete/{id}", defaultSupplier.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSuppliersWhenYouDeleteTheSetSupplier() throws Exception {
		mvc.perform(post("/admin/supplier/delete/{id}", setSupplier.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewSupplierItemWhenYouTryToReachIt() throws Exception {
		mvc.perform(get("/admin/supplier/{id}/items", defaultSupplier.getId()))
				.andExpect(status().isOk())
				.andExpect(view().name("supplierItem"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsAdminSuppliersWhenYouChangeTheSurcharge() throws Exception {
		mvc.perform(post("/admin/supplier/{id}/surcharge/edit", defaultSupplier.getId())
				.param("surcharge", "0.2"))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

}
