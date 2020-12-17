package furnitureshop.order;

import furnitureshop.FurnitureShop;
import furnitureshop.inventory.Category;
import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemCatalog;
import furnitureshop.inventory.Piece;
import furnitureshop.lkw.LKW;
import furnitureshop.lkw.LKWService;
import furnitureshop.lkw.LKWType;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.salespointframework.order.*;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = FurnitureShop.class)
public class OrderServiceTests {

	@Autowired
	OrderManagement<ShopOrder> orderManagement;

	@Autowired
	ItemCatalog itemCatalog;

	@Autowired
	SupplierRepository supplierRepository;

	@Autowired
	OrderService orderService;

	@Autowired
	BusinessTime businessTime;

	@Autowired
	LKWService lkwService;

	Cart exampleCart;

	@BeforeEach
	void setUp() {
		for (ShopOrder order : orderManagement.findBy(orderService.getDummyUser().get())) {
			orderManagement.delete(order);
		}

		itemCatalog.deleteAll();
		supplierRepository.deleteAll();

		final Supplier supplier = new Supplier("test", 0.2);
		supplierRepository.save(supplier);

		Piece stuhl1 = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), new byte[0], "schwarz",
				"", supplier, 5, Category.CHAIR);
		Piece sofa1_green = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), new byte[0], "grün",
				"", supplier, 50, Category.COUCH);

		itemCatalog.save(stuhl1);
		itemCatalog.save(sofa1_green);

		this.exampleCart = new Cart();

		exampleCart.addOrUpdateItem(stuhl1, 1);
		exampleCart.addOrUpdateItem(sofa1_green, 10);
	}

	/**
	 * testFindById method
	 * Tests if u find the matching {@link Order} for a specific Id
	 *
	 * @throws Exception
	 */
	@Test
	public void testFindById() {
		ContactInformation exampleContactInformation = new ContactInformation("testName", "testAdresse", "testEmail");

		Order order = orderService.orderPickupItem(exampleCart, exampleContactInformation).get();
		assertEquals(this.orderService.findById(order.getId().getIdentifier()).get(), order, "Die Methode findById" +
				"returned nicht die richtige Order für eine gegebene ID");

		assertEquals(Optional.empty(), orderService.findById("thisisatestid"), "Es wird kein leeres Optional returned" +
				" wenn keine Order gefunden wird");

	}

	/**
	 * testFindAll method
	 * Tests if all {@link Order} that are saved in the repository are returned
	 *
	 * @throws Exception
	 */

	@Test
	public void testFindAll() {
		for (int i = 0; i < 10; i++) {
			ContactInformation exampleContactInformation = new ContactInformation("testName", "testAdresse", "testEmail");
			orderService.orderPickupItem(exampleCart, exampleContactInformation);
		}
		assertEquals(this.orderService.findAll().toList().size(), 10, "Es werden nicht alle Bestellungen gefunden und ausgegeben");

	}

	/**
	 * testOrderDeliveryItem method
	 * Tests if a {@link Delivery} is booked for the correct delivery date, if all properties of the Order are correctly
	 * mapped to the Delivery and that the correct Lkw is booked for a specific order
	 *
	 * @throws Exception
	 */
	@Test
	public void testOrderDeliveryItem() {
		ContactInformation exampleContactInformation = new ContactInformation("testName", "testAdresse", "testEmail");
		LocalDate deliveryDate = lkwService.findNextAvailableDeliveryDate(this.businessTime.getTime().toLocalDate().plusDays(2), LKWType.SMALL);
		LKW lkw = lkwService.createDeliveryLKW(deliveryDate, LKWType.SMALL).get();

		Delivery order = orderService.orderDelieveryItem(exampleCart, exampleContactInformation).get();

		Delivery goalOrder = (Delivery) exampleCart.addItemsTo(new Delivery(this.orderService.getDummyUser().get(), exampleContactInformation,
				lkw, deliveryDate));
		assertEquals(order.getDeliveryDate(), goalOrder.getDeliveryDate(),
				"Das Lieferdatum sollte übereinstimmen");
		Iterator<OrderLine> goalOrderLineIterator = goalOrder.getOrderLines().get().iterator();
		Iterator<OrderLine> orderOrderLineIterator = order.getOrderLines().get().iterator();
		for (int i = 0; i < goalOrder.getOrderLines().get().count(); i++) {
			OrderLine goalOrderEntry = goalOrderLineIterator.next();
			OrderLine orderOrderEntry = orderOrderLineIterator.next();
			assertEquals(goalOrderEntry.getProductName(), orderOrderEntry.getProductName(),
					"Der Produktname der OrderEntrys sollte übereinstimmen");
			assertEquals(goalOrderEntry.getPrice(), orderOrderEntry.getPrice(),
					"Der Preis der OrderEntrys sollte übereinstimmen");
			assertEquals(goalOrderEntry.getQuantity(), orderOrderEntry.getQuantity(),
					"Die Anzahl der Produkte in den OrderEntrys sollte übereinstimmen");
		}
		assertEquals(order.getContactInformation(), goalOrder.getContactInformation(),
				"Die Kontaktinformationen sollten übereinstimmen");
		assertEquals(order.getLkw(), goalOrder.getLkw(), "Die gemieteten LKWs sollten übereinstimmen");
	}

	/**
	 * testCancelLKW method
	 * Tests if a {@link LKW} is properly cancelled
	 *
	 * @throws Exception
	 */
	@Test
	public void testCancelLKW() {
		ContactInformation exampleContactInformation = new ContactInformation("testName", "testAdresse", "testEmail");
		LocalDate deliveryDate = lkwService.findNextAvailableDeliveryDate(this.businessTime.getTime().toLocalDate().plusDays(2), LKWType.SMALL);
		LKW lkw = lkwService.createDeliveryLKW(deliveryDate, LKWType.SMALL).get();
		Optional<LKWCharter> lkwCharterOptional = lkwService.createLKWOrder(lkw, deliveryDate, exampleContactInformation);
		assertTrue(lkwCharterOptional.isPresent(), String.format("Irgendetwas lief beim Mieten eines kleinen LKW am {0} schief", deliveryDate));
		LKWCharter lkwCharter = lkwCharterOptional.get();
		String lkwCharterIdentifier = lkwCharter.getId().getIdentifier();
		assertTrue(orderService.cancelLKW(lkwCharter), "Irgendetwas lief beim stornieren des LKWs schief");
		assertEquals(Optional.empty(), orderService.findById(lkwCharterIdentifier), "Der LKWCharter wurde nicht richtig gelöscht");
	}

	/**
	 * testRemoveItemFromOrders method
	 * Tests if {@link Item} are properly removed out of all existing {@link ItemOrder} and that resulting empty {@link ItemOrder}
	 * are deleted respectively
	 *
	 * @throws Exception
	 */
	@Test
	public void testRemoveItemFromOrders() {
		ContactInformation exampleContactInformation = new ContactInformation("testName", "testAdresse", "testEmail");

		List<CartItem> itemList = exampleCart.get().collect(Collectors.toList());

		Item item = (Item) itemList.get(0).getProduct();

		Optional<Pickup> orderOptional = orderService.orderPickupItem(exampleCart, exampleContactInformation);
		assertTrue(orderOptional.isPresent(), "Beim Bestellen der Beispiellieferung lief etwas schief");
		Pickup order = orderOptional.get();

		orderService.removeItemFromOrders(item);

		assertTrue(() -> {
			for (ItemOrderEntry entry : ((ItemOrder) (orderService.findById(order.getId().getIdentifier()).get())).getOrderEntries()) {
				if (entry.getItem() == item) {
					return false;
				}
			}
			return true;
		}, "Die Items wurden nicht richtig aus der Bestellung gelöscht");

		Item item2 = (Item) itemList.get(1).getProduct();
		assertNotEquals(item, item2, "Die Items sind gleich");

		orderService.removeItemFromOrders(item2);
		assertEquals(Optional.empty(), orderService.findById(order.getId().getIdentifier()), "Die leere Order wurde nicht entfernt");

	}
}
