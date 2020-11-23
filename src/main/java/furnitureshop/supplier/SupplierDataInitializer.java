package furnitureshop.supplier;

import org.salespointframework.core.DataInitializer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class SupplierDataInitializer implements DataInitializer {
	
	private SupplierRepository supplierRepository;
	
	public SupplierDataInitializer(SupplierRepository supplierRepository) {
		Assert.notNull(supplierRepository, "supplierRepository must not be null!");
		this.supplierRepository = supplierRepository;
	}
	
	public void initialize() {
		if(!supplierRepository.findAll().iterator().hasNext()) {
			return;
		}

		supplierRepository.save(new Supplier("Müller Möbel", 0.1));
		supplierRepository.save(new Supplier("Möbelmeister", 0.2));
		supplierRepository.save(new Supplier("Herberts schicke Stühle", 0.1));
	}
}
