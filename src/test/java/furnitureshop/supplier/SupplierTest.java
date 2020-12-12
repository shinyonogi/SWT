package furnitureshop.supplier;

import org.junit.jupiter.api.Test;
import javax.persistence.Entity;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class SupplierTest {

	@Test
	void testSupplierIsEntity() {
		assertTrue(Supplier.class.isAnnotationPresent(Entity.class), "Supplier must have @Entity!");
	}
}
