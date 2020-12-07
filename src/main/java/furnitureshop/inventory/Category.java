package furnitureshop.inventory;

import org.springframework.util.Assert;

import java.util.Optional;

/**
 * This Enum represents all available types of {@link Item}s.
 */
public enum Category {

	CHAIR, TABLE, COUCH, SET;

	/**
	 * Finds a {@link Category} by its case insensitivity Enum-Name.
	 *
	 * @param name The name of the type
	 *
	 * @return The {@link Category} with this name
	 *
	 * @throws IllegalArgumentException If the {@code name} is {@code null}
	 */
	public static Optional<Category> getByName(String name) {
		Assert.hasText(name, "Name must not be null");

		for (Category type : Category.values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return Optional.of(type);
			}
		}

		return Optional.empty();
	}

}