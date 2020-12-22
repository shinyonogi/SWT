package furnitureshop.inventory;

import com.mysema.commons.lang.Assert;
import furnitureshop.supplier.Supplier;
import org.salespointframework.core.Currencies;

import javax.money.MonetaryAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class represents an {@link StatisticEntry}.
 * It's used to store information about the profit of an {@link Supplier}
 */
public class StatisticEntry {

	private final Supplier supplier;
	private final List<StatisticItemEntry> statisticItemEntries;

	/**
	 * Creates a new instance of an {@link StatisticEntry}
	 *
	 * @param supplier The {@link Supplier} from which the statistic is
	 *
	 * @throws IllegalArgumentException If any of the arguments is {@code null}
	 */
	public StatisticEntry(Supplier supplier) {
		Assert.notNull(supplier, "Supplier must not be null");

		this.supplier = supplier;
		this.statisticItemEntries = new ArrayList<>();
	}

	public Supplier getSupplier() {
		return supplier;
	}

	/**
	 * Sorts and returns the {@link StatisticItemEntry}s of the {@link Supplier}.
	 *
	 * @return A {@link List} with {@link StatisticItemEntry}
	 */
	public List<StatisticItemEntry> getStatisticItemEntries() {
		statisticItemEntries.sort(
				Comparator.comparing(StatisticItemEntry::getInitProfit, Comparator.reverseOrder())
						.thenComparing(s -> s.getItem().getName())
		);
		return Collections.unmodifiableList(statisticItemEntries);
	}

	/**
	 * Adds a {@link StatisticItemEntry} to the statistic.
	 * If the {@link Item} already exists, the values will be added.
	 *
	 * @param entry The adding {@link StatisticItemEntry} with profit information
	 *
	 * @throws IllegalArgumentException If {@code entry} is {@code null} or has a different {@link Supplier}
	 */
	public void addEntry(StatisticItemEntry entry) {
		Assert.notNull(entry, "StatisticItemEntry must not be null!");
		Assert.isTrue(supplier.equals(entry.getItem().getSupplier()), "Supplier must be the same!");

		for (StatisticItemEntry itemEntry : statisticItemEntries) {
			if (itemEntry.getItem().equals(entry.getItem())) {
				itemEntry.addInitProfit(entry.getInitProfit());
				itemEntry.addCompareProfit(entry.getCompareProfit());
				return;
			}
		}
		statisticItemEntries.add(entry);
	}

	/**
	 * Calculated the total init profit of the {@link Supplier}.
	 *
	 * @return The total init profit
	 */
	public MonetaryAmount getInitProfit() {
		MonetaryAmount amount = Currencies.ZERO_EURO;

		for (StatisticItemEntry itemEntry : statisticItemEntries) {
			amount = amount.add(itemEntry.getInitProfit());
		}

		return amount;
	}

	/**
	 * Calculated the total compare profit of the {@link Supplier}.
	 *
	 * @return The total compare profit
	 */
	public MonetaryAmount getCompareProfit() {
		MonetaryAmount amount = Currencies.ZERO_EURO;

		for (StatisticItemEntry itemEntry : statisticItemEntries) {
			amount = amount.add(itemEntry.getCompareProfit());
		}

		return amount;
	}

	/**
	 * Calculated the total difference between the profits of the {@link Supplier}.
	 *
	 * @return The total difference
	 */
	public MonetaryAmount getDifference() {
		return getInitProfit().subtract(getCompareProfit());
	}

}
