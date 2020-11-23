package furnitureshop.supplier;

import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SupplierManager {
	
	private SupplierRepository supplierRepository;
	
	public SupplierManager(SupplierRepository supplierRepository) {
		this.supplierRepository = supplierRepository;
	}
	
	public void addSupplier(Supplier supplier) {
		supplierRepository.save(supplier);
	}
	
	public void deleteSupplier(Supplier supplier) {
		supplierRepository.delete(supplier);
	}
	
	public void deleteSupplier(long supplierId) {
		supplierRepository.deleteById(supplierId);
	}

	public Iterable<Supplier> findAll() {
		return supplierRepository.findAll();
	}

	public void analyse() { return; }
}
