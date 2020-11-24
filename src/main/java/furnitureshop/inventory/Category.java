package furnitureshop.inventory;

import java.util.Optional;

public enum Category {

	CHAIR, TABLE, COUCH;

	public static Optional<Category> getByName(String name) {
		for (Category type : Category.values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return Optional.of(type);
			}
		}

		return Optional.empty();
	}

}