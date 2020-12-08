package furnitureshop.inventory;

import furnitureshop.FurnitureShop;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.core.Currencies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * UnitTest for {@link ItemService}
 *
 * @author Shintaro Onogi
 */

@SpringBootTest
@ContextConfiguration(classes = FurnitureShop.class)
public class ItemServiceTests {

	@Autowired
	ItemCatalog itemCatalog;

	@Autowired
	SupplierRepository supplierRepository;

	@Autowired
	ItemService itemService;

	@BeforeEach
	void setUp() {
		itemCatalog.deleteAll();
		supplierRepository.deleteAll();

		final Supplier supplier = new Supplier("Supplier 1", 0.15);
		final Supplier setSupplier = new Supplier("Set Supplier", 0.05);

		final List<Supplier> suppliers = Arrays.asList(supplier, setSupplier);
		final List<Item> items = new ArrayList<>();

		items.add(new Piece(1, "Tisch 1", Money.of(89.99, Currencies.EURO), "table_2.jpg", "weiß",
				"Tisch 1 in weiß.", supplier, 30, Category.TABLE));
		items.add(new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "sofa_2_green.jpg", "grün",
				"Sofa 1 in grün.", supplier, 50, Category.COUCH));

		Piece sofa1_grey = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "sofa_2_grey.jpg", "grau",
				"Sofa 1 in grau.", supplier, 80, Category.COUCH);
		Piece stuhl1 = new Piece(3, "Stuhl 1", Money.of(59.99, Currencies.EURO), "chair_2.jpg", "schwarz",
				"Stuhl 1 in schwarz.", supplier, 5, Category.CHAIR);

		items.add(stuhl1);
		items.add(sofa1_grey);
		items.add(new Set(4, "Set 1", Money.of(299.99, Currencies.EURO), "set_1.jpg", "black",
				"Set bestehend aus Sofa 1 und Stuhl 1.", setSupplier, Category.SET, Arrays.asList(stuhl1, sofa1_grey))
		);

		supplierRepository.saveAll(suppliers);
		itemCatalog.saveAll(items);
	}

	/**
	 * Tests if the findAll() function returns the right amount of products
	 */

	@Test
	void findAllItemsBySizeTest() {
		assertEquals(5, itemService.findAll().stream().count(), "ItemService.findAll() should find all items!/Number of items in catalog is wrong!");
	}

	/**
	 * Tests if the findById() function returns the right product (Optional)
	 */

	@Test
	void findAllByIdTest() {
		ProductIdentifier id = itemCatalog.findAll().stream().findAny().get().getId();
		assertEquals(itemService.findById(id).toString(), "Optional[Tisch 1, " + id + ", EUR 89.99, handled in UNIT]");
	}

	/**
	 * Tests if the findAllByGroupId() function returns the right amount products
	 */

	@Test
	void findAllByGroupIdBySizeTest() {
		assertEquals(1, itemService.findAllByGroupId(1).stream().count(), "ItemService.findAll() should find all items by groupIDs!/Number of items in catalog is wrong!");
		assertEquals(2, itemService.findAllByGroupId(2).stream().count(), "ItemService.findAll() should find all items by groupIDs!/Number of items in catalog is wrong!");
		assertEquals(1, itemService.findAllByGroupId(3).stream().count(), "ItemService.findAll() should find all items by groupIDs!/Number of items in catalog is wrong!");
		assertEquals(1, itemService.findAllByGroupId(4).stream().count(), "ItemService.findAll() should find all items by groupIDs!/Number of items in catalog is wrong!");
	}

	/**
	 * Tests if the find findAllByCategory() function returns the right amount of products
	 */

	@Test
	void findAllByCategoryBySizeTest() {
		assertEquals(2, itemService.findAllByCategory(Category.COUCH).stream().count(), "ItemService.findAll() should find all items by categories!/Number of items in catalog is wrong!");
		assertEquals(1, itemService.findAllByCategory(Category.TABLE).stream().count(), "ItemService.findAll() should find all items by categories!/Number of items in catalog is wrong!");
		assertEquals(1, itemService.findAllByCategory(Category.CHAIR).stream().count(), "ItemService.findAll() should find all items by categories!/Number of items in catalog is wrong!");
		assertEquals(1, itemService.findAllByCategory(Category.SET).stream().count(), "ItemService.findAll() should find all items by categories!/Number of items in catalog is wrong!");
	}
}
