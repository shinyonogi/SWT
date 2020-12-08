package furnitureshop.order;

import furnitureshop.lkw.LKW;
import furnitureshop.lkw.LKWType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.UserAccount;

import javax.persistence.Entity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class LKWCharterTests {
	
	UserAccount account;
	ContactInformation info;
	LKW lkw;
	LocalDate date;

	@BeforeEach
	void setUp() {
		this.account = mock(UserAccount.class);
		this.info = new ContactInformation("name", "address", "email");
		this.lkw = new LKW(LKWType.SMALL);
		this.date = LocalDate.now();
	}
	
	@Test
	void testConstructorWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> new LKWCharter(null, info, lkw, date),
				"LKWCharter() should throw an IllegalArgumentException if the useraccount argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new LKWCharter(account, null, lkw, date),
				"LKWCharter() should throw an IllegalArgumentException if the contactinformation argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new LKWCharter(account, info, null, date),
				"LKWCharter() should throw an IllegalArgumentException if the lkw argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new LKWCharter(account, info, lkw, null),
				"LKWCharter() should throw an IllegalArgumentException if the date argument is invalid!"
		);
	}
	
	@Test
	void testLKWCharterIsChild() {
		assertTrue(ShopOrder.class.isAssignableFrom(LKWCharter.class), "LKWCharter must extends ShopOrder!");
	}

	@Test
	void testLKWCharterIsEntity() {
		assertTrue(LKWCharter.class.isAnnotationPresent(Entity.class), "LKWCharter must have @Entity!");
	}

}
