package furnitureshop.inventory;

import furnitureshop.FurnitureShop;
import furnitureshop.order.ContactInformation;
import furnitureshop.order.OrderService;
import furnitureshop.order.OrderStatus;
import furnitureshop.order.Pickup;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import furnitureshop.utils.Utils;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.Cart;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Streamable;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = FurnitureShop.class)
public class ItemServiceTests {

	@Autowired
	ItemCatalog itemCatalog;

	@Autowired
	SupplierRepository supplierRepository;

	@Autowired
	ItemService itemService;

	@Autowired
	OrderService orderService;

	@Autowired
	BusinessTime businessTime;

	Piece piece1, piece2, piece3, piece4;
	Set set1;

	Item item;

	@BeforeEach
	void setUp() {
		Utils.clearRepositories();

		final Supplier supplier = new Supplier("Supplier 1", 0);
		final Supplier supplier2 = new Supplier("Supplier 2", 0);
		final Supplier setSupplier = new Supplier("Set Supplier", 0);

		final List<Item> items = new ArrayList<>();

		piece1 = new Piece(1, "Tisch 1", Money.of(90, Currencies.EURO), new byte[0], "weiß",
				"", supplier, 30, Category.TABLE);
		piece2 = new Piece(2, "Sofa 1", Money.of(260, Currencies.EURO), new byte[0], "schwarz",
				"", supplier2, 50, Category.COUCH);
		piece3 = new Piece(2, "Sofa 2", Money.of(260, Currencies.EURO), new byte[0], "braun",
				"", supplier2, 80, Category.COUCH);
		piece4 = new Piece(3, "Stuhl 1", Money.of(60, Currencies.EURO), new byte[0], "schwarz",
				"", supplier, 5, Category.CHAIR);

		set1 = new Set(4, "Set 1", Money.of(300, Currencies.EURO), new byte[0], "black",
				"", setSupplier, Arrays.asList(piece3, piece4));

		item = new Piece(10, "Sofa 3", Money.of(10, Currencies.EURO), new byte[0], "rot",
				"", supplier, 80, Category.COUCH);

		supplierRepository.saveAll(Arrays.asList(supplier, supplier2, setSupplier));
		itemCatalog.saveAll(Arrays.asList(piece1, piece2, piece3, piece4, set1));

		// Init test Orders
		Cart cart = new Cart();
		ContactInformation info = new ContactInformation("testName", "testAdresse", "testEmail");
		LocalDateTime time = LocalDateTime.of(2020, 12, 21, 0, 0);

		cart.addOrUpdateItem(piece1, 1);  // 90 -> 1
		cart.addOrUpdateItem(piece4, 1);  // 60 -> 1
		cart.addOrUpdateItem(set1, 2);    // 0
		businessTime.forward(Duration.between(businessTime.getTime(), time));

		Pickup order = orderService.orderPickupItem(cart, info);
		order = (Pickup) orderService.findById(order.getId().getIdentifier()).orElse(null);
		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(0).getId(), OrderStatus.COMPLETED);
		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(1).getId(), OrderStatus.COMPLETED);
		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(2).getId(), OrderStatus.PAID);
		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(3).getId(), OrderStatus.CANCELLED);

		cart = new Cart();
		info = new ContactInformation("testName", "testAdresse", "testEmail");
		time = LocalDateTime.of(2020, 11, 17, 0, 0);

		cart.addOrUpdateItem(piece1, 1); // 90 -> 1
		cart.addOrUpdateItem(piece2, 2); // 260 -> 2
		cart.addOrUpdateItem(set1, 1); // 300 ( 56,25 -> 1, 243,75 -> 2 )
		businessTime.forward(Duration.between(businessTime.getTime(), time));

		order = orderService.orderPickupItem(cart, info);
		order = (Pickup) orderService.findById(order.getId().getIdentifier()).orElse(null);
		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(0).getId(), OrderStatus.COMPLETED);
		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(1).getId(), OrderStatus.CANCELLED);
		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(2).getId(), OrderStatus.COMPLETED);
		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(3).getId(), OrderStatus.COMPLETED);

		cart = new Cart();
		info = new ContactInformation("testName", "testAdresse", "testEmail");
		time = LocalDateTime.of(2020, 10, 12, 0, 0);

		cart.addOrUpdateItem(piece1, 2); // 90 -> 1
		cart.addOrUpdateItem(piece2, 1); // 260 -> 2
		cart.addOrUpdateItem(set1, 1); // 0
		businessTime.forward(Duration.between(businessTime.getTime(), time));

		order = orderService.orderPickupItem(cart, info);
		order = (Pickup) orderService.findById(order.getId().getIdentifier()).orElse(null);
		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(0).getId(), OrderStatus.PAID);
		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(1).getId(), OrderStatus.COMPLETED);
		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(2).getId(), OrderStatus.COMPLETED);
		orderService.changeItemEntryStatus(order, order.getOrderEntries().get(3).getId(), OrderStatus.OPEN);

		businessTime.reset();
	}

	@Test
	void testAddOrUpdateItemWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.addOrUpdateItem(null),
				"addOrUpdateItem() should throw an IllegalArgumentException if the item argument is invalid!"
		);
	}

	@Test
	void testAddOrUpdateItem() {
		assertTrue(itemService.findById(item.getId()).isEmpty(), "addOrUpdateItem() should add the Item first!");
		itemService.addOrUpdateItem(item);

		assertTrue(itemService.findById(item.getId()).isPresent(), "addOrUpdateItem() should add the Item!");
		item.setName("Test");

		itemService.addOrUpdateItem(item);
		final Optional<Item> item = itemService.findById(this.item.getId());

		assertTrue(item.isPresent(), "addOrUpdateItem() should update the Item!");
		assertEquals("Test", item.get().getName(), "addOrUpdateItem() should update the Item!");
	}

	@Test
	void testRemoveItemWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.removeItem(null),
				"removeItem() should throw an IllegalArgumentException if the item argument is invalid!"
		);
	}

	@Test
	void testRemoveItem() {
		assertFalse(itemService.removeItem(item), "removeItem() should not remove Item!");

		assertTrue(itemService.removeItem(piece1), "removeItem() should remove Item!");
		assertTrue(itemService.findById(piece1.getId()).isEmpty(), "removeItem() should remove Set!");
		assertTrue(itemService.findById(set1.getId()).isPresent(), "removeItem() should not remove Set!");

		assertTrue(itemService.removeItem(piece3), "removeItem() should remove Item!");
		assertTrue(itemService.findById(piece3.getId()).isEmpty(), "removeItem() should remove Set!");
		assertTrue(itemService.findById(set1.getId()).isEmpty(), "removeItem() should remove Set!");
	}

	@Test
	void testAnalyseProfitsWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.analyseProfits(null, businessTime.getTime().toLocalDate()),
				"analyseProfits() should throw an IllegalArgumentException if the date argument is invalid!"
		);

		assertThrows(IllegalArgumentException.class, () -> itemService.analyseProfits(businessTime.getTime().toLocalDate(), null),
				"analyseProfits() should throw an IllegalArgumentException if the date argument is invalid!"
		);
	}

	@Test
	void testAnalyseProfits() {
		List<StatisticEntry> profits = itemService.analyseProfits(
				LocalDate.of(2020, 12, 1),
				LocalDate.of(2020, 11, 1)
		);

		assertEquals(2L, profits.size(), "analyseProfits() should return the correct entries!");

		for (StatisticEntry entry : profits) {
			if (entry.getSupplier().getName().equals("Supplier 1")) {
				assertEquals(2, entry.getStatisticItemEntries().size(), "analyseProfits() should return the correct entries!");

				assertEquals(150.0, entry.getInitProfit().getNumber().doubleValue(), 0.01, "analyseProfits() should return the correct value!");
				assertEquals(146.25, entry.getCompareProfit().getNumber().doubleValue(), 0.01, "analyseProfits() should return the correct value!");
				assertEquals(3.75, entry.getDifference().getNumber().doubleValue(), 0.01, "analyseProfits() should return the correct value!");
			} else if (entry.getSupplier().getName().equals("Supplier 2")) {
				assertEquals(2, entry.getStatisticItemEntries().size(), "analyseProfits() should return the correct entries!");

				assertEquals(0, entry.getInitProfit().getNumber().doubleValue(), 0.01, "analyseProfits() should return the correct value!");
				assertEquals(503.75, entry.getCompareProfit().getNumber().doubleValue(), 0.01, "analyseProfits() should return the correct value!");
				assertEquals(-503.75, entry.getDifference().getNumber().doubleValue(), 0.01, "analyseProfits() should return the correct value!");
			} else {
				fail("Invalid State!");
			}
		}

		profits = itemService.analyseProfits(
				LocalDate.of(2020, 10, 1),
				LocalDate.of(2020, 10, 1)
		);

		assertEquals(2L, profits.size(), "analyseProfits() should return the correct entries!");

		for (StatisticEntry entry : profits) {
			if (entry.getSupplier().getName().equals("Supplier 1")) {
				assertEquals(2, entry.getStatisticItemEntries().size(), "analyseProfits() should return the correct entries!");

				assertEquals(90, entry.getInitProfit().getNumber().doubleValue(), 0.01, "analyseProfits() should return the correct value!");
				assertEquals(90, entry.getCompareProfit().getNumber().doubleValue(), 0.01, "analyseProfits() should return the correct value!");
				assertEquals(0, entry.getDifference().getNumber().doubleValue(), 0.01, "analyseProfits() should return the correct value!");
			} else if (entry.getSupplier().getName().equals("Supplier 2")) {
				assertEquals(2, entry.getStatisticItemEntries().size(), "analyseProfits() should return the correct entries!");

				assertEquals(260, entry.getInitProfit().getNumber().doubleValue(), 0.01, "analyseProfits() should return the correct value!");
				assertEquals(260, entry.getCompareProfit().getNumber().doubleValue(), 0.01, "analyseProfits() should return the correct value!");
				assertEquals(0, entry.getDifference().getNumber().doubleValue(), 0.01, "analyseProfits() should return the correct value!");
			} else {
				fail("Invalid State!");
			}
		}
	}

	@Test
	void testFindAll() {
		assertEquals(5L, itemService.findAll().stream().count(), "findAll() should find all Items!");
	}

	@Test
	void testFindBySupplierWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.findBySupplier(null),
				"findBySupplier() should throw an IllegalArgumentException if the supplier argument is invalid!"
		);
	}

	@Test
	void testFindBySupplier() {
		for (Supplier supplier : supplierRepository.findAll()) {
			final Streamable<Item> items = itemService.findBySupplier(supplier);

			switch (supplier.getName()) {
				case "Supplier 1": case "Supplier 2":
					assertEquals(2L, items.stream().count(), "findBySupplier() should find the correct items!");
					break;
				case "Set Supplier":
					assertEquals(1L, items.stream().count(), "findBySupplier() should find the correct items!");
					break;
				default:
					fail("Invalid State!");
					break;
			}
		}
	}

	@Test
	void testFindAllByIdWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.findById(null),
				"findById() should throw an IllegalArgumentException if the id argument is invalid!"
		);
	}

	@Test
	void testFindAllById() {
		final Optional<Item> result = itemService.findById(piece1.getId());

		assertTrue(result.isPresent(), "findById() should find the correct Item!");
		assertEquals(piece1, result.get(), "findById() should find the correct Item!");
	}

	@Test
	void testFindAllByGroupId() {
		assertEquals(0L, itemService.findAllByGroupId(0).stream().count(), "findAllByGroupId() should find the correct items!");
		assertEquals(1L, itemService.findAllByGroupId(1).stream().count(), "findAllByGroupId() should find the correct items!");
		assertEquals(2L, itemService.findAllByGroupId(2).stream().count(), "findAllByGroupId() should find the correct items!");
		assertEquals(1L, itemService.findAllByGroupId(3).stream().count(), "findAllByGroupId() should find the correct items!");
		assertEquals(1L, itemService.findAllByGroupId(4).stream().count(), "findAllByGroupId() should find the correct items!");
	}

	@Test
	void testFindAllByCategoryWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.findAllByCategory(null),
				"findAllByCategory() should throw an IllegalArgumentException if the category argument is invalid!"
		);
	}

	@Test
	void testFindAllByCategory() {
		assertEquals(2L, itemService.findAllByCategory(Category.COUCH).stream().count(), "findAllByCategory() should find the correct Items!");
		assertEquals(1L, itemService.findAllByCategory(Category.TABLE).stream().count(), "findAllByCategory() should find the correct Items!");
		assertEquals(1L, itemService.findAllByCategory(Category.CHAIR).stream().count(), "findAllByCategory() should find the correct Items!");
		assertEquals(1L, itemService.findAllByCategory(Category.SET).stream().count(), "findAllByCategory() should find the correct Items!");
	}

	@Test
	void testFindAllSetsByItemWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> itemService.findAllSetsByItem(null),
				"findAllSetsByItem() should throw an IllegalArgumentException if the item argument is invalid!"
		);
	}

	@Test
	void testFindAllSetsByItem() {
		assertEquals(0L, itemService.findAllSetsByItem(piece1).size(), "findAllSetsByItem() should find the correct Sets!");
		assertEquals(1L, itemService.findAllSetsByItem(piece3).size(), "findAllSetsByItem() should find the correct Sets!");
	}

	@Test
	void testGetFirstOrderDate() {
		assertEquals(LocalDate.of(2020, 10, 12), itemService.getFirstOrderDate());
	}

}
