package furnitureshop.inventory;

import com.mysema.commons.lang.Assert;
import org.javamoney.moneta.Money;
import org.salespointframework.core.Currencies;
import org.salespointframework.core.DataInitializer;
import org.springframework.stereotype.Component;

@Component
public class ItemDataInitializer implements DataInitializer {

	private final ItemCatalog itemCatalog;

	ItemDataInitializer(ItemCatalog itemCatalog) {
		Assert.notNull(itemCatalog, "ItemCatalog must not be null!");
		this.itemCatalog = itemCatalog;
	}

	@Override
	public void initialize() {
		if (!itemCatalog.findAll().isEmpty()) {
			return;
		}

		itemCatalog.save(new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "", "blue",
				"Ein blauer Stuhl 1.", null, 5, Category.CHAIR));
		itemCatalog.save(new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "", "red",
				"Ein roter Stuhl 1.", null, 5, Category.CHAIR));
		itemCatalog.save(new Piece(2, "Stuhl 2", Money.of(45.87, Currencies.EURO), "", "blue",
				"Ein blauer Stuhl 2.", null, 5, Category.CHAIR));

		itemCatalog.save(new Piece(3, "Sofa 1", Money.of(139.99, Currencies.EURO), "", "mikrofaser grau",
				"Ein Sofa 1.", null, 50, Category.COUCH));
		itemCatalog.save(new Piece(3, "Sofa 1", Money.of(159.99, Currencies.EURO), "", "lederoptik",
				"Ein sofa 1.", null, 50, Category.COUCH));
		itemCatalog.save(new Piece(4, "Sofa 2", Money.of(259.99, Currencies.EURO), "", "mikrofaser blau",
				"Ein Sofa 2.", null, 80, Category.COUCH));

		itemCatalog.save(new Piece(5, "Tisch 1", Money.of(59.99, Currencies.EURO), "", "blue",
				"Ein blauer Tisch 1.", null, 30, Category.TABLE));
		itemCatalog.save(new Piece(5, "Tisch 1", Money.of(59.99, Currencies.EURO), "", "red",
				"Ein roter Tisch 1.", null, 30, Category.TABLE));
		itemCatalog.save(new Piece(5, "Tisch 1", Money.of(59.99, Currencies.EURO), "", "green",
				"Ein grüner Tisch 1.", null, 30, Category.TABLE));
	}
}
