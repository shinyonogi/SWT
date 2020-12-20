package furnitureshop.supplier;

import org.junit.jupiter.api.Test;

import javax.persistence.Entity;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SupplierTests {

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
