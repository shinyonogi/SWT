package furnitureshop.order;

import furnitureshop.FurnitureShop;
import furnitureshop.inventory.Category;
import furnitureshop.inventory.ItemService;
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
import org.salespointframework.order.Cart;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = FurnitureShop.class)
public class OrderServiceTests {

	@Autowired
	OrderManagement<ShopOrder> orderManagement;

	@Autowired
	ItemService itemService;

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
		Iterator<Supplier> iterator = supplierRepository.findAll().iterator();
		Supplier supplier = iterator.next();

		Piece Stuhl1 = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "", "schwarz",
				"", supplier, 5, Category.CHAIR);
		Piece Sofa1_green = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "", "grün",
				"", supplier, 50, Category.COUCH);
		this.exampleCart = new Cart();
		exampleCart.addOrUpdateItem(Stuhl1, 1);
		exampleCart.addOrUpdateItem(Sofa1_green, 10);
	}

	@Test
	public void testFindById() {
		ContactInformation exampleContactInformation = new ContactInformation("testName", "testAdresse", "testEmail");

		Order order = orderService.orderPickupItem(exampleCart, exampleContactInformation).get();
		assertEquals(this.orderService.findById(order.getId().getIdentifier()).get(), order, "Die Methode findById" +
				"returned nicht die richtige Order für eine gegebene ID");
	}

	@Test
	public void testFindAll() {
		for (int i = 0; i < 10; i++) {
			ContactInformation exampleContactInformation = new ContactInformation("testName", "testAdresse", "testEmail");
			orderService.orderPickupItem(exampleCart, exampleContactInformation);
		}
		assertEquals(this.orderService.findAll().toList().size(), 10, "Es werden nicht alle Bestellungen gefunden und ausgegeben");

	}

	@Test
	public void testOrderDeliveryItem() {
		ContactInformation exampleContactInformation = new ContactInformation("testName", "testAdresse", "testEmail");
		LocalDate deliveryDate = lkwService.findNextAvailableDeliveryDate(this.businessTime.getTime().toLocalDate().plusDays(2), LKWType.SMALL);
		LKW lkw = lkwService.createDeliveryLKW(deliveryDate, LKWType.SMALL).get();

		Delivery order = orderService.orderDelieveryItem(exampleCart, exampleContactInformation).get();

		Delivery goalOrder = (Delivery) exampleCart.addItemsTo(new Delivery(this.orderService.getDummyUser().get(), exampleContactInformation,
				lkw, deliveryDate));
		assertEquals(order.getDeliveryDate(), goalOrder.getDeliveryDate());
		Iterator<OrderLine> goalOrderLineIterator = goalOrder.getOrderLines().get().iterator();
		Iterator<OrderLine> orderOrderLineIterator = order.getOrderLines().get().iterator();
		for (int i = 0; i < goalOrder.getOrderLines().get().count(); i++) {
			OrderLine goalOrderEntry = goalOrderLineIterator.next();
			OrderLine orderOrderEntry = orderOrderLineIterator.next();
			assertEquals(goalOrderEntry.getProductName(), orderOrderEntry.getProductName());
			assertEquals(goalOrderEntry.getPrice(), orderOrderEntry.getPrice());
			assertEquals(goalOrderEntry.getQuantity(), orderOrderEntry.getQuantity());
		}
		assertEquals(order.getContactInformation(), goalOrder.getContactInformation());
		assertEquals(order.getLkw(), goalOrder.getLkw());
	}
}
