package furnitureshop.inventory;

import com.mysema.commons.lang.Assert;
import furnitureshop.supplier.Supplier;
import org.salespointframework.core.Currencies;

import javax.money.MonetaryAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticEntry {

	private final Supplier supplier;
	private final List<StatisticItemEntry> statisticItemEntryList;

	public StatisticEntry(Supplier supplier) {
		Assert.notNull(supplier, "Supplier must not be null");

		this.supplier = supplier;
		statisticItemEntryList = new ArrayList<>();
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public List<StatisticItemEntry> getStatisticItemEntryList() {
		return Collections.unmodifiableList(statisticItemEntryList);
	}

	public void addEntry(StatisticItemEntry entry) {
		for (StatisticItemEntry itemEntry : statisticItemEntryList) {
			if (itemEntry.equals(entry)) {
				itemEntry.addInitProfit(entry.getInitProfit());
				itemEntry.addCompareProfit(entry.getCompareProfit());
				return;
			}
		}
		statisticItemEntryList.add(entry);
	}

	public MonetaryAmount getInitProfit() {
		MonetaryAmount amount = Currencies.ZERO_EURO;

		for (StatisticItemEntry itemEntry : statisticItemEntryList) {
			amount = amount.add(itemEntry.getInitProfit());
		}

		return amount;
	}

	public MonetaryAmount getCompareProfit() {
		MonetaryAmount amount = Currencies.ZERO_EURO;

		for (StatisticItemEntry itemEntry : statisticItemEntryList) {
			amount = amount.add(itemEntry.getCompareProfit());
		}

		return amount;
	}

	public MonetaryAmount getDifference() {
		return getInitProfit().subtract(getCompareProfit());
	}
}
