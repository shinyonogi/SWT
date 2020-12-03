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
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = FurnitureShop.class)
public class ItemServiceTests {

	@Autowired
	ItemCatalog itemCatalog;

	@Autowired
	SupplierRepository supplierRepository;

	@Autowired
	ItemService itemservice;

	ProductIdentifier id;

	@BeforeEach
	void setUp() {
		itemCatalog.deleteAll();
		Iterator<Supplier> iterator = supplierRepository.findAll().iterator();
		Supplier supplier = iterator.next();

		Piece Stuhl1 = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "/resources/img/chair_2.jpg", "schwarz",
				"Stuhl 1 in schwarz.", supplier, 5, Category.CHAIR);

		supplier = iterator.next();

		Piece Sofa1_green = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "/resources/img/sofa_2_green.jpg", "grün",
				"Sofa 1 in grün.", supplier, 50, Category.COUCH);

		Piece Sofa1_red = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "/resources/img/sofa_2_red.jpg", "rot",
				"Sofa 1 in rot.", supplier, 50, Category.COUCH);

		Piece Sofa1_white = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "/resources/img/sofa_2_white.jpg", "weiß",
				"Sofa 1 in weiß.", supplier, 80, Category.COUCH);

		Piece Sofa1_grey = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "/resources/img/sofa_2_grey.jpg", "grau",
				"Sofa 1 in grau.", supplier, 80, Category.COUCH);

		supplier = iterator.next();

		Piece Tisch1 = new Piece(3, "Tisch 1", Money.of(89.99, Currencies.EURO), "/resources/img/table_2.jpg", "weiß",
				"Tisch 1 in weiß.", supplier, 30, Category.TABLE);

		List<Item> Set1_items = new ArrayList<>();
		Set1_items.add(Stuhl1);
		Set1_items.add(Sofa1_grey);

		Supplier setSupplier = new Supplier("Set Supplier", 0.05);
		supplierRepository.save(setSupplier);

		Set Set1 = new Set(4, "Set 1", Money.of(299.99, Currencies.EURO), "/resources/img/set_1.jpg", "black",
				"Set bestehend aus Sofa 1 und Stuhl 1.", setSupplier, Category.SET, Set1_items);

		id = Stuhl1.getId();

		itemCatalog.save(Stuhl1);
		itemCatalog.save(Sofa1_green);
		itemCatalog.save(Sofa1_grey);
		itemCatalog.save(Sofa1_red);
		itemCatalog.save(Sofa1_white);
		itemCatalog.save(Tisch1);
		itemCatalog.save(Set1);

	}

	@Test
	void findAllItemsBySizeTest() {
		assertEquals(itemservice.findAll().stream().count(), 7, "ItemService.findAll() should find all items!/Number of items in catalog is wrong!");
	}

	/*
	@Test
	void findAllByIdTest() {
		assertEquals(itemservice.findById(id).toString(), "");
	}
	*/

	@Test
	void findAllByGroupIdBySizeTest() {
		assertEquals(itemservice.findAllByGroupId(1).stream().count(), 1, "ItemService.findAll() should find all items by groupIDs!/Number of items in catalog is wrong!");
		assertEquals(itemservice.findAllByGroupId(2).stream().count(), 4, "ItemService.findAll() should find all items by groupIDs!/Number of items in catalog is wrong!");
		assertEquals(itemservice.findAllByGroupId(3).stream().count(), 1, "ItemService.findAll() should find all items by groupIDs!/Number of items in catalog is wrong!");
		assertEquals(itemservice.findAllByGroupId(4).stream().count(), 1, "ItemService.findAll() should find all items by groupIDs!/Number of items in catalog is wrong!");

	}

	@Test
	void findAllByCategoryBySizeTest() {
		assertEquals(itemservice.findAllByCategory(Category.COUCH).stream().count(), 4, "ItemService.findAll() should find all items by categories!/Number of items in catalog is wrong!");
		assertEquals(itemservice.findAllByCategory(Category.TABLE).stream().count(), 1, "ItemService.findAll() should find all items by categories!/Number of items in catalog is wrong!");
		assertEquals(itemservice.findAllByCategory(Category.CHAIR).stream().count(), 1, "ItemService.findAll() should find all items by categories!/Number of items in catalog is wrong!");
		assertEquals(itemservice.findAllByCategory(Category.SET).stream().count(), 1, "ItemService.findAll() should find all items by categories!/Number of items in catalog is wrong!");
	}
}
