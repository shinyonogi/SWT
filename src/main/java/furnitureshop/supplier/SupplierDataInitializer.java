package furnitureshop.supplier;

import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Order(10)
@Component
public class SupplierDataInitializer implements DataInitializer {

	private final SupplierRepository supplierRepository;

	SupplierDataInitializer(SupplierRepository supplierRepository) {
		Assert.notNull(supplierRepository, "SupplierRepository must not be null!");

		this.supplierRepository = supplierRepository;
	}

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
