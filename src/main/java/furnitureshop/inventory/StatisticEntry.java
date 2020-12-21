package furnitureshop.inventory;

import com.mysema.commons.lang.Assert;
import furnitureshop.supplier.Supplier;
import org.salespointframework.core.Currencies;

import javax.money.MonetaryAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatisticEntry {

	private final Supplier supplier;
	private final List<StatisticItemEntry> statisticItemEntries;

	public StatisticEntry(Supplier supplier) {
		Assert.notNull(supplier, "Supplier must not be null");

		this.supplier = supplier;
		this.statisticItemEntries = new ArrayList<>();
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public List<StatisticItemEntry> getStatisticItemEntries() {
		statisticItemEntries.sort(
				Comparator.comparing(StatisticItemEntry::getInitProfit, Comparator.reverseOrder())
						.thenComparing(s -> s.getItem().getName())
		);
		return Collections.unmodifiableList(statisticItemEntries);
	}

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

	public MonetaryAmount getInitProfit() {
		MonetaryAmount amount = Currencies.ZERO_EURO;

		for (StatisticItemEntry itemEntry : statisticItemEntries) {
			amount = amount.add(itemEntry.getInitProfit());
		}

		return amount;
	}

	public MonetaryAmount getCompareProfit() {
		MonetaryAmount amount = Currencies.ZERO_EURO;

		for (StatisticItemEntry itemEntry : statisticItemEntries) {
			amount = amount.add(itemEntry.getCompareProfit());
		}

		return amount;
	}

	public MonetaryAmount getDifference() {
		return getInitProfit().subtract(getCompareProfit());
	}

}
