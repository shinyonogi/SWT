package furnitureshop.supplier;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import furnitureshop.inventory.ItemService;

@SpringBootTest
public class SupplierServiceTests {

	@Autowired
	SupplierRepository supplierRepository;
	
	@Autowired
	ItemService itemService;
	
	SupplierService supplierService;
	Supplier testSupplier = new Supplier("test", 1);
	
	@BeforeEach
	void setUp() {
		supplierService = new SupplierService(supplierRepository, itemService);
		//(SQL error) supplierRepository.deleteAll();
	}
	
	@Test
	void testAddSupplier() {
		supplierService.addSupplier(testSupplier);
		Supplier storedSupplier = supplierRepository.findById(testSupplier.getId()).get();
		assertEquals(storedSupplier.getId(), testSupplier.getId(), "supplier id mismatch");
		
		// duplicate test
		supplierService.addSupplier(testSupplier);
		boolean supplierFound = false;
		for(Supplier supplier : supplierRepository.findAll()) {
			if(supplier.getId() == testSupplier.getId()) {
				assertFalse(supplierFound, "testSupplier was found twice");
				supplierFound = true;
			}
		}
	}
	
	@Test
	void testDeleteSupplierById() {
		supplierRepository.save(testSupplier);
		supplierService.deleteSupplierById(testSupplier.getId());
		assertTrue(supplierRepository.findById(testSupplier.getId()).isEmpty(), "supplier was not deleted correctly");
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
		 assertEquals(supplierRepository.findAll(), supplierService.findAll());
	}
}
