package furnitureshop.lkw;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LKWTypeTests {

	@Test
	void testGetByWeightWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> LKWType.getByWeight(-1),
				"getByWeight() should throw an IllegalArgumentException if the weight argument is negative!"
		);
	}

	@Test
	void testGetByWeight() {
		Optional<LKWType> type = LKWType.getByWeight(10);

		assertTrue(type.isPresent(), "getByWeight() should find a LKWType!");
		assertSame(LKWType.SMALL, type.get(), "getByWeight() should return the correct LKWType!");

		type = LKWType.getByWeight(LKWType.SMALL.getWeight());

		assertTrue(type.isPresent(), "getByWeight() should find a LKWType!");
		assertSame(LKWType.SMALL, type.get(), "getByWeight() should return the correct LKWType!");

		type = LKWType.getByWeight(LKWType.SMALL.getWeight() + 5);

		assertTrue(type.isPresent(), "getByWeight() should find a LKWType!");
		assertSame(LKWType.MEDIUM, type.get(), "getByWeight() should return the correct LKWType!");

		type = LKWType.getByWeight(LKWType.MEDIUM.getWeight());

		assertTrue(type.isPresent(), "getByWeight() should find a LKWType!");
		assertSame(LKWType.MEDIUM, type.get(), "getByWeight() should return the correct LKWType!");

		type = LKWType.getByWeight(LKWType.LARGE.getWeight());

		assertTrue(type.isPresent(), "getByWeight() should find a LKWType!");
		assertSame(LKWType.LARGE, type.get(), "getByWeight() should return the correct LKWType!");

		type = LKWType.getByWeight(LKWType.LARGE.getWeight() + 5);

		assertTrue(type.isEmpty(), "getByWeight() should not find a LKWType!");
	}

	@Test
	void testGetByNameWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> LKWType.getByName(null),
				"getByName() should throw an IllegalArgumentException if the name argument is null!"
		);

		assertThrows(IllegalArgumentException.class, () -> LKWType.getByName(""),
				"getByName() should throw an IllegalArgumentException if the name argument is empty!"
		);
	}

	@Test
	void testGetByName() {
		for (LKWType type : LKWType.values()) {
			Optional<LKWType> actual = LKWType.getByName(type.getName());

			assertTrue(actual.isPresent(), "getByName() should find a LKWType!");
			assertSame(type, actual.get(), "getByName() should return the correct LKWType!");

			actual = LKWType.getByName(type.name());

			assertTrue(actual.isPresent(), "getByName() should find a LKWType!");
			assertSame(type, actual.get(), "getByName() should return the correct LKWType!");
		}

		assertTrue(LKWType.getByName("unknown").isEmpty(), "getByName() should not find a LKWType!");
	}

	@Test
	void testLKWTypeIsEnum() {
		assertTrue(LKWType.class.isEnum(), "LKWType must be an Enum!");
	}

}
