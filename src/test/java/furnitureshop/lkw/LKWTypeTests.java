package furnitureshop.lkw;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LKWTypeTests {

	@Test
	void testLKWTypeEnum() {
		assertTrue(LKWType.class.isEnum(), "LKWType must be an Enum");
	}

	@Test
	void testGetByWeight() {
		assertThrows(IllegalArgumentException.class, () -> LKWType.getByWeight(-1),
				"LKWType.getByWeight() should throw an IllegalArgumentException if the weight argument is negative!"
		);

		Optional<LKWType> type = LKWType.getByWeight(10);

		assertTrue(type.isPresent(), "LKWType must exists");
		assertSame(LKWType.SMALL, type.get(), "LKWType must be the same");

		type = LKWType.getByWeight(LKWType.SMALL.getWeight());

		assertTrue(type.isPresent(), "LKWType must exists");
		assertSame(LKWType.SMALL, type.get(), "LKWType must be the same");

		type = LKWType.getByWeight(LKWType.SMALL.getWeight() + 5);

		assertTrue(type.isPresent(), "LKWType must exists");
		assertSame(LKWType.MEDIUM, type.get(), "LKWType must be the same");

		type = LKWType.getByWeight(LKWType.MEDIUM.getWeight());

		assertTrue(type.isPresent(), "LKWType must exists");
		assertSame(LKWType.MEDIUM, type.get(), "LKWType must be the same");

		type = LKWType.getByWeight(LKWType.LARGE.getWeight());

		assertTrue(type.isPresent(), "LKWType must exists");
		assertSame(LKWType.LARGE, type.get(), "LKWType must be the same");

		type = LKWType.getByWeight(LKWType.LARGE.getWeight() + 5);

		assertTrue(type.isEmpty(), "LKWType must not exists");
	}

	@Test
	void testGetByName() {
		assertThrows(IllegalArgumentException.class, () -> LKWType.getByName(null),
				"LKWType.getByName() should throw an IllegalArgumentException if the name argument is null!"
		);

		assertThrows(IllegalArgumentException.class, () -> LKWType.getByName(""),
				"LKWType.getByName() should throw an IllegalArgumentException if the name argument is empty!"
		);

		for (LKWType type : LKWType.values()) {
			Optional<LKWType> actual = LKWType.getByName(type.getName());

			assertTrue(actual.isPresent(), "LKWType must exists");
			assertSame(type, actual.get(), "LKWType must be the same");

			actual = LKWType.getByName(type.name());

			assertTrue(actual.isPresent(), "LKWType must exists");
			assertSame(type, actual.get(), "LKWType must be the same");
		}

		assertTrue(LKWType.getByName("unknown").isEmpty(), "LKWType should not exists");
	}

}
