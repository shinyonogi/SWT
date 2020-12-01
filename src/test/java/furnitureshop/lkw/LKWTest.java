package furnitureshop.lkw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product;

import javax.persistence.Entity;

import static org.junit.jupiter.api.Assertions.*;

public class LKWTest {

	LKW[] lkws;

	@BeforeEach
	void setUp() {
		this.lkws = new LKW[LKWType.values().length];

		for (int i = 0; i < LKWType.values().length; i++) {
			lkws[i] = new LKW(LKWType.values()[i]);
		}
	}

	@Test
	void testConstructorWithInvalidType() {
		try {
			new LKW(null);
			fail("LKW.LKW() should throw an IllegalArgumentException if the type argument is invalid!");
		} catch (NullPointerException | IllegalArgumentException ignored) {
			// IllegalArgumentException correctly thrown
		}
	}

	@Test
	void testCorrectType() {
		for (int i = 0; i < LKWType.values().length; i++) {
			assertSame(lkws[i].getType(), LKWType.values()[i], "LKW.getType() should return the correct type!");
		}
	}

	@Test
	void testLkwIsProduct() {
		assertTrue(Product.class.isAssignableFrom(LKW.class), "LKW must extends Product!");
	}

	@Test
	void testLkwIsEntity() {
		assertTrue(LKW.class.isAnnotationPresent(Entity.class), "LKW must have @Entity!");
	}

}
