package furnitureshop.order;

import furnitureshop.inventory.Category;
import furnitureshop.inventory.Item;
import furnitureshop.inventory.Piece;
import furnitureshop.inventory.Set;
import furnitureshop.lkw.LKW;
import furnitureshop.lkw.LKWType;
import furnitureshop.supplier.Supplier;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ItemOrderTests {

	UserAccount account;
	ContactInformation info;
	LKW lkw;
	LocalDate date;

	ItemOrder order;

	Item item1, item2, set;

	@BeforeEach
	void setUp() {
		this.account = mock(UserAccount.class);
		this.info = new ContactInformation("name", "address", "email");
		this.lkw = new LKW(LKWType.SMALL);
		this.date = LocalDate.now();

		this.order = new Delivery(account, info, lkw, date);

		final Supplier supplier = new Supplier("test", 0);

		this.item1 = new Piece(1, "Stuhl 1", Money.of(60, Currencies.EURO), new byte[0], "schwarz",
				"", supplier, 5, Category.CHAIR);
		this.item2 = new Piece(1, "Stuhl 2", Money.of(50, Currencies.EURO), new byte[0], "weiß",
				"", supplier, 5, Category.CHAIR);
		this.set = new Set(2, "Set 1", Money.of(100, Currencies.EURO), new byte[0], "bunt",
				"", supplier, Arrays.asList(item1, item2));
	}

	@Test
	void testGetRefund() {
		order.addOrderLine(item1, Quantity.of(1));
		order.addOrderLine(item2, Quantity.of(1));

		order.getOrderEntriesByItem(item1).get(0).setStatus(OrderStatus.CANCELLED);
		assertEquals(item1.getPrice(), order.getRefund(), "getRefund() should return the correct value");

		order.getOrderEntriesByItem(item2).get(0).setStatus(OrderStatus.CANCELLED);
		assertEquals(item1.getPrice().add(item2.getPrice()), order.getRefund(), "getRefund() should return the correct value");
	}

	@Test
	void testGetMissingPayment() {
		order.addOrderLine(item1, Quantity.of(1));
		order.addOrderLine(item2, Quantity.of(1));

		assertEquals(item1.getPrice().add(item2.getPrice()), order.getMissingPayment(), "getMissingPayment() should return the correct value");

		order.getOrderEntriesByItem(item2).get(0).setStatus(OrderStatus.PAID);
		assertEquals(item1.getPrice(), order.getMissingPayment(), "getMissingPayment() should return the correct value");

		order.getOrderEntriesByItem(item2).get(0).setStatus(OrderStatus.CANCELLED);
		assertEquals(item1.getPrice(), order.getMissingPayment(), "getMissingPayment() should return the correct value");
	}

	@Test
	void testGetCancelFee() {
		order.addOrderLine(item1, Quantity.of(1));

		order.changeStatus(0, OrderStatus.STORED);
		assertEquals(Currencies.ZERO_EURO, order.getCancelFee(), "getCancelFee() should return the correct value");

		order.changeStatus(0, OrderStatus.CANCELLED);
		assertEquals(item1.getPrice().multiply(0.2), order.getCancelFee(), "getCancelFee() should return the correct value");

		order.changeStatus(0, OrderStatus.OPEN);
		order.changeStatus(0, OrderStatus.CANCELLED);
		assertEquals(Currencies.ZERO_EURO, order.getCancelFee(), "getCancelFee() should return the correct value");
	}

	@Test
	void testGetItemTotal() {
		order.addOrderLine(item1, Quantity.of(1));
		assertEquals(item1.getPrice(), order.getItemTotal(), "getItemTotal() should return the correct value");

		order.addOrderLine(item2, Quantity.of(1));
		assertEquals(item1.getPrice().add(item2.getPrice()), order.getItemTotal(), "getItemTotal() should return the correct value");
	}

	@Test
	void testGetTotal() {
		order.addOrderLine(item1, Quantity.of(1));
		order.addOrderLine(item2, Quantity.of(1));
		assertEquals(item1.getPrice().add(item2.getPrice()).add(lkw.getType().getDelieveryPrice()), order.getTotal(), "getTotal() should return the correct value");

		order.changeStatus(0, OrderStatus.CANCELLED);
		assertEquals(item2.getPrice().add(lkw.getType().getDelieveryPrice()), order.getTotal(), "getTotal() should return the correct value");

		order.changeStatus(0, OrderStatus.STORED);
		order.changeStatus(0, OrderStatus.CANCELLED);
		assertEquals(item1.getPrice().multiply(0.2).add(item2.getPrice()).add(lkw.getType().getDelieveryPrice()), order.getTotal(), "getTotal() should return the correct value");
	}

	@Test
	void testRemoveEntry() {
		order.addOrderLine(item1, Quantity.of(1));
		assertTrue(order.removeEntry(0), "removeEntry() should have a valid EntryID");

		order.addOrderLine(item1, Quantity.of(1));
		assertFalse(order.removeEntry(1), "removeEntry() should not have a valid EntryID");
	}

	@Test
	void testChangeStatus() {
		order.addOrderLine(item1, Quantity.of(1));

		assertTrue(order.changeStatus(0, OrderStatus.PAID), "changeStatus() should have a valid EntryID");
		assertTrue(order.changeStatus(0, OrderStatus.CANCELLED), "changeStatus() should have a valid EntryID");
		assertFalse(order.getOrderEntriesByItem(item1).get(0).hasCancelFee(), "changeStatus() shouldn't change cancel fee");

		assertTrue(order.changeStatus(0, OrderStatus.STORED), "changeStatus() should have a valid EntryID");
		assertTrue(order.changeStatus(0, OrderStatus.CANCELLED), "changeStatus() should have a valid EntryID");
		assertTrue(order.getOrderEntriesByItem(item1).get(0).hasCancelFee(), "changeStatus() should change cancel fee");

		assertFalse(order.changeStatus(1, OrderStatus.CANCELLED), "changeStatus() should not have a valid EntryID");
	}

	@Test
	void testGetOrderEntriesByItem() {
		order.addOrderLine(item1, Quantity.of(1));
		assertEquals(order.getOrderEntriesByItem(item1), order.getOrderEntries(), "getOrderEntriesByItem() should return the right orderentry");
	}

	@Test
	void testGetProfits() {
		order.addOrderLine(item1, Quantity.of(2));
		order.addOrderLine(set, Quantity.of(1));

		Map<Item, MonetaryAmount> profits = order.getProfits();
		assertEquals(0, profits.size(), "getProfits() should return the correct values");

		order.changeStatus(0, OrderStatus.COMPLETED);
		profits = order.getProfits();
		assertEquals(1, profits.size(), "getProfits() should return the correct values");
		assertEquals(60, profits.get(item1).getNumber().doubleValue(), 1e-10, "getProfits() should return the correct values");

		final ItemOrderEntry setEntry = order.getOrderEntriesByItem(set).get(0);
		setEntry.setStatus(OrderStatus.COMPLETED);
		profits = order.getProfits();
		assertEquals(2, profits.size(), "getProfits() should return the correct values");
		assertEquals(114.545, profits.get(item1).getNumber().doubleValue(), 1e-3, "getProfits() should return the correct values");
		assertEquals(45.455, profits.get(item2).getNumber().doubleValue(), 1e-3, "getProfits() should return the correct values");
	}

	@Test
	void testConstructorWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> new Pickup(null, info),
				"Pickup() should throw an IllegalArgumentException if the useraccount argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Pickup(account, null),
				"Pickup() should throw an IllegalArgumentException if the contactinformation argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Delivery(null, info, lkw, date),
				"Delivery() should throw an IllegalArgumentException if the useraccount argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Delivery(account, null, lkw, date),
				"Delivery() should throw an IllegalArgumentException if the contactinformation argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Delivery(account, info, null, date),
				"Delivery() should throw an IllegalArgumentException if the lkw argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Delivery(account, info, lkw, null),
				"Delivery() should throw an IllegalArgumentException if the date argument is invalid!"
		);
	}

	@Test
	void testItemOrderEntries() {
		order.addOrderLine(item1, Quantity.of(1));

		assertEquals(1, order.getOrderEntries().size(), "getOrderEntries() must contain 1 element");

		final ItemOrderEntry entry = order.getOrderEntries().get(0);
		assertEquals(item1, entry.getItem(), "getOrderEntries() must contain the correct Item");
		assertEquals(OrderStatus.OPEN, entry.getStatus(), "getOrderEntries() must contain the correct itemstatus");

		entry.setStatus(OrderStatus.CANCELLED);
		assertEquals(OrderStatus.CANCELLED, entry.getStatus(), "getOrderEntries() must contain the correct itemstatus");

		order.addOrderLine(item2, Quantity.of(2));

		assertEquals(3, order.getOrderEntries().size(), "getOrderEntries() must contain 3 elements");
	}

	@Test
	void testItemOrderIsAbstract() {
		assertTrue(Modifier.isAbstract(ItemOrder.class.getModifiers()), "ItemOrder should be an abstract class!");
	}

	@Test
	void testItemOrderIsChild() {
		assertTrue(ShopOrder.class.isAssignableFrom(ItemOrder.class), "ItemOrder must extends ShopOrder!");

		assertTrue(ItemOrder.class.isAssignableFrom(Pickup.class), "Pickup must extends ItemOrder!");
		assertTrue(ItemOrder.class.isAssignableFrom(Delivery.class), "Delivery must extends ItemOrder!");
	}

	@Test
	void testItemOrderIsEntity() {
		assertTrue(ItemOrder.class.isAnnotationPresent(Entity.class), "ItemOrder must have @Entity!");
		assertTrue(Pickup.class.isAnnotationPresent(Entity.class), "Pickup must have @Entity!");
		assertTrue(Delivery.class.isAnnotationPresent(Entity.class), "Delivery must have @Entity!");
	}

}
