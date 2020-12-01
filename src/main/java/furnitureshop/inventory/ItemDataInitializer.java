package furnitureshop.inventory;

import com.mysema.commons.lang.Assert;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import org.javamoney.moneta.Money;
import org.salespointframework.core.Currencies;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

		Iterator<Supplier> iterator = supplierRepository.findAll().iterator();
		Supplier supplier = iterator.next();

		Piece Stuhl1 = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "/resources/img/chair_2.jpg", "schwarz",
				"Stuhl 1 in schwarz.", supplier, 5, Category.CHAIR);

		supplier = iterator.next();

		Piece Sofa1_green = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "/resources/img/sofa_2_green.jpg", "grün",
				"Sofa 1 in grün.", supplier, 50, Category.COUCH);

		Piece Sofa1_red = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "/resources/img/sofa_2_red.jpg", "rot",
				"Sofa 1 in rot.", supplier, 50, Category.COUCH);

		Piece Sofa1_white = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "/resources/img/sofa_2_white.jpg", "weiß",
				"Sofa 1 in weiß.", supplier, 80, Category.COUCH);

		Piece Sofa1_grey = new Piece(2, "Sofa 1", Money.of(259.99, Currencies.EURO), "/resources/img/sofa_2_grey.jpg", "grau",
				"Sofa 1 in grau.", supplier, 80, Category.COUCH);

		supplier = iterator.next();

		Piece Tisch1 = new Piece(3, "Tisch 1", Money.of(89.99, Currencies.EURO), "/resources/img/table_2.jpg", "weiß",
				"Tisch 1 in weiß.", supplier, 30, Category.TABLE);

		List<Item> Set1_items = new ArrayList<>();
		Set1_items.add(Stuhl1);
		Set1_items.add(Sofa1_grey);

		Supplier setSupplier = new Supplier("Set Supplier", 0.05);
		supplierRepository.save(setSupplier);

		Set Set1 = new Set(4, "Set 1", Money.of(299.99, Currencies.EURO), "/resources/img/set_1.jpg", "black",
				"Set bestehend aus Sofa 1 und Stuhl 1.", setSupplier, Category.SET, Set1_items);

		itemCatalog.save(Stuhl1);
		itemCatalog.save(Sofa1_green);
		itemCatalog.save(Sofa1_grey);
		itemCatalog.save(Sofa1_red);
		itemCatalog.save(Sofa1_white);
		itemCatalog.save(Tisch1);
		itemCatalog.save(Set1);
	}

}