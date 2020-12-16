package furnitureshop.supplier;

import furnitureshop.inventory.Item;
import furnitureshop.inventory.ItemService;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;


/**
 * This class manages all methods to add, remove or find a {@link Supplier} and its assigned {@link Item}s.
 */
@Service
@Transactional
public class SupplierService {

	private final SupplierRepository supplierRepository;
	private final ItemService itemService;

	/**
	 * Creates a new instance of a {@link SupplierService}
	 *
	 * @param supplierRepository {@link SupplierRepository} contains all {@link Supplier}s
	 * @param itemService        {@link ItemService} reference to the itemService
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	SupplierService(SupplierRepository supplierRepository, ItemService itemService) {
		Assert.notNull(supplierRepository, "SupplierRepository must not be null!");
		Assert.notNull(itemService, "ItemService must not be null!");

		this.supplierRepository = supplierRepository;
		this.itemService = itemService;
	}

	/**
	 * Adds a {@link Supplier} to the {@link SupplierRepository} if no {@link Supplier} with the same name already exists
	 *
	 * @param supplier The {@link Supplier} to be added
	 *
	 * @return {@code true} if the {@link Supplier} was added
	 *
	 * @throws IllegalArgumentException if the {@link Supplier} is null
	 */
	public boolean addSupplier(Supplier supplier) {
		Assert.notNull(supplier, "Supplier must not be null!");

		if (findByName(supplier.getName()).isPresent()) {
			return false;
		}

		supplierRepository.save(supplier);

		return true;
	}

	/**
	 * Deletes a {@link Supplier} from the {@link SupplierRepository} and
	 * all {@link Item}s assigned to the {@link Supplier} from the {@link ItemService}
	 *
	 * @param supplierId The id of the {@link Supplier} that is to be deleted
	 *
	 * @return {@code true} if the {@link Supplier} was found in the {@link SupplierRepository}
	 */
	public boolean deleteSupplierById(long supplierId) {
		final Optional<Supplier> supplier = supplierRepository.findById(supplierId);

		return supplier.map(supp -> {
			Streamable<Item> items = itemService.findBySupplier(supp);
			for (Item it : items) {
				itemService.removeItem(it);
			}
			supplierRepository.delete(supp);
			return true;
		}).orElse(false);
	}

	/**
	 * Changes the surcharge for the given {@link Supplier} and saves the new version to {@link SupplierRepository}
	 *
	 * @param supplierId The id of the {@link Supplier} that is to be deleted
	 * @param surcharge value of new surcharge
	 *
	 * @return {@code true} if the surcharge was changed and false if the
	 * supplier was not found in {@link SupplierRepository}
	 *
	 * @throws IllegalArgumentException if the surcharge is lower than 0
	 */
	public boolean changeSupplierSurcharge(long supplierId, double surcharge) {
		Assert.isTrue(surcharge >= 0, "Surcharge must be greater equal to 0!");

		final Optional<Supplier> supplier = supplierRepository.findById(supplierId);

		if (supplier.isPresent()) {
			supplier.get().setSurcharge(surcharge / 100);
			supplierRepository.save(supplier.get());
			return true;
		}
		return false;
	}

	/**
	 * Searches for a {@link Supplier} by its name in the {@link SupplierRepository}
	 *
	 * @param name The name of the {@link Supplier} to be searched for
	 *
	 * @return The found {@link Supplier} with the name
	 *
	 * @throws IllegalArgumentException if the {@code name} is null
	 */
	public Optional<Supplier> findByName(String name) {
		Assert.hasText(name, "Name must not be null!");

		for (Supplier s : findAll()) {
			if (s.getName().equalsIgnoreCase(name)) {
				return Optional.of(s);
			}
		}

		return Optional.empty();
	}

	/**
	 * Finds all {@link Supplier}s in the {@link SupplierRepository}
	 *
	 * @return All {@link Supplier}s in the {@link SupplierRepository}
	 */
	public Streamable<Supplier> findAll() {
		return supplierRepository.findAll();
	}

	/**
	 * Finds a {@link Supplier} by its id
	 *
	 * @param id The id of the {@link Supplier} to be searched for
	 *
	 * @return The found {@link Supplier} with the id
	 */
	public Optional<Supplier> findById(long id) {
		return supplierRepository.findById(id);
	}

	/**
	 * Finds all {@link Item}s of a {@link Supplier}
	 *
	 * @param supplier The {@link Supplier} the {@link Item}s belong to
	 *
	 * @return All {@link Item}s of the {@link Supplier}
	 */
	public Streamable<Item> findItemsBySupplier(Supplier supplier) {
		return itemService.findBySupplier(supplier);
	}

}
