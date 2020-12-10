package furnitureshop.inventory;

import furnitureshop.FurnitureShop;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = FurnitureShop.class)
public class ItemControllerIntegrationTests {

	@Autowired
	MockMvc mvc;

	@Autowired
	ItemCatalog itemCatalog;

	@Autowired
	SupplierRepository supplierRepository;

	@Autowired
	ItemService itemService;

	final Supplier supplier = new Supplier("Supplier 1", 0.15);

	final Supplier setSupplier = new Supplier("Set Supplier", 0.05);

	Piece sofa_grey = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "sofa_2_grey.jpg", "grau",
			"Sofa 1 in grau.", supplier, 80, Category.COUCH);

	Piece sofa1_grey = new Piece(3, "Sofa 1", Money.of(259.99, Currencies.EURO), "sofa_2_grey.jpg", "grau",
			"Sofa 1 in grau.", supplier, 80, Category.COUCH);

	Piece stuhl = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "chair_2.jpg", "schwarz",
			"Stuhl 1 in schwarz.", supplier, 5, Category.CHAIR);

	Set set = new Set(4, "Set 1", Money.of(299.99, Currencies.EURO), "set_1.jpg", "black",
			"Set bestehend aus Sofa 1 und Stuhl 1.", setSupplier, Arrays.asList(stuhl, sofa_grey));

	@BeforeEach
	void setUp() {
		itemCatalog.deleteAll();
		supplierRepository.deleteAll();

		final List<Supplier> suppliers = Arrays.asList(supplier, setSupplier);
		final List<Item> items = new ArrayList<>();

		Piece tisch = new Piece(3, "Tisch 1", Money.of(89.99, Currencies.EURO), "table_2.jpg", "weiß",
				"Tisch 1 in weiß.", supplier, 30, Category.TABLE);

		items.add(set);
		items.add(tisch);
		items.add(stuhl);
		items.add(sofa_grey);

		supplierRepository.saveAll(suppliers);
		itemCatalog.saveAll(items);
	}

	@Test
	void returnsModelAndViewOnCatalogOverview() throws Exception {
		mvc.perform(get("/catalog"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("items", contains(itemService.findAll().stream().toArray())))
				.andExpect(view().name("catalog"));
	}

	@Test
	void returnsModelAndViewOnCouchOverview() throws Exception {
		mvc.perform(get("/catalog/{category}", Category.COUCH))
				.andExpect(status().isOk())
				.andExpect(model().attribute("items", contains(itemService.findAllByCategory(Category.COUCH).stream().toArray())))
				.andExpect(view().name("catalog"));
	}

	@Test
	void returnsModelAndViewOnTableOverview() throws Exception {
		mvc.perform(get("/catalog/{category}", Category.TABLE))
				.andExpect(status().isOk())
				.andExpect(model().attribute("items", contains(itemService.findAllByCategory(Category.TABLE).stream().toArray())))
				.andExpect(view().name("catalog"));
	}

	@Test
	void returnsModelAndViewOnChairOverview() throws Exception {
		mvc.perform(get("/catalog/{category}", Category.CHAIR))
				.andExpect(status().isOk())
				.andExpect(model().attribute("items", contains(itemService.findAllByCategory(Category.CHAIR).stream().toArray())))
				.andExpect(view().name("catalog"));
	}

	@Test
	void returnsModelAndViewOnSetOverview() throws Exception {
		mvc.perform(get("/catalog/{category}", Category.SET))
				.andExpect(status().isOk())
				.andExpect(model().attribute("items", contains(itemService.findAllByCategory(Category.SET).stream().toArray())))
				.andExpect(view().name("catalog"));
	}

	@Test
	void redirectsToCatalogWithInvalidCategory() throws Exception {
		mvc.perform(get("/catalog/{category}","Bett"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/catalog"))
				.andExpect(view().name("redirect:/catalog"));
	}

	@Test
	void returnsModelAndViewOnItemOverview() throws Exception {
		itemCatalog.save(sofa_grey);

		mvc.perform(get("/catalog/{category}/{itemId}", Category.COUCH, sofa_grey.getId()))
				.andExpect(status().isOk())
				.andExpect(model().attribute("item", is(sofa_grey)))
				.andExpect(model().attribute("variants", hasItem(sofa_grey)))
				.andExpect(view().name("itemView"));
	}

	@Test
	void redirectsToCatalogCategoryWithValidCategoryAndInvalidItem() throws Exception {
		mvc.perform(get("/catalog/{category}/{itemId}", Category.COUCH, sofa1_grey.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/catalog/" + Category.COUCH))
				.andExpect(view().name("redirect:/catalog/" + Category.COUCH));
	}

	@Test
	void redirectsToCatalogWithInvalidCategoryAndValidItem() throws Exception {
		itemCatalog.save(sofa1_grey);

		mvc.perform(get("/catalog/{category}/{itemId}","bett", sofa1_grey.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/catalog"))
				.andExpect(view().name("redirect:/catalog"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToCatalogOnItemOverviewAfterPieceDeletion() throws Exception {
		mvc.perform(post("/admin/supplier/{suppId}/items/delete/{itemId}", sofa_grey.getSupplier().getId(), sofa_grey.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/supplier/" + sofa_grey.getSupplier().getId() + "/items"))
				.andExpect(view().name("redirect:/admin/supplier/" + sofa_grey.getSupplier().getId() + "/items"));

		mvc.perform(get("/catalog/{category}/{itemId}",Category.SET, set.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/catalog/" + Category.SET))
				.andExpect(view().name("redirect:/catalog/" + Category.SET));
	}

}

