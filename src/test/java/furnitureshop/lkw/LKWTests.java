package furnitureshop.lkw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product;

import javax.persistence.Entity;

import static org.junit.jupiter.api.Assertions.*;

public class LKWTests {

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
		assertThrows(NullPointerException.class, () -> new LKW(null),
				"LKW() should throw an NullPointerException if the type argument is invalid!"
		);
	}

	@Test
	void testCorrectType() {
		for (int i = 0; i < LKWType.values().length; i++) {
			assertSame(LKWType.values()[i], lkws[i].getType(), "getType() should return the correct type!");
		}
	}

	@Test
	void testLKWIsProduct() {
		assertTrue(Product.class.isAssignableFrom(LKW.class), "LKW must extends Product!");
	}

	@Test
	void testLKWIsEntity() {
		assertTrue(LKW.class.isAnnotationPresent(Entity.class), "LKW must have @Entity!");
	}

}
