package furnitureshop.order;

import org.junit.jupiter.api.Test;
import org.salespointframework.order.Order;

import javax.persistence.Entity;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShopOrderTests {

	@Test
	void testShopOrderIsAbstract() {
		assertTrue(Modifier.isAbstract(ShopOrder.class.getModifiers()), "ShopOrder should be an abstract class!");
	}

	@Test
	void testShopOrderIsChild() {
		assertTrue(Order.class.isAssignableFrom(ShopOrder.class), "ShopOrder must extends Order!");
	}

	@Test
	void testShopOrderIsEntity() {
		assertTrue(ShopOrder.class.isAnnotationPresent(Entity.class), "ShopOrder must have @Entity!");
	}

}
