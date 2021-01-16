package furnitureshop.lkw;

import org.javamoney.moneta.Money;
import org.salespointframework.core.Currencies;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import java.util.Optional;

/**
 * This Enum represents all available types of {@link LKW}s.
 * It stores information like the name, maximum weight, picture path, and price for charter or delivery.
 */
public enum LKWType {

	SMALL(
			"1,5t", 500, "smalllkw.jpg",
			Money.of(20.00, Currencies.EURO), Money.of(5.00, Currencies.EURO),
			"Ein kleiner LKW für alle, die mal schnell was umräumen wollen."
	),
	MEDIUM(
			"3,5t", 2000, "mediumlkw.jpg",
			Money.of(50.00, Currencies.EURO), Money.of(10.00, Currencies.EURO),
			"Ein mittelgroßer LKW für alle, die ihr Büro umverlegen wollen."
	),
	LARGE(
			"7,5t", 6000, "largelkw.jpg",
			Money.of(80.00, Currencies.EURO), Money.of(20.00, Currencies.EURO),
			"Ein großer LKW für alle, die eine große Lagerhalle leer räumen wollen."
	);

	// Displayname of the LKWType
	private final String name;

	// Maximum transport weight
	private final int weight;

	// Picturename of the LKWType
	private final String picture;

	// Price if an LKW of this Type is rent
	private final MonetaryAmount charterPrice;

	// Price for an delivery with an LKW of this Type
	private final MonetaryAmount delieveryPrice;

	//Short description of the type
	private final String description;

	/**
	 * Creates a new instance of an {@link LKWType}
	 *
	 * @param name           The displayname of the type
	 * @param weight         The maximum weight it can store
	 * @param picture        The relative path to the type picture
	 * @param charterPrice   The price if a customer wants to rent a {@link LKW} of this type
	 * @param delieveryPrice The price for a customer for a delivery
	 * @param description    The short description of the type
	 */
	LKWType(String name, int weight, String picture, MonetaryAmount charterPrice, MonetaryAmount delieveryPrice, String description) {
		this.name = name;
		this.weight = weight;
		this.picture = "/resources/img/lkw/" + picture;
		this.charterPrice = charterPrice;
		this.delieveryPrice = delieveryPrice;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public int getWeight() {
		return weight;
	}

	public String getPicture() {
		return picture;
	}

	public MonetaryAmount getCharterPrice() {
		return charterPrice;
	}

	public MonetaryAmount getDelieveryPrice() {
		return delieveryPrice;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Finds a {@link LKWType} by its case insensitivity Enum-Name or Displayname.
	 * Similar to {@link LKWType#valueOf(String)} but with no {@link Exception}.
	 *
	 * @param name The name of the type
	 *
	 * @return The {@link LKWType} with this name
	 *
	 * @throws IllegalArgumentException If the {@code name} is {@code null}
	 */
	public static Optional<LKWType> getByName(String name) {
		Assert.hasText(name, "Name must not be null");

		// Iterate over all Types and compare Enum Name and Displayname
		for (LKWType type : LKWType.values()) {
			if (type.name().equalsIgnoreCase(name) || type.getName().equalsIgnoreCase(name)) {
				return Optional.of(type);
			}
		}

		return Optional.empty();
	}

	/**
	 * Finds a {@link LKWType} by its weight. Used to find an {@link LKW} which can transport the weight.
	 * Returns the next biggest {@link LKWType} to fit the weight.
	 *
	 * @param weight The minimum weight of the {@link LKW}
	 *
	 * @return The {@link LKWType} with the weight
	 *
	 * @throws IllegalArgumentException If the {@code weight} is negative
	 */
	public static Optional<LKWType> getByWeight(int weight) {
		Assert.isTrue(weight >= 0, "Weight must be greater than 0");

		LKWType minType = null;

		// Iterate over all Types and finds the smalles type to fit the weight
		for (LKWType type : LKWType.values()) {
			if (type.weight >= weight && (minType == null || type.weight < minType.weight)) {
				minType = type;
			}
		}

		return Optional.ofNullable(minType);
	}

}