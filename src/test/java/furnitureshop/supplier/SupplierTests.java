package furnitureshop.supplier;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;

import javax.persistence.Entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SupplierTests {

	Supplier supplier;

	@BeforeEach
	void setUp() {
		supplier = new Supplier("name", 1);
	}

	@Test
	void testSetSurcharge() {
		assertThrows(IllegalArgumentException.class, () -> supplier.setSurcharge(-1),
				"setSurcharge() should throw an IllegalArgumentException if the surcharge argument is invalid!"
		);

		supplier.setSurcharge(0.5);
		assertEquals(0.5, supplier.getSurcharge(), 1e-10, "setSurcharge() should set the correct value!");
	}

	@Test
	void testEquals() {
		assertEquals(supplier, supplier, "equals() should return the correct value!");
		assertNotEquals(supplier, new Object(), "equals() should return the correct value!");
		assertEquals(supplier, new Supplier("test", 0));
	}

	@Test
	void testSupplierConstructorWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> new Supplier(null, 1),
				"Supplier() should throw an IllegalArgumentException if the name argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Supplier("test", -1),
				"Supplier() should throw an IllegalArgumentException if the surcharge argument is negative!"
		);
	}

	@Test
	void testSupplierIsEntity() {
		assertTrue(Supplier.class.isAnnotationPresent(Entity.class), "Supplier must have @Entity!");
	}

}
