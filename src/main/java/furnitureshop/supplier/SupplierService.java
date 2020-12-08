package furnitureshop.supplier;

import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemService;
import furnitureshop.inventory.Set;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;

@Service
@Transactional
public class SupplierService {

	private final SupplierRepository supplierRepository;
	private final ItemService itemService;

	SupplierService(SupplierRepository supplierRepository, ItemService itemService) {
		Assert.notNull(supplierRepository, "SupplierRepository must not be null!");
		Assert.notNull(itemService, "ItemService must not be null!");

		this.supplierRepository = supplierRepository;
		this.itemService = itemService;
	}

	public boolean addSupplier(Supplier supplier) {
		Assert.notNull(supplier, "Supplier must not be null!");

		if (findByName(supplier.getName()).isPresent()) {
			return false;
		}

		supplierRepository.save(supplier);

		return true;
	}

	public boolean deleteSupplierById(long supplierId) {
		Optional<Supplier> supplier = supplierRepository.findById(supplierId);
		return supplier.map(supp -> {
			Streamable<Item> items = itemService.findBySupplier(supp);
			for (Item it : items) {
				itemService.removeItem(it);
			}
			supplierRepository.delete(supp);
			return true;
		}).orElse(false);
	}

	public Optional<Supplier> findByName(String name) {
		for (Supplier s : findAll()) {
			if (s.getName().equalsIgnoreCase(name)) {
				return Optional.of(s);
			}
		}

		return Optional.empty();
	}

	public Streamable<Supplier> findAll() {
		return supplierRepository.findAll();
	}

	public Optional<Supplier> findById(long id) {
		return supplierRepository.findById(id);
	}

	public Streamable<Item> findItemsBySupplier(Supplier supplier) {
		return itemService.findBySupplier(supplier);
	}

	public void deleteAll() {
		supplierRepository.deleteAll();
	}

	public void analyse() {
		return;
	}

}
