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

	private final String name;
	private final int weight;
	private final String picture;
	private final MonetaryAmount charterPrice;
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
