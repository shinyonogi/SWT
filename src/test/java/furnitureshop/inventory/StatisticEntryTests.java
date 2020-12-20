package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;

import static org.junit.jupiter.api.Assertions.*;

public class StatisticEntryTests {

	Supplier supplier1;
	Item item1, item2;

	StatisticEntry statisticEntry;
	StatisticItemEntry itemEntry1, itemEntry2, itemEntry3;

	@BeforeEach
	void setUp() {
		this.supplier1 = new Supplier("test", 0);

		this.item1 = new Piece(0, "piece1", Money.of(10, Currencies.EURO),
				new byte[0], "", "", supplier1, 10, Category.CHAIR);
		this.item2 = new Piece(0, "piece2", Money.of(20, Currencies.EURO),
				new byte[0], "", "", supplier1, 20, Category.CHAIR);

		this.statisticEntry = new StatisticEntry(supplier1);

		this.itemEntry1 = new StatisticItemEntry(item1, Money.of(10, Currencies.EURO), Money.of(20, Currencies.EURO));
		this.itemEntry2 = new StatisticItemEntry(item1, Money.of(10, Currencies.EURO), Money.of(20, Currencies.EURO));
		this.itemEntry3 = new StatisticItemEntry(item2, Money.of(20, Currencies.EURO), Money.of(20, Currencies.EURO));
	}

	@Test
	void testAddInitProfit() {
		assertThrows(IllegalArgumentException.class, () -> itemEntry1.addInitProfit(null),
				"addInitProfit() should throw an IllegalArgumentException if the amount argument is invalid!"
		);
		itemEntry1.addInitProfit(Money.of(10, Currencies.EURO));

		assertEquals(Money.of(20, Currencies.EURO), itemEntry1.getInitProfit(), "addInitProfit() should set the correct value!");
	}

	@Test
	void testAddCompareProfit() {
		assertThrows(IllegalArgumentException.class, () -> itemEntry1.addCompareProfit(null),
				"addCompareProfit() should throw an IllegalArgumentException if the amount argument is invalid!"
		);
		itemEntry1.addCompareProfit(Money.of(10, Currencies.EURO));

		assertEquals(Money.of(30, Currencies.EURO), itemEntry1.getCompareProfit(), "addCompareProfit() should set the correct value!");
	}

	@Test
	void testAddEntry() {
		assertThrows(IllegalArgumentException.class, () -> statisticEntry.addEntry(null),
				"addEntry() should throw an IllegalArgumentException if the entry argument is invalid!"
		);

		statisticEntry.addEntry(itemEntry1);
		assertEquals(1, statisticEntry.getStatisticItemEntryList().size(), "addEntry() should add the correct value!");
		assertEquals(10, statisticEntry.getInitProfit().getNumber().doubleValue(), 1e-3, "addEntry() should add the correct value!");
		assertEquals(20, statisticEntry.getCompareProfit().getNumber().doubleValue(), 1e-3,"addEntry() should add the correct value!");

		statisticEntry.addEntry(itemEntry2);
		assertEquals(1, statisticEntry.getStatisticItemEntryList().size(), "addEntry() should add the correct value!");
		assertEquals(20, statisticEntry.getInitProfit().getNumber().doubleValue(), 1e-3, "addEntry() should add the correct value!");
		assertEquals(40, statisticEntry.getCompareProfit().getNumber().doubleValue(), 1e-3,"addEntry() should add the correct value!");

		statisticEntry.addEntry(itemEntry3);
		assertEquals(2, statisticEntry.getStatisticItemEntryList().size(), "addEntry() should add the correct value!");
		assertEquals(40, statisticEntry.getInitProfit().getNumber().doubleValue(), 1e-3, "addEntry() should add the correct value!");
		assertEquals(60, statisticEntry.getCompareProfit().getNumber().doubleValue(), 1e-3,"addEntry() should add the correct value!");
	}

	@Test
	void testStatisticEntryConstructorWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> new StatisticEntry(null),
				"StatisticEntry() should throw an IllegalArgumentException if the supplier argument is invalid!"
		);
	}

	@Test
	void testStatisticItemEntryConstructorWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> new StatisticItemEntry(null, Money.of(10, Currencies.EURO), Money.of(20, Currencies.EURO)),
				"StatisticItemEntry() should throw an IllegalArgumentException if the item argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new StatisticItemEntry(item1, null, Money.of(20, Currencies.EURO)),
				"StatisticItemEntry() should throw an IllegalArgumentException if the init argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new StatisticItemEntry(item1, Money.of(10, Currencies.EURO), null),
				"StatisticItemEntry() should throw an IllegalArgumentException if the compare argument is invalid!"
		);
	}

	@Test
	void testStatisticItemEntryEquals() {
		assertEquals(itemEntry1, itemEntry1);
		assertNotEquals(itemEntry3, itemEntry1);
		assertNotEquals(item2, itemEntry1);
	}

}
