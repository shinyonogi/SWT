package furnitureshop.lkw;

import org.javamoney.moneta.Money;
import org.salespointframework.core.Currencies;

import javax.money.MonetaryAmount;

public enum LKWType {

	SMALL(3500, Money.of(20, Currencies.EURO), Money.of(5, Currencies.EURO)),
	MEDIUM(5500, Money.of(50, Currencies.EURO), Money.of(10, Currencies.EURO)),
	LARGE(7500, Money.of(80, Currencies.EURO), Money.of(20, Currencies.EURO));

	private final int weight;
	private final MonetaryAmount charterPrice;
	private final MonetaryAmount delieveryPrice;

	LKWType(int weight, MonetaryAmount charterPrice, MonetaryAmount delieveryPrice) {
		this.weight = weight;
		this.charterPrice = charterPrice;
		this.delieveryPrice = delieveryPrice;
	}

	public int getWeight() {
		return weight;
	}

	public MonetaryAmount getCharterPrice() {
		return charterPrice;
	}

	public MonetaryAmount getDelieveryPrice() {
		return delieveryPrice;
	}

}
