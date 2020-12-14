package furnitureshop.inventory;

import com.mysema.commons.lang.Assert;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import org.javamoney.moneta.Money;
import org.salespointframework.core.Currencies;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * This class initialize {@link Item}s and stores them into the {@link ItemCatalog}
 */
@Order(20)
@Component
public class ItemDataInitializer implements DataInitializer {

	private final ItemCatalog itemCatalog;
	private final SupplierRepository supplierRepository;

	/**
	 * Creates a new instance of an {@link ItemDataInitializer}
	 *
	 * @param itemCatalog        The {@link ItemCatalog} for all {@link Item}s
	 * @param supplierRepository The Repository of the suppliers
	 *
	 * @throws IllegalArgumentException If the {@code itemCatalog} or {@code supplierRepository} is {@code null}
	 */
	ItemDataInitializer(ItemCatalog itemCatalog, SupplierRepository supplierRepository) {
		Assert.notNull(itemCatalog, "ItemCatalog must not be null!");
		Assert.notNull(supplierRepository, "SupplierRepository must not be null!");

		this.itemCatalog = itemCatalog;
		this.supplierRepository = supplierRepository;
	}

	/**
	 * This method initializes {@link Item}s and saves them into the {@link ItemCatalog}, if no {@link Item} exists
	 */
	@Override
	public void initialize() {
		if (itemCatalog.count() > 0) {
			return;
		}

		final List<Item> items = new ArrayList<>();

		final Supplier muellerSupplier = findSupplierByName("Müller Möbel").orElse(null);
		final Supplier moebelSupplier = findSupplierByName("Möbelmeister").orElse(null);
		final Supplier stuehleSupplier = findSupplierByName("Herberts schicke Stühle").orElse(null);

		//add chairs
		items.add(new Piece(2, "Stuhl 2", Money.of(69.99, Currencies.EURO), "chair_2.jpg", "schwarz",
				"Stuhl 2 in schwarz.", stuehleSupplier, 5, Category.CHAIR));

		items.add(new Piece(3, "Stuhl 3", Money.of(49.99, Currencies.EURO), "chair_3.jpg", "schwarz",
				"Stuhl 3.", stuehleSupplier, 5, Category.CHAIR));

		items.add(new Piece(4, "Stuhl 4", Money.of(89.99, Currencies.EURO), "chair_4.jpg", "schwarz",
				"Stuhl 4.", stuehleSupplier, 5, Category.CHAIR));

		items.add(new Piece(5, "Stuhl 5", Money.of(149.99, Currencies.EURO), "chair_5.jpg", "schwarz",
				"Stuhl 5.", stuehleSupplier, 5, Category.CHAIR));

		items.add(new Piece(6, "Stuhl 6", Money.of(79.99, Currencies.EURO), "chair_6.jpg", "beige",
				"Stuhl 6.", stuehleSupplier, 5, Category.CHAIR));

		Piece chair_7 = new Piece(7, "Stuhl 7", Money.of(79.99, Currencies.EURO), "chair_7.jpg", "grau",
				"Stuhl 7.", stuehleSupplier, 5, Category.CHAIR);

		items.add(chair_7);

		items.add(new Piece(8, "Stuhl 8", Money.of(69.99, Currencies.EURO), "chair_8.jpg", "weiß",
				"Stuhl 8.", stuehleSupplier, 5, Category.CHAIR));

		items.add(new Piece(9, "Stuhl 9", Money.of(99.99, Currencies.EURO), "chair_9.jpg", "braun",
				"Stuhl 9.", stuehleSupplier, 5, Category.CHAIR));

		Piece chair_10_white = new Piece(10, "Stuhl 10", Money.of(39.99, Currencies.EURO), "chair_10_white.jpg", "weiß",
				"Stuhl 10 in weiß.", stuehleSupplier, 5, Category.CHAIR);

		Piece chair_10_black = new Piece(10, "Stuhl 10", Money.of(39.99, Currencies.EURO), "chair_10_black.jpg", "schwarz",
				"Stuhl 10 in schwarz.", stuehleSupplier, 5, Category.CHAIR);

		items.add(chair_10_white);
		items.add(chair_10_black);

		items.add(new Piece(11, "Stuhl 11", Money.of(79.99, Currencies.EURO), "chair_11.jpg", "navy",
				"Stuhl 11.", stuehleSupplier, 5, Category.CHAIR));

		//add couches
		items.add(new Piece(21, "Sofa 2", Money.of(1499.99, Currencies.EURO), "sofa_2_black.jpg", "schwarz",
				"Sofa 2 in schwarz.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(21, "Sofa 2", Money.of(1499.99, Currencies.EURO), "sofa_2_brown.jpg", "braun",
				"Sofa 2 in braun.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(22, "Sofa 3", Money.of(999.99, Currencies.EURO), "sofa_3.jpg", "braun",
				"Sofa 3.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(23, "Sofa 4", Money.of(799.99, Currencies.EURO), "sofa_4.jpg", "weiß",
				"Sofa 4.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(24, "Sofa 5", Money.of(559.99, Currencies.EURO), "sofa_5.jpg", "grau",
				"Sofa 5.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(25, "Sofa 6", Money.of(899.99, Currencies.EURO), "sofa_6.jpg", "braun",
				"Sofa 6.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(26, "Sofa 7", Money.of(2099.99, Currencies.EURO), "sofa_7.jpg", "weiß",
				"Sofa 7.", muellerSupplier, 50, Category.COUCH));

		Piece sofa_8 = new Piece(27, "Sofa 8", Money.of(299.99, Currencies.EURO), "sofa_8.jpg", "grau",
				"Sofa 8.", muellerSupplier, 50, Category.COUCH);

		items.add(sofa_8);

		//add tables
		items.add(new Piece(30, "Tisch 2", Money.of(109.99, Currencies.EURO), "table_2.jpg", "weiß",
				"Tisch 2 in weiß.", moebelSupplier, 30, Category.TABLE));

		items.add(new Piece(31, "Tisch 3", Money.of(309.99, Currencies.EURO), "table_3.jpg", "Eichenholz",
				"Tisch 3.", moebelSupplier, 30, Category.TABLE));

		items.add(new Piece(32, "Tisch 4", Money.of(259.99, Currencies.EURO), "table_4.jpg", "Glas",
				"Tisch 4.", moebelSupplier, 30, Category.TABLE));

		items.add(new Piece(33, "Tisch 5", Money.of(279.99, Currencies.EURO), "table_5.jpg", "Walnuss",
				"Tisch 5.", moebelSupplier, 30, Category.TABLE));

		items.add(new Piece(34, "Tisch 6", Money.of(69.99, Currencies.EURO), "table_6.jpg", "weiße Keramik",
				"Tisch 6.", moebelSupplier, 30, Category.TABLE));

		Piece table_7 = new Piece(35, "Tisch 7", Money.of(59.99, Currencies.EURO), "table_7.jpg", "weiß",
				"Tisch 7.", moebelSupplier, 30, Category.TABLE);

		items.add(table_7);

		items.add(new Piece(36, "Tisch 8", Money.of(229.99, Currencies.EURO), "table_8.jpg", "Eichenholz",
				"Tisch 8.", moebelSupplier, 30, Category.TABLE));

		Piece table_9 = new Piece(37, "Tisch 9", Money.of(69.99, Currencies.EURO), "table_9.jpg", "schwarz",
				"Tisch 9.", moebelSupplier, 30, Category.TABLE);

		items.add(table_9);

		items.add(new Piece(38, "Tisch 10", Money.of(289.99, Currencies.EURO), "table_10.jpg", "Eichenholz",
				"Tisch 10.", moebelSupplier, 30, Category.TABLE));

		//add sets
		final Supplier setSupplier = findSupplierByName("Set Supplier").orElse(null);

		items.add(new Set(40, "Set 1", Money.of(399.99, Currencies.EURO), "set_1.jpg", "schwarz",
				"Set bestehend aus Sofa 8 und Stuhl 7.", setSupplier, Arrays.asList(chair_7, sofa_8)));

		items.add(new Set(41, "Set 2", Money.of(109.99, Currencies.EURO), "set_2.jpg", "schwarz",
				"Set bestehend aus Tisch 9 und Stuhl 10.", setSupplier, Arrays.asList(chair_10_black, table_9)));

		items.add(new Set(42, "Set 3", Money.of(99.99, Currencies.EURO), "set_3.jpg", "weiß",
				"Set bestehend aus Sofa 10 und Tisch 7.", setSupplier, Arrays.asList(chair_10_white, table_7)));

		itemCatalog.saveAll(items);
	}

	/**
	 * Finds a {@link Supplier} by their name.
	 *
	 * @param name Name of the supplier
	 *
	 * @return Returns {@link Supplier} or nothing
	 */
	private Optional<Supplier> findSupplierByName(String name) {
		for (Supplier s : supplierRepository.findAll()) {
			if (s.getName().equalsIgnoreCase(name)) {
				return Optional.of(s);
			}
		}

		return Optional.empty();
	}

}
