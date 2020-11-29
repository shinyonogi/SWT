package furnitureshop.inventory;

import com.mysema.commons.lang.Assert;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import org.javamoney.moneta.Money;
import org.salespointframework.core.Currencies;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(20)
@Component
public class ItemDataInitializer implements DataInitializer {

	private final ItemCatalog itemCatalog;
	private final SupplierRepository supplierRepository;

	ItemDataInitializer(ItemCatalog itemCatalog, SupplierRepository supplierRepository) {
		Assert.notNull(itemCatalog, "ItemCatalog must not be null!");
		Assert.notNull(supplierRepository, "SupplierRepository must not be null!");

		this.itemCatalog = itemCatalog;
		this.supplierRepository = supplierRepository;
	}

	@Override
	public void initialize() {
		if (!itemCatalog.findAll().isEmpty()) {
			return;
		}

		final Supplier supplier = supplierRepository.findAll().iterator().next();

		itemCatalog.save(new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "chair_2.jpg", "schwarz",
				"Stuhl 1 in schwarz.", supplier, 5, Category.CHAIR));

		itemCatalog.save(new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "sofa_2_green.jpg", "grün",
				"Sofa 1 in grün.", supplier, 50, Category.COUCH));
		itemCatalog.save(new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "sofa_2_red.jpg", "rot",
				"Sofa 1 in rot.", supplier, 50, Category.COUCH));
		itemCatalog.save(new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "sofa_2_white.jpg", "weiß",
				"Sofa 1 in weiß.", supplier, 80, Category.COUCH));

		itemCatalog.save(new Piece(3, "Tisch 1", Money.of(89.99, Currencies.EURO), "table_2.jpg", "weiß",
				"Tisch 1 in weiß.", supplier, 30, Category.TABLE));
	}

}