package furnitureshop.supplier;

import furnitureshop.FurnitureShop;
import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemCatalog;
import furnitureshop.order.ShopOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = FurnitureShop.class)
public class SupplierServiceTests {

	@Autowired
	SupplierRepository supplierRepository;

	@Autowired
	SupplierService supplierService;

	@Autowired
	ItemCatalog itemCatalog;

	@Autowired
	OrderManagement<ShopOrder> orderManagement;

	@Autowired
	UserAccountManagement userAccountManagement;

	Supplier testSupplier, defaultSupplier, defaultSupplier2;

	@BeforeEach
	void setUp() {
		for (ShopOrder order : orderManagement.findBy(userAccountManagement.findByUsername("Dummy").get())) {
			orderManagement.delete(order);
		}

		itemCatalog.deleteAll();
		supplierRepository.deleteAll();

		defaultSupplier = new Supplier("default1", 0);
		defaultSupplier2 = new Supplier("default2", 0.2);
		testSupplier = new Supplier("test", 1);

		supplierRepository.save(defaultSupplier);
		supplierRepository.save(defaultSupplier2);
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
			if (supplier.getName().contentEquals(testSupplier.getName())) {
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

		Supplier foundSupplier = supplierService.findByName(defaultSupplier.getName()).get();
		assertEquals(defaultSupplier.getId(), foundSupplier.getId(), "findByName() should return the correct supplier!");
	}

	@Test
	void testEquals() {
		assertEquals(defaultSupplier, defaultSupplier);
		assertNotEquals(defaultSupplier, defaultSupplier2);
	}

	@Test
	void testChangeSupplierSurcharge() {
		Long id = supplierRepository.findAll().stream().findAny().get().getId();
		assertTrue(supplierService.changeSupplierSurcharge(id, 0.3));
		assertFalse(supplierService.changeSupplierSurcharge(999999999, 0.1));
	}

	@Test
	void testIllegalArgumentExceptionWhenYouSurchargeNegative() {
		Long id = supplierRepository.findAll().stream().findAny().get().getId();
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			supplierService.changeSupplierSurcharge(id, -1);
		});
		String expectedMessage = "Surcharge must be greater equal to 0!";
		String actualMessage = exception.getMessage();

		assertEquals(actualMessage, expectedMessage);
	}
}
