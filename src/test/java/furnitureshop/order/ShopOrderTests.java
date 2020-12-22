package furnitureshop.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.Order;
import org.salespointframework.useraccount.UserAccount;

import javax.persistence.Entity;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ShopOrderTests {

	UserAccount account;
	ContactInformation info;

	ShopOrder order;

	@BeforeEach
	void setUp() {
		this.account = mock(UserAccount.class);
		this.info = new ContactInformation("name", "address", "email");

		this.order = new Pickup(account, info);
	}

	@Test
	void testSetCreated() {
		assertThrows(IllegalArgumentException.class, () -> order.setCreated(null),
				"setCreated() should throw an IllegalArgumentException if the created argument is invalid!"
		);
		final LocalDateTime now = LocalDateTime.now();
		order.setCreated(now);

		assertEquals(now, order.getCreated(), "setCreated() should set the correct value!");
	}

	@Test
	void testSetUpdated() {
		assertThrows(IllegalArgumentException.class, () -> order.setUpdated(null),
				"setUpdated() should throw an IllegalArgumentException if the created argument is invalid!"
		);
		final LocalDateTime now = LocalDateTime.now();
		order.setUpdated(now);

		assertEquals(now, order.getUpdated(), "setUpdated() should set the correct value!");
	}

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
