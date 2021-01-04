package furnitureshop.inventory;

import furnitureshop.FurnitureShop;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import furnitureshop.utils.Utils;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

	Piece sofa_black, sofa1_black, stuhl, tisch;
	Set set;
	Supplier supplier, setSupplier;

	MockMultipartFile multipartFile;

	@BeforeEach
	void setUp() {
		Utils.clearRepositories();

		supplier = new Supplier("Supplier 1", 0.15);
		setSupplier = new Supplier("Set Supplier", 0.05);

		sofa_black = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), new byte[0], "black",
				"Sofa 1 in schwarz.", supplier, 80, Category.COUCH);
		sofa1_black = new Piece(3, "Sofa 1", Money.of(259.99, Currencies.EURO), new byte[0], "black",
				"Sofa 1 in schwarz.", supplier, 80, Category.COUCH);
		stuhl = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), new byte[0], "schwarz",
				"Stuhl 1 in schwarz.", supplier, 5, Category.CHAIR);
		tisch = new Piece(3, "Tisch 1", Money.of(89.99, Currencies.EURO), new byte[0], "weiß",
				"Tisch 1 in weiß.", supplier, 30, Category.TABLE);

		set = new Set(4, "Set 1", Money.of(299.99, Currencies.EURO), new byte[0], "black",
				"Set bestehend aus Sofa 1 und Stuhl 1.", setSupplier, Arrays.asList(stuhl, sofa_black));

		supplierRepository.saveAll(Arrays.asList(supplier, setSupplier));
		itemCatalog.saveAll(Arrays.asList(set, tisch, stuhl, sofa_black));

		this.multipartFile = new MockMultipartFile("image", "test.png",
				"image/png", "Spring Framework".getBytes());
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
		mvc.perform(get("/catalog/{category}", "Bett"))
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

		mvc.perform(get("/catalog/{category}/{itemId}", "bett", sofa1_black.getId()))
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

		mvc.perform(get("/catalog/{category}/{itemId}", Category.SET, set.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/catalog/" + Category.SET))
				.andExpect(view().name("redirect:/catalog/" + Category.SET));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSupplierItemOverviewWithoutDeletingWithWrongSupplier() throws Exception {
		mvc.perform(post("/admin/supplier/{suppId}/items/delete/{itemId}", setSupplier.getId(), sofa_black.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%s/items", setSupplier.getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%s/items", setSupplier.getId())));

		assertEquals(4, itemService.findAll().stream().count());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSupplierItemOverviewWithoutDeletingWithNonexistentSupplier() throws Exception {
		final Supplier supp = new Supplier("wrong", 0.05);

		mvc.perform(post("/admin/supplier/{suppId}/items/delete/{itemId}", supp.getId(), sofa_black.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));

		assertEquals(4, itemService.findAll().stream().count());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToItemOverviewOnSuccessfulVisibilityToggle() throws Exception {
		mvc.perform(post("/admin/supplier/{suppId}/items/toggle/{itemId}", sofa_black.getSupplier().getId(), sofa_black.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%s/items", sofa_black.getSupplier().getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%s/items", sofa_black.getSupplier().getId())));

		assertFalse(itemCatalog.findById(sofa_black.getId()).orElse(null).isVisible());

		mvc.perform(post("/admin/supplier/{suppId}/items/toggle/{itemId}", sofa_black.getSupplier().getId(), sofa_black.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%s/items", sofa_black.getSupplier().getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%s/items", sofa_black.getSupplier().getId())));

		assertTrue(itemCatalog.findById(sofa_black.getId()).orElse(null).isVisible());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToItemOverviewOnSuccessfulVisibilityToggleWithWrongSupplier() throws Exception {
		mvc.perform(post("/admin/supplier/{suppId}/items/toggle/{itemId}", setSupplier.getId(), sofa_black.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%s/items", setSupplier.getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%s/items", setSupplier.getId())));

		assertTrue(itemCatalog.findById(sofa_black.getId()).orElse(null).isVisible());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToItemOverviewOnSuccessfulVisibilityToggleWithNonexistentSupplier() throws Exception {
		final Supplier supp = new Supplier("wrong", 0.05);

		mvc.perform(post("/admin/supplier/{suppId}/items/toggle/{itemId}", supp.getId(), sofa_black.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));

		assertTrue(itemCatalog.findById(sofa_black.getId()).orElse(null).isVisible());
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
	void redirectsToSupplierOverviewOnAddPieceWithNonexistentSupplier() throws Exception {
		final Supplier supp = new Supplier("wrong", 0.05);

		mvc.perform(get("/admin/supplier/{id}/items/add", supp.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToOverviewAfterPieceAddition() throws Exception {
		mvc.perform(multipart("/admin/supplier/{id}/items/add", supplier.getId())
				.file(multipartFile)
				.param("groupId", "0")
				.param("weight", "50")
				.param("name", "Item Name")
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
		final Supplier supp = new Supplier("wrong", 0.05);

		mvc.perform(multipart("/admin/supplier/{id}/items/add", supp.getId())
				.file(multipartFile)
				.param("groupId", "0")
				.param("weight", "50")
				.param("name", "Item Name")
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
		mvc.perform(multipart("/admin/supplier/{id}/items/add", setSupplier.getId())
				.file(multipartFile)
				.param("groupId", "0")
				.param("weight", "50")
				.param("name", "Item Name")
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
		final Supplier supp = new Supplier("wrong", 0.05);

		mvc.perform(get("/admin/supplier/{id}/sets/add", supp.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewOnItemSelectionWhenAddingSetsWithSetSupplier() throws Exception {
		final List<String> itemList = Arrays.asList(stuhl.getId().toString(), sofa_black.getId().toString());
		final MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("itemList", itemList);

		mvc.perform(post("/admin/supplier/{id}/sets/add", setSupplier.getId()).params(itemMap))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("setForm"))
				.andExpect(model().attributeExists("maxPrice"))
				.andExpect(model().attribute("suppId", setSupplier.getId()))
				.andExpect(view().name("supplierSetform"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToSupplierOverviewOnItemSelectionWhenAddingSetsWithWrongSupplier() throws Exception {
		final Supplier supp = new Supplier("wrong", 0.05);
		final List<String> itemList = Arrays.asList(stuhl.getId().toString(), sofa_black.getId().toString());
		final MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("itemList", itemList);

		mvc.perform(post("/admin/supplier/{id}/sets/add", supp.getId()).params(itemMap))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToAddItemOnItemSelectionWhenAddingSetsWithoutSetSupplier() throws Exception {
		final List<String> itemList = Arrays.asList(stuhl.getId().toString(), sofa_black.getId().toString());
		final MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("itemList", itemList);

		mvc.perform(post("/admin/supplier/{id}/sets/add", supplier.getId()).params(itemMap))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%s/items", supplier.getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%s/items", supplier.getId())));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToOverviewOnSetAddingWithSetSupplier() throws Exception {
		final List<String> itemList = Arrays.asList(stuhl.getId().toString(), sofa_black.getId().toString());
		final MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("items", itemList);

		mvc.perform(multipart("/admin/supplier/{id}/sets/add/set", setSupplier.getId())
				.file(multipartFile)
				.param("groupId", "0")
				.param("weight", "100")
				.param("name", "Set Name")
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
		final Supplier supp = new Supplier("wrong", 0.05);
		final List<String> itemList = Arrays.asList(stuhl.getId().toString(), sofa_black.getId().toString());
		final MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("items", itemList);

		mvc.perform(multipart("/admin/supplier/{id}/sets/add/set", supp.getId())
				.file(multipartFile)
				.param("groupId", "0")
				.param("weight", "100")
				.param("name", "Set Name")
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
		final List<String> itemList = Arrays.asList(stuhl.getId().toString(), sofa_black.getId().toString());
		final MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("items", itemList);

		mvc.perform(multipart("/admin/supplier/{id}/sets/add/set", supplier.getId())
				.file(multipartFile)
				.param("groupId", "0")
				.param("weight", "100")
				.param("name", "Set Name")
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

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewOnPieceEditWithCorrectSupplier() throws Exception {
		mvc.perform(get("/admin/supplier/{suppId}/items/edit/{itemId}", sofa_black.getSupplier().getId(), sofa_black.getId()))
				.andExpect(status().isOk())
				.andExpect(model().attributeDoesNotExist("items"))
				.andExpect(model().attributeDoesNotExist("maxPrice"))
				.andExpect(model().attributeExists("itemForm"))
				.andExpect(model().attribute("suppId", sofa_black.getSupplier().getId()))
				.andExpect(model().attribute("itemId", sofa_black.getId()))
				.andExpect(model().attribute("categories", Category.values()))
				.andExpect(model().attribute("edit", true))
				.andExpect(view().name("supplierItemform"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewOnSetEditWithCorrectSupplier() throws Exception {
		mvc.perform(get("/admin/supplier/{suppId}/items/edit/{itemId}", set.getSupplier().getId(), set.getId()))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("items"))
				.andExpect(model().attributeExists("maxPrice"))
				.andExpect(model().attributeExists("itemForm"))
				.andExpect(model().attribute("suppId", set.getSupplier().getId()))
				.andExpect(model().attribute("itemId", set.getId()))
				.andExpect(model().attribute("categories", Category.values()))
				.andExpect(model().attribute("edit", true))
				.andExpect(view().name("supplierItemform"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToOverviewOnItemEditWithNonexistentSupplier() throws Exception {
		final Supplier supp = new Supplier("wrong", 0.05);

		mvc.perform(get("/admin/supplier/{suppId}/items/edit/{itemId}", supp.getId(), sofa_black.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToOverviewOnItemEditWithWrongSupplier() throws Exception {
		mvc.perform(get("/admin/supplier/{suppId}/items/edit/{itemId}", setSupplier.getId(), sofa_black.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%d/items", setSupplier.getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%d/items", setSupplier.getId())));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsAfterSuccessfulEditOfItemWithCorrectSupplier() throws Exception {
		final List<String> itemList = new ArrayList<>();
		final MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("items", itemList);

		mvc.perform(multipart("/admin/supplier/{suppId}/items/edit/{itemId}", sofa_black.getSupplier().getId(), sofa_black.getId())
				.file(multipartFile)
				.param("groupId", String.valueOf(sofa_black.getGroupId()))
				.param("weight", String.valueOf(sofa_black.getWeight()))
				.param("name", "Sofa 50") // changed
				.param("variant", sofa_black.getVariant())
				.param("description", "New") // changed
				.param("price", "359.99") // changed
				.param("category", "COUCH")
				.params(itemMap))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%d/items", sofa1_black.getSupplier().getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%d/items", sofa1_black.getSupplier().getId())));

		assertEquals("Sofa 50", itemCatalog.findById(sofa_black.getId()).orElse(null).getName());
		assertEquals("New", itemCatalog.findById(sofa_black.getId()).orElse(null).getDescription());
		assertEquals(Money.of(359.99, Currencies.EURO), itemCatalog.findById(sofa_black.getId()).orElse(null).getSupplierPrice());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToItemOverviewWithoutEditOfItemWithNonexistentSupplier() throws Exception {
		final Supplier supp = new Supplier("wrong", 0.05);
		final List<String> itemList = new ArrayList<>();
		final MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("items", itemList);

		mvc.perform(multipart("/admin/supplier/{suppId}/items/edit/{itemId}", supp.getId(), sofa_black.getId())
				.file(multipartFile)
				.param("groupId", String.valueOf(sofa_black.getGroupId()))
				.param("weight", String.valueOf(sofa_black.getWeight()))
				.param("name", "Sofa 50") // changed
				.param("variant", sofa_black.getVariant())
				.param("description", "New") // changed
				.param("price", "359.99") // changed
				.param("category", "COUCH")
				.params(itemMap))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/suppliers"))
				.andExpect(view().name("redirect:/admin/suppliers"));

		assertEquals("Sofa 1", itemCatalog.findById(sofa_black.getId()).orElse(null).getName());
		assertEquals("Sofa 1 in schwarz.", itemCatalog.findById(sofa_black.getId()).orElse(null).getDescription());
		assertEquals(Money.of(259.99, Currencies.EURO), itemCatalog.findById(sofa_black.getId()).orElse(null).getSupplierPrice());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void redirectsToItemOverviewWithoutEditOfItemWithWrongSupplier() throws Exception {
		List<String> itemList = new ArrayList<>();
		MultiValueMap<String, String> itemMap = new LinkedMultiValueMap<>();
		itemMap.put("items", itemList);

		mvc.perform(multipart("/admin/supplier/{suppId}/items/edit/{itemId}", setSupplier.getId(), sofa_black.getId())
				.file(multipartFile)
				.param("groupId", String.valueOf(sofa_black.getGroupId()))
				.param("weight", String.valueOf(sofa_black.getWeight()))
				.param("name", "Sofa 50") // changed
				.param("variant", sofa_black.getVariant())
				.param("description", "New") // changed
				.param("price", "359.99") // changed
				.param("category", "COUCH")
				.params(itemMap))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(String.format("/admin/supplier/%d/items", setSupplier.getId())))
				.andExpect(view().name(String.format("redirect:/admin/supplier/%d/items", setSupplier.getId())));

		assertEquals("Sofa 1", itemCatalog.findById(sofa_black.getId()).orElse(null).getName());
		assertEquals("Sofa 1 in schwarz.", itemCatalog.findById(sofa_black.getId()).orElse(null).getDescription());
		assertEquals(Money.of(259.99, Currencies.EURO), itemCatalog.findById(sofa_black.getId()).orElse(null).getSupplierPrice());
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewOfMonthlyStatistic() throws Exception {
		mvc.perform(get("/admin/statistic"))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("statistic"))
				.andExpect(view().name("monthlyStatistic"));
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void returnsModelAndViewOfRequestMonthlyStatistic() throws Exception {
		final DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MMMM yyyy");
		final String dec = LocalDate.of(2020, 12, 1).format(pattern);
		final String nov = LocalDate.of(2020, 11, 1).format(pattern);

		mvc.perform(post("/admin/statistic")
				.param("init", dec).param("compare", nov))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("statistic"))
				.andExpect(view().name("monthlyStatistic"));
	}

	@Test
	void returnImageFromExistingItem() throws Exception {
		mvc.perform(get("/catalog/image/{id}", sofa_black.getId()))
				.andExpect(status().isOk())
				.andExpect(content().bytes(sofa_black.getImage()));
	}

	@Test
	void returnImageFromUnknownItem() throws Exception {
		mvc.perform(get("/catalog/image/{id}", "id"))
				.andExpect(status().is4xxClientError());
	}

}
