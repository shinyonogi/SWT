package furnitureshop.supplier;

import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Order(10)
@Component
public class SupplierDataInitializer implements DataInitializer {

	private final SupplierRepository supplierRepository;

	/**
	 * Creates a new instance of {@link SupplierDataInitializer}
	 *
	 * @param supplierRepository The {@link SupplierRepository} for all {@link Supplier}s
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	SupplierDataInitializer(SupplierRepository supplierRepository) {
		Assert.notNull(supplierRepository, "SupplierRepository must not be null!");

		this.supplierRepository = supplierRepository;
	}

	/**
	 * This method initializes {@link Supplier}s and saves them into the {@link Supplier}, if no {@link Supplier} exists
	 */
	public void initialize() {
		if (supplierRepository.count() > 0) {
			return;
		}

		supplierRepository.save(new Supplier("Müller Möbel", 0.1));
		supplierRepository.save(new Supplier("Möbelmeister", 0.2));
		supplierRepository.save(new Supplier("Herberts schicke Stühle", 0.1));
		supplierRepository.save(new Supplier("Set Supplier", 0));
	}

}
