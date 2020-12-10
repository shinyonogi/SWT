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

		Piece stuhl1 = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "chair_2.jpg", "schwarz",
				"Stuhl 1 in schwarz.", muellerSupplier, 5, Category.CHAIR);

		items.add(stuhl1);

		items.add(new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "sofa_2_green.jpg", "grün",
				"Sofa 1 in grün.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "sofa_2_red.jpg", "rot",
				"Sofa 1 in rot.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "sofa_2_white.jpg", "weiß",
				"Sofa 1 in weiß.", muellerSupplier, 80, Category.COUCH));

		Piece sofa1_grey = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "sofa_2_grey.jpg", "grau",
				"Sofa 1 in grau.", muellerSupplier, 80, Category.COUCH);

		items.add(sofa1_grey);

		items.add(new Piece(3, "Tisch 1", Money.of(89.99, Currencies.EURO), "table_2.jpg", "weiß",
				"Tisch 1 in weiß.", moebelSupplier, 30, Category.TABLE));

		final Supplier setSupplier = findSupplierByName("Set Supplier").orElse(null);

		items.add(new Set(4, "Set 1", Money.of(299.99, Currencies.EURO), "set_1.jpg", "black",
				"Set bestehend aus Sofa 1 und Stuhl 1.", setSupplier, Category.SET, Arrays.asList(stuhl1, sofa1_grey)));

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