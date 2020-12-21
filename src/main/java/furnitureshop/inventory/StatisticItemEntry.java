package furnitureshop.inventory;

import com.mysema.commons.lang.Assert;

import javax.money.MonetaryAmount;

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
		Assert.notNull(amount, "Amount must not be null!");

		initProfit = initProfit.add(amount);
	}

	public void addCompareProfit(MonetaryAmount amount) {
		Assert.notNull(amount, "Amount must not be null!");

		compareProfit = compareProfit.add(amount);
	}

}