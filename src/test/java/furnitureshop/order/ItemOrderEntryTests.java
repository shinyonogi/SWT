package furnitureshop.order;

import furnitureshop.inventory.Category;
import furnitureshop.inventory.Item;
import furnitureshop.inventory.Piece;
import furnitureshop.supplier.Supplier;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;

import javax.persistence.Entity;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemOrderEntryTests {

	Item item;

	ItemOrderEntry entry;

	@BeforeEach
	void setUp() {
		final Supplier supplier = new Supplier("test", 0.2);

		this.item = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "", "schwarz",
				"", supplier, 5, Category.CHAIR);

		entry = new ItemOrderEntry(item, OrderStatus.OPEN);
	}

	@Test
	void testConstructorWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> new ItemOrderEntry(null, OrderStatus.OPEN),
				"ItemOrderEntry() should throw an IllegalArgumentException if the item argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new ItemOrderEntry(item, null),
				"ItemOrderEntry() should throw an IllegalArgumentException if the status argument is invalid!"
		);
	}

	@Test
	void testSetStatusWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> entry.setStatus(null),
				"setStatus() should throw an IllegalArgumentException if the status argument is invalid!"
		);
	}

	@Test
	void testItemOrderEntryIsEntity() {
		assertTrue(ItemOrderEntry.class.isAnnotationPresent(Entity.class), "ContactInformation must have @Entity!");
	}

}
