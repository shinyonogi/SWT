package furnitureshop.supplier;

import furnitureshop.FurnitureShop;
import furnitureshop.inventory.Category;
import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemCatalog;
import furnitureshop.inventory.Piece;
import furnitureshop.order.OrderService;
import furnitureshop.order.ShopOrder;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.OrderManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = FurnitureShop.class)
public class SupplierServiceTests {

	@Autowired
	ItemCatalog itemCatalog;

	@Autowired
	SupplierRepository supplierRepository;

	@Autowired
	OrderManagement<ShopOrder> orderManagement;

	@Autowired
	SupplierService supplierService;

	@Autowired
	OrderService orderService;

	Supplier testSupplier, defaultSupplier, defaultSupplier2;
	Item item;

	@BeforeEach
	void setUp() {
		for (ShopOrder order : orderService.findAll()) {
			orderManagement.delete(order);
		}

		itemCatalog.deleteAll();
		supplierRepository.deleteAll();

		defaultSupplier = new Supplier("default1", 0);
		defaultSupplier2 = new Supplier("default2", 0.2);
		testSupplier = new Supplier("test", 1);

		item = new Piece(10, "Sofa 3", Money.of(10, Currencies.EURO), new byte[0], "rot",
				"", defaultSupplier, 80, Category.COUCH);

		supplierRepository.saveAll(Arrays.asList(defaultSupplier, defaultSupplier2));

		itemCatalog.save(item);
	}

	@Test
	void testAddSupplierWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> supplierService.addSupplier(null),
				"addSupplier() should throw an IllegalArgumentException if the supplier argument is invalid!"
		);
	}

	@Test
	void testAddSupplier() {
		assertTrue(supplierService.addSupplier(testSupplier), "addSupplier() should not find a duplicate supplier!");

		final Optional<Supplier> optional = supplierRepository.findById(testSupplier.getId());
		assertTrue(optional.isPresent(), "addSupplier() should store the supplier in the supplierRepository!");

		// duplicate test
		supplierService.addSupplier(testSupplier);
		boolean testSupplierFound = false;
		for (Supplier supplier : supplierRepository.findAll()) {
			if (supplier.getName().equals(testSupplier.getName())) {
				assertFalse(testSupplierFound, "addSupplier() should not store the same supplier twice!");
				testSupplierFound = true;
			}
		}
	}

	@Test
	void testDeleteSupplierById() {
		assertFalse(supplierService.deleteSupplierById(0), "deleteSupplier() should return false if the id argument is invalid!");

		assertTrue(supplierService.deleteSupplierById(defaultSupplier.getId()), "deleteSupplier() should find the supplier in the supplierRepository!");
		assertTrue(supplierRepository.findById(defaultSupplier.getId()).isEmpty(), "deleteSupplier() should remove the supplier from the supplierRepository!");
		assertFalse(supplierService.deleteSupplierById(defaultSupplier.getId()), "deleteSupplier() should not find the supplier in the supplierRepository anymore!");

		supplierService.deleteSupplierById(defaultSupplier.getId());
		for (Item item : itemCatalog.findAll()) {
			assertNotEquals(defaultSupplier.getId(), item.getSupplier().getId(), "deleteSupplier() should delete all items associated with the deleted supplier!");
		}
	}

	@Test
	void testFindByName() {
		assertThrows(IllegalArgumentException.class, () -> supplierService.findByName(null),
				"findByName() should throw an IllegalArgumentException if the name argument is invalid!"
		);

		final Supplier foundSupplier = supplierService.findByName(defaultSupplier.getName()).orElse(null);
		assertEquals(defaultSupplier.getId(), foundSupplier.getId(), "findByName() should return the correct supplier!");
	}

	@Test
	void testEquals() {
		assertEquals(defaultSupplier, defaultSupplier, "equals() should return the correct value!");
		assertNotEquals(defaultSupplier, defaultSupplier2, "equals() should return the correct value!");
	}

	@Test
	void testChangeSupplierSurcharge() {
		assertTrue(supplierService.changeSupplierSurcharge(defaultSupplier.getId(), 0.3), "changeSupplierSurcharge() should change the surcharge!");
		assertFalse(supplierService.changeSupplierSurcharge(-1, 0.1), "changeSupplierSurcharge() should not change the surcharge!");
	}

	@Test
	void testIllegalArgumentExceptionWhenYouSurchargeNegative() {
		assertThrows(IllegalArgumentException.class, () -> supplierService.changeSupplierSurcharge(defaultSupplier.getId(), -1),
				"changeSupplierSurcharge() should throw an IllegalArgumentException if the surcharge argument is invalid!"
		);
	}
}
