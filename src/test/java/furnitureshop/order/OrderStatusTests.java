package furnitureshop.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderStatusTests {

	@Test
	void testOrderStatusIsEnum() {
		assertTrue(OrderStatus.class.isEnum(), "OrderStatus must be an Enum!");
	}

}
