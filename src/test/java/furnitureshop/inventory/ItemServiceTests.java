package furnitureshop.inventory;

import furnitureshop.FurnitureShop;
import furnitureshop.order.OrderService;
import furnitureshop.order.ShopOrder;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Streamable;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = FurnitureShop.class)
public class ItemServiceTests {

	@Autowired
	ItemCatalog itemCatalog;

	@Autowired
	SupplierRepository supplierRepository;

	@Autowired
	ItemService itemService;

	@Autowired
	OrderManagement<ShopOrder> orderManagement;

	@Autowired
	OrderService orderService;

	@Autowired
	BusinessTime businessTime;

	Piece piece1, piece2, piece3, piece4;
	Set set1;

	Item item;

	@BeforeEach
	void setUp() {
		for (ShopOrder order : orderService.findAll()) {
			orderManagement.delete(order);
		}

		itemCatalog.deleteAll();
		supplierRepository.deleteAll();

		final Supplier supplier = new Supplier("Supplier 1", 0.15);
		final Supplier setSupplier = new Supplier("Set Supplier", 0.05);

		final List<Item> items = new ArrayList<>();

		piece1 = new Piece(1, "Tisch 1", Money.of(89.99, Currencies.EURO), new byte[0], "weiß",
				"", supplier, 30, Category.TABLE);
		piece2 = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), new byte[0], "schwarz",
				"", supplier, 50, Category.COUCH);
		piece3 = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), new byte[0], "braun",
				"", supplier, 80, Category.COUCH);
		piece4 = new Piece(3, "Stuhl 1", Money.of(59.99, Currencies.EURO), new byte[0], "schwarz",
				"", supplier, 5, Category.CHAIR);

		set1 = new Set(4, "Set 1", Money.of(299.99, Currencies.EURO), new byte[0], "black",
				"", setSupplier, Arrays.asList(piece3, piece4));

		item = new Piece(10, "Sofa 10", Money.of(9.99, Currencies.EURO), new byte[0], "rot",
				"", supplier, 80, Category.COUCH);

		supplierRepository.saveAll(Arrays.asList(supplier, setSupplier));
		itemCatalog.saveAll(Arrays.asList(piece1, piece2, piece3, piece4, set1));
	}

	@Test
	void testAddOrUpdateItemWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.addOrUpdateItem(null),
				"addOrUpdateItem() should throw an IllegalArgumentException if the item argument is invalid!"
		);
	}

	@Test
	void testAddOrUpdateItem() {
		assertTrue(itemService.findById(item.getId()).isEmpty(), "addOrUpdateItem() should add the Item first!");
		itemService.addOrUpdateItem(item);

		assertTrue(itemService.findById(item.getId()).isPresent(), "addOrUpdateItem() should add the Item!");
		item.setName("Test");

		itemService.addOrUpdateItem(item);
		final Optional<Item> item = itemService.findById(this.item.getId());

		assertTrue(item.isPresent(), "addOrUpdateItem() should update the Item!");
		assertEquals("Test", item.get().getName(), "addOrUpdateItem() should update the Item!");
	}

	@Test
	void testRemoveItemWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.removeItem(null),
				"removeItem() should throw an IllegalArgumentException if the item argument is invalid!"
		);
	}

	@Test
	void testRemoveItem() {
		assertFalse(itemService.removeItem(item), "removeItem() should not remove Item!");

		assertTrue(itemService.removeItem(piece1), "removeItem() should remove Item!");
		assertTrue(itemService.findById(piece1.getId()).isEmpty(), "removeItem() should remove Set!");
		assertTrue(itemService.findById(set1.getId()).isPresent(), "removeItem() should not remove Set!");

		assertTrue(itemService.removeItem(piece3), "removeItem() should remove Item!");
		assertTrue(itemService.findById(piece3.getId()).isEmpty(), "removeItem() should remove Set!");
		assertTrue(itemService.findById(set1.getId()).isEmpty(), "removeItem() should remove Set!");
	}

	@Test
	void testAnalyseProfitsWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.analyseProfits(null, businessTime.getTime().toLocalDate()),
				"analyseProfits() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> itemService.analyseProfits(businessTime.getTime().toLocalDate(), null),
				"analyseProfits() should throw an IllegalArgumentException if the date argument is invalid!"
		);
	}

	@Test
	void testAnalyseProfits() {
		//TODO Write Test
	}

	@Test
	void testFindAll() {
		assertEquals(5L, itemService.findAll().stream().count(), "findAll() should find all Items!");
	}

	@Test
	void testFindBySupplierWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.findBySupplier(null),
				"findBySupplier() should throw an IllegalArgumentException if the supplier argument is invalid!"
		);
	}

	@Test
	void testFindBySupplier() {
		for (Supplier supplier : supplierRepository.findAll()) {
			final Streamable<Item> items = itemService.findBySupplier(supplier);

			if (supplier.getName().equals("Supplier 1")) {
				assertEquals(4L, items.stream().count(), "findBySupplier() should find the correct items!");
			} else if (supplier.getName().equals("Set Supplier")) {
				assertEquals(1L, items.stream().count(), "findBySupplier() should find the correct items!");
			} else {
				fail("Invalid State!");
			}
		}
	}

	@Test
	void testFindAllByIdWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.findById(null),
				"findById() should throw an IllegalArgumentException if the id argument is invalid!"
		);
	}

	@Test
	void testFindAllById() {
		final Optional<Item> result = itemService.findById(piece1.getId());

		assertTrue(result.isPresent(), "findById() should find the correct Item!");
		assertEquals(piece1, result.get(), "findById() should find the correct Item!");
	}

	@Test
	void testFindAllByGroupId() {
		assertEquals(0L, itemService.findAllByGroupId(0).stream().count(), "findAllByGroupId() should find the correct items!");
		assertEquals(1L, itemService.findAllByGroupId(1).stream().count(), "findAllByGroupId() should find the correct items!");
		assertEquals(2L, itemService.findAllByGroupId(2).stream().count(), "findAllByGroupId() should find the correct items!");
		assertEquals(1L, itemService.findAllByGroupId(3).stream().count(), "findAllByGroupId() should find the correct items!");
		assertEquals(1L, itemService.findAllByGroupId(4).stream().count(), "findAllByGroupId() should find the correct items!");
	}

	@Test
	void testFindAllByCategoryWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.findAllByCategory(null),
				"findAllByCategory() should throw an IllegalArgumentException if the category argument is invalid!"
		);
	}

	@Test
	void testFindAllByCategory() {
		assertEquals(2L, itemService.findAllByCategory(Category.COUCH).stream().count(), "findAllByCategory() should find the correct Items!");
		assertEquals(1L, itemService.findAllByCategory(Category.TABLE).stream().count(), "findAllByCategory() should find the correct Items!");
		assertEquals(1L, itemService.findAllByCategory(Category.CHAIR).stream().count(), "findAllByCategory() should find the correct Items!");
		assertEquals(1L, itemService.findAllByCategory(Category.SET).stream().count(), "findAllByCategory() should find the correct Items!");
	}

	@Test
	void testFindAllSetsByItemWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.findAllSetsByItem(null),
				"findAllSetsByItem() should throw an IllegalArgumentException if the item argument is invalid!"
		);
	}

	@Test
	void testFindAllSetsByItem() {
		assertEquals(0L, itemService.findAllSetsByItem(piece1).size(), "findAllSetsByItem() should find the correct Sets!");
		assertEquals(1L, itemService.findAllSetsByItem(piece3).size(), "findAllSetsByItem() should find the correct Sets!");
	}

}
