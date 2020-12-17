package furnitureshop.inventory;

import furnitureshop.FurnitureShop;
import furnitureshop.order.ShopOrder;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

	@Autowired
	OrderManagement<ShopOrder> orderManagement;

	@Autowired
	UserAccountManagement userAccountManagement;

	final Supplier supplier = new Supplier("Supplier 1", 0.15);

	final Supplier setSupplier = new Supplier("Set Supplier", 0.05);

	Piece sofa_black = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), new byte[0], "black",
			"Sofa 1 in schwarz.", supplier, 80, Category.COUCH);

	Piece sofa1_black = new Piece(3, "Sofa 1", Money.of(259.99, Currencies.EURO), new byte[0], "black",
			"Sofa 1 in schwarz.", supplier, 80, Category.COUCH);

	Piece stuhl = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), new byte[0], "schwarz",
			"Stuhl 1 in schwarz.", supplier, 5, Category.CHAIR);

	Set set = new Set(4, "Set 1", Money.of(299.99, Currencies.EURO), new byte[0], "black",
			"Set bestehend aus Sofa 1 und Stuhl 1.", setSupplier, Arrays.asList(stuhl, sofa_black));

	@BeforeEach
	void setUp() {
		Optional<UserAccount> user = userAccountManagement.findByUsername("Dummy");

		if (user.isPresent()) {
			for (ShopOrder order : orderManagement.findBy(user.get())) {
				orderManagement.delete(order);
			}
		}

		itemCatalog.deleteAll();
		supplierRepository.deleteAll();

		final List<Supplier> suppliers = Arrays.asList(supplier, setSupplier);
		final List<Item> items = new ArrayList<>();

		Piece tisch = new Piece(3, "Tisch 1", Money.of(89.99, Currencies.EURO), new byte[0], "weiß",
				"Tisch 1 in weiß.", supplier, 30, Category.TABLE);

		items.add(set);
		items.add(tisch);
		items.add(stuhl);
		items.add(sofa_black);

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
		itemCatalog.save(sofa_black);

		mvc.perform(get("/catalog/{category}/{itemId}", Category.COUCH, sofa_black.getId()))
				.andExpect(status().isOk())
				.andExpect(model().attribute("item", is(sofa_black)))
				.andExpect(model().attribute("variants", hasItem(sofa_black)))
				.andExpect(view().name("itemView"));
	}

	@Test
	void redirectsToCatalogCategoryWithValidCategoryAndInvalidItem() throws Exception {
		mvc.perform(get("/catalog/{category}/{itemId}", Category.COUCH, sofa1_black.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/catalog/" + Category.COUCH))
				.andExpect(view().name("redirect:/catalog/" + Category.COUCH));
	}

	@Test
	void redirectsToCatalogWithInvalidCategoryAndValidItem() throws Exception {
		itemCatalog.save(sofa1_black);

		mvc.perform(get("/catalog/{category}/{itemId}","bett", sofa1_black.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/catalog"))
				.andExpect(view().name("redirect:/catalog"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToCatalogOnItemOverviewAfterPieceDeletion() throws Exception {
		mvc.perform(post("/admin/supplier/{suppId}/items/delete/{itemId}", sofa_black.getSupplier().getId(), sofa_black.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/supplier/" + sofa_black.getSupplier().getId() + "/items"))
				.andExpect(view().name("redirect:/admin/supplier/" + sofa_black.getSupplier().getId() + "/items"));

		mvc.perform(get("/catalog/{category}/{itemId}",Category.SET, set.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/catalog/" + Category.SET))
				.andExpect(view().name("redirect:/catalog/" + Category.SET));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewAddPiece() throws Exception {
		mvc.perform(get("/admin/supplier/{id}/items/add", supplier.getId()))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("itemForm"))
				.andExpect(model().attribute("suppId", supplier.getId()))
				.andExpect(model().attribute("categories", Category.values()))
				.andExpect(model().attribute("edit", false))
				.andExpect(view().name("supplierItemform"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToOverviewAfterPieceAddition() throws Exception {

		mvc.perform(post("/admin/supplier/{id}/items/add", supplier.getId())
				.param("groupId", "0")
				.param("weight", "50")
				.param("name", "Item Name")
				.param("picture", "picture")
				.param("variant", "Variante")
				.param("description", "Beschreibung")
				.param("price", "50.65")
				.param("category", "CHAIR")
				.param("items", ""))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%s/items", supplier.getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%s/items", supplier.getId())));
		assertEquals(5, itemService.findAll().stream().count());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSupplierOverviewOnPieceAddingWithWrongSupplier() throws Exception {
		Supplier supp = new Supplier("wrong", 0.05);
		mvc.perform(post("/admin/supplier/{id}/items/add", supp.getId())
				.param("groupId", "0")
				.param("weight", "50")
				.param("name", "Item Name")
				.param("picture", "picture")
				.param("variant", "Variante")
				.param("description", "Beschreibung")
				.param("price", "50.65")
				.param("category", "CHAIR")
				.param("items", ""))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
		assertEquals(4, itemService.findAll().stream().count());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSetAddOnPieceAddingWithSetSupplier() throws Exception {
		mvc.perform(post("/admin/supplier/{id}/items/add", setSupplier.getId())
				.param("groupId", "0")
				.param("weight", "50")
				.param("name", "Item Name")
				.param("picture", "picture")
				.param("variant", "Variante")
				.param("description", "Beschreibung")
				.param("price", "50.65")
				.param("category", "CHAIR")
				.param("items", ""))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%s/sets/add", setSupplier.getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%s/sets/add", setSupplier.getId())));
		assertEquals(4, itemService.findAll().stream().count());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToAddSetOnAddPieceWithSetSupplier() throws Exception {
		mvc.perform(get("/admin/supplier/{id}/items/add", setSupplier.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%s/sets/add", setSupplier.getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%s/sets/add", setSupplier.getId())));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToAddPieceOnAddSetWithoutSetSupplier() throws Exception {
		mvc.perform(get("/admin/supplier/{id}/sets/add", supplier.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%s/items/add", supplier.getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%s/items/add", supplier.getId())));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnModelAndViewAddSet() throws Exception {
		mvc.perform(get("/admin/supplier/{id}/sets/add", setSupplier.getId()))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("itemMap"))
				.andExpect(model().attribute("suppId", setSupplier.getId()))
				.andExpect(view().name("supplierSetitems"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSupplierOverviewOnNonexistentSupplier() throws Exception {
		Supplier supp = new Supplier("wrong", 0.05);
		mvc.perform(get("/admin/supplier/{id}/sets/add", supp.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewOnItemSelectionWhenAddingSetsWithSetSupplier() throws Exception {

		List<String> itemList = Arrays.asList(stuhl.getId().toString(), sofa_black.getId().toString());
		MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("itemList", itemList);

		mvc.perform(post("/admin/supplier/{id}/sets/add", setSupplier.getId()).params(itemMap))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("setForm"))
				//.andExpect(model().attribute("maxPrice", stuhl.getPrice().add(sofa_black.getPrice()).getNumber()))
				.andExpect(model().attribute("suppId", setSupplier.getId()))
				.andExpect(view().name("supplierSetform"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSupplierOverviewOnItemSelectionWhenAddingSetsWithWrongSupplier() throws Exception {
		Supplier supp = new Supplier("wrong", 0.05);
		List<String> itemList = Arrays.asList(stuhl.getId().toString(), sofa_black.getId().toString());
		MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("itemList", itemList);

		mvc.perform(post("/admin/supplier/{id}/sets/add", supp.getId()).params(itemMap))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToAddItemOnItemSelectionWhenAddingSetsWithoutSetSupplier() throws Exception {
		List<String> itemList = Arrays.asList(stuhl.getId().toString(), sofa_black.getId().toString());
		MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("itemList", itemList);

		mvc.perform(post("/admin/supplier/{id}/sets/add", supplier.getId()).params(itemMap))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%s/items", supplier.getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%s/items", supplier.getId())));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToOverviewOnSetAddingWithSetSupplier() throws Exception {
		List<String> itemList = Arrays.asList(stuhl.getId().toString(), sofa_black.getId().toString());
		MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("items", itemList);

		mvc.perform(post("/admin/supplier/{id}/sets/add/set", setSupplier.getId())
				.param("groupId", "0")
				.param("weight", "100")
				.param("name", "Set Name")
				.param("picture", "picture")
				.param("variant", "Variante")
				.param("description", "Beschreibung")
				.param("price", "99.99")
				.param("category", "SET")
				.params(itemMap))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%s/items", setSupplier.getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%s/items", setSupplier.getId())));
		assertEquals(5, itemService.findAll().stream().count());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToOverviewOnSetAddingWithNonexistentSupplier() throws Exception {
		Supplier supp = new Supplier("wrong", 0.05);
		List<String> itemList = Arrays.asList(stuhl.getId().toString(), sofa_black.getId().toString());
		MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("items", itemList);

		mvc.perform(post("/admin/supplier/{id}/sets/add/set", supp.getId())
				.param("groupId", "0")
				.param("weight", "100")
				.param("name", "Set Name")
				.param("picture", "picture")
				.param("variant", "Variante")
				.param("description", "Beschreibung")
				.param("price", "99.99")
				.param("category", "SET")
				.params(itemMap))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
		assertEquals(4, itemService.findAll().stream().count());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToOverviewOnSetAddingWithWrongSupplier() throws Exception {
		List<String> itemList = Arrays.asList(stuhl.getId().toString(), sofa_black.getId().toString());
		MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("items", itemList);

		mvc.perform(post("/admin/supplier/{id}/sets/add/set", supplier.getId())
				.param("groupId", "0")
				.param("weight", "100")
				.param("name", "Set Name")
				.param("picture", "picture")
				.param("variant", "Variante")
				.param("description", "Beschreibung")
				.param("price", "99.99")
				.param("category", "SET")
				.params(itemMap))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%s/items", supplier.getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%s/items", supplier.getId())));
		assertEquals(4, itemService.findAll().stream().count());
	}

}
