package furnitureshop.inventory;

import com.mysema.commons.lang.Assert;

import javax.money.MonetaryAmount;

/**
 * This class represents an {@link StatisticItemEntry}.
 * It's used to store information about the profit of an {@link Item}
 */
public class StatisticItemEntry {

	private final Item item;
	private MonetaryAmount initProfit, compareProfit;

	/**
	 * Creates a new instance of an {@link StatisticItemEntry}
	 *
	 * @param item          The {@link Item} from which the statistic is
	 * @param initProfit    The init {@link MonetaryAmount} profit of the {@link Item}
	 * @param compareProfit The compare {@link MonetaryAmount} profit of the {@link Item}
	 *
	 * @throws IllegalArgumentException If any of the arguments is {@code null}
	 */
	public StatisticItemEntry(Item item, MonetaryAmount initProfit, MonetaryAmount compareProfit) {
		Assert.notNull(item, "Item must not be null!");
		Assert.notNull(initProfit, "InitProfit must not be null!");
		Assert.notNull(compareProfit, "CompareProfit must not be null!");

		this.item = item;
		this.initProfit = initProfit;
		this.compareProfit = compareProfit;
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

	public MonetaryAmount getDifference() {
		return initProfit.subtract(compareProfit);
	}

	/**
	 * Adds additional init profit to the {@link Item}
	 *
	 * @param amount The additional init profit
	 *
	 * @throws IllegalArgumentException If any of the arguments is {@code null}
	 */
	public void addInitProfit(MonetaryAmount amount) {
		Assert.notNull(amount, "Amount must not be null!");

		initProfit = initProfit.add(amount);
	}

	/**
	 * Adds additional compare profit to the {@link Item}
	 *
	 * @param amount The additional compare profit
	 *
	 * @throws IllegalArgumentException If any of the arguments is {@code null}
	 */
	public void addCompareProfit(MonetaryAmount amount) {
		Assert.notNull(amount, "Amount must not be null!");

		compareProfit = compareProfit.add(amount);
	}

}