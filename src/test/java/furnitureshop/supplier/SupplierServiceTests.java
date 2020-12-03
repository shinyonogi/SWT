package furnitureshop.supplier;

import furnitureshop.FurnitureShop;
import furnitureshop.inventory.ItemCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

	Supplier testSupplier, defaultSupplier, defaultSupplier2;

	@BeforeEach
	void setUp() {
		itemCatalog.deleteAll();
		supplierRepository.deleteAll();

		defaultSupplier = new Supplier("default1", 0);
		defaultSupplier2 = new Supplier("default2", 0.2);
		testSupplier = new Supplier("test", 1);

		supplierRepository.save(defaultSupplier);
		supplierRepository.save(defaultSupplier2);
	}

	@Test
	void testAddSupplier() {
		assertTrue(supplierService.addSupplier(testSupplier));

		final Optional<Supplier> optional = supplierRepository.findById(testSupplier.getId());
		assertTrue(optional.isPresent());

		Supplier storedSupplier = optional.get();
		assertEquals(testSupplier.getId(), storedSupplier.getId(), "supplier id mismatch");

		// duplicate test
		supplierService.addSupplier(testSupplier);
		boolean supplierFound = false;
		for (Supplier supplier : supplierRepository.findAll()) {
			if (supplier.getId() == testSupplier.getId()) {
				assertFalse(supplierFound, "testSupplier was found twice");
				supplierFound = true;
			}
		}
	}

	@Test
	void testDeleteSupplierById() {
		assertTrue(supplierService.deleteSupplierById(defaultSupplier.getId()));

		assertTrue(supplierRepository.findById(defaultSupplier.getId()).isEmpty(), "supplier was not deleted correctly");

		assertFalse(supplierService.deleteSupplierById(defaultSupplier.getId()));
	}
	/*
	@Test
	void testFindByName() {
		supplierRepository.save(testSupplier);
		Supplier foundSupplier = supplierService.findByName(testSupplier.getName()).get();
		assertEquals(foundSupplier.getId(), testSupplier.getId(), "supplier id mismatch");
	}*/

	@Test
	void testFindAll() {
		assertEquals(2, supplierRepository.count());
	}

}
