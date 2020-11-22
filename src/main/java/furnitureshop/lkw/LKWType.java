package furnitureshop.lkw;

import org.javamoney.moneta.Money;
import org.salespointframework.core.Currencies;

import javax.money.MonetaryAmount;

public enum LKWType {

	SMALL(
			"3,5t", 2000, "smalllkw.jpg",
			Money.of(20, Currencies.EURO), Money.of(5, Currencies.EURO)
	),
	MEDIUM(
			"5,5t", 4000, "mediumlkw.jpg",
			Money.of(50, Currencies.EURO), Money.of(10, Currencies.EURO)
	),
	LARGE(
			"7,5t", 6000, "largelkw.jpg",
			Money.of(80, Currencies.EURO), Money.of(20, Currencies.EURO)
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

	LKWType(String name, int weight, String picture, MonetaryAmount charterPrice, MonetaryAmount delieveryPrice) {
		this.name = name;
		this.weight = weight;
		this.picture = picture;
		this.charterPrice = charterPrice;
		this.delieveryPrice = delieveryPrice;
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

	/**
	 * Finds an {@code LKWType} by its case insensitivity Enum-Name or Displayname.
	 * Similar to {@code LKWType.valueOf()} but with no Exception.
	 *
	 * @param name The name of the type
	 *
	 * @return The {@code LKWType} with this name or {@code null} if none was found
	 */
	public static LKWType getByName(String name) {
		for (LKWType type : LKWType.values()) {
			if (type.name().equalsIgnoreCase(name) || type.getName().equalsIgnoreCase(name)) {
				return type;
			}
		}

		return null;
	}

	/**
	 * Finds an {@code LKWType} by its weight. Used to find an {@code LKW} which can transport the weight.
	 * Returns the next biggest {@code LKWType} to fit the weight.
	 *
	 * @param weight The minimum weight of the {@code LKW}
	 *
	 * @return The {@code LKWType} with the weight or {@code null} if none was found
	 */
	public static LKWType getByWeight(int weight) {
		LKWType minType = null;

		for (LKWType type : LKWType.values()) {
			if (type.weight >= weight && (minType == null || type.weight < minType.weight)) {
				minType = type;
			}
		}

		return minType;
	}

}