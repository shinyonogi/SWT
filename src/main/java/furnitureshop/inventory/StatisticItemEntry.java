package furnitureshop.inventory;


import com.mysema.commons.lang.Assert;

import javax.money.MonetaryAmount;
import java.util.Objects;

public class StatisticItemEntry {
	private final Item item;
	private MonetaryAmount initProfit, compareProfit;

	public StatisticItemEntry(Item item, MonetaryAmount initProfit, MonetaryAmount compareProfit) {
		Assert.notNull(item, "Item must not be null!");
		Assert.notNull(initProfit, "InitProfit must not be null!");
		Assert.notNull(compareProfit, "CompareProfit must not be null!");

		this.item = item;
		this.initProfit = initProfit;
		this.compareProfit = compareProfit;
	}

	public MonetaryAmount getDifference() {
		return initProfit.subtract(compareProfit);
	}

	public Item getItem() {
		return item;
	}

	public MonetaryAmount getInitProfit() {
		return initProfit;
	}

	public MonetaryAmount getCompareProfit() {
		return compareProfit;
	}

	public void addInitProfit(MonetaryAmount amount) {
		initProfit = initProfit.add(amount);
	}

	public void addCompareProfit(MonetaryAmount amount) {
		compareProfit = compareProfit.add(amount);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof StatisticItemEntry)) {
			return false;
		}

		StatisticItemEntry that = (StatisticItemEntry) o;
		return item.equals(that.item);
	}

	@Override
	public int hashCode() {
		return Objects.hash(item);
	}
}