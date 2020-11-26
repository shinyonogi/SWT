package furnitureshop.supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;

@Service
@Transactional
public class SupplierService {

	private final SupplierRepository supplierRepository;

	SupplierService(SupplierRepository supplierRepository) {
		Assert.notNull(supplierRepository, "SupplierRepository must not be null!");

		this.supplierRepository = supplierRepository;
	}

	public void addSupplier(Supplier supplier) {
		Assert.notNull(supplier, "Supplier must not be null!");

		supplierRepository.save(supplier);
	}

	public void deleteSupplier(Supplier supplier) {
		Assert.notNull(supplier, "Supplier must not be null!");

		supplierRepository.delete(supplier);
	}

	public void deleteSupplierById(long supplierId) {
		supplierRepository.deleteById(supplierId);
	}

	public Optional<Supplier> findByName(String name) {
		for (Supplier s : findAll()) {
			if (s.getName().equalsIgnoreCase(name)) {
				return Optional.of(s);
			}
		}

		return Optional.empty();
	}

	public Iterable<Supplier> findAll() {
		return supplierRepository.findAll();
	}

	public void analyse() {
		return;
	}

}
