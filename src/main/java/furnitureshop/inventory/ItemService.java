package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierService;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.core.Currencies;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class manages all methods to add, remove or find an {@link Item} by its attributes.
 */
@Service
@Transactional
public class ItemService {

	private final ItemCatalog itemCatalog;
	private final SupplierService supplierService;

	/**
	 * Creates a new instance of an {@link Item}
	 *
	 * @param itemCatalog {@link ItemCatalog} which contains all items
	 *
	 * @param supplierService
	 * @throws IllegalArgumentException If {@code itemCatalog} is {@code null}
	 */
	public ItemService(ItemCatalog itemCatalog, @Lazy SupplierService supplierService) {
		Assert.notNull(itemCatalog, "ItemCatalog must not be null!");
		Assert.notNull(supplierService, "SupplierService must not be null!");

		this.itemCatalog = itemCatalog;
		this.supplierService = supplierService;
	}

	/**
	 * Adds an {@link Item} to the catalog
	 *
	 * @param item A {@link Item} to add to the {@code ItemCatalog}
	 *
	 * @throws IllegalArgumentException If {@code item} is {@code null}
	 */
	public void addItem(Item item) {
		Assert.notNull(item, "Item must not be null!");
		itemCatalog.save(item);
	}

	/**
	 * Removes an {@link Item} from the catalog
	 *
	 * @param item A {@link Item} to remove from the {@code itemCatalog}
	 *
	 * @throws IllegalArgumentException If {@code item} is {@code null}
	 */
	public void removeItem(Item item) {
		Assert.notNull(item, "Item must not be null!");
		for (Item it : findAllSetsByItem(item)){
			removeItem(it);
		}
		itemCatalog.delete(item);
	}

	/**
	 * Finds all items in the catalog
	 *
	 * @return Returns all items in the {@code itemCatalog}
	 */
	public Streamable<Item> findAll() {
		return itemCatalog.findAll();
	}

	/**
	 * Finds all items from a specific supplier
	 *
	 * @param supplier A {@link Supplier}
	 *
	 * @return Returns a stream of {@link Item}s with the same {@link Supplier}
	 *
	 * @throws IllegalArgumentException If {@code supplier} is {@code null}
	 */
	public Streamable<Item> findBySupplier(Supplier supplier) {
		Assert.notNull(supplier, "Supplier must not be null!");

		return itemCatalog.findAll().filter(it -> it.getSupplier() == supplier);
	}

	/**
	 * Finds a specific item by its id
	 *
	 * @param id A {@link ProductIdentifier}
	 *
	 * @return Returns an {@link Item}
	 *
	 * @throws IllegalArgumentException If {@code id} is {@code null}
	 */
	public Optional<Item> findById(ProductIdentifier id) {
		Assert.notNull(id, "Id must not be null!");

		return itemCatalog.findById(id);
	}

	/**
	 * Finds all items of a specific category
	 *
	 * @param category A {@link Category}
	 *
	 * @return Returns a stream of {@link Item}s all with the same category
	 *
	 * @throws IllegalArgumentException If {@code category} is {@code null}
	 */
	public Streamable<Item> findAllByCategory(Category category) {
		Assert.notNull(category, "Category must not be null!");

		return itemCatalog.findAll().filter(it -> it.getCategory() == category);
	}

	/**
	 * Finds all items of a specific category
	 *
	 * @param groupId GroupId
	 *
	 * @return Returns a stream of {@link Item}s all with the same {@code groupId}
	 */
	public Streamable<Item> findAllByGroupId(int groupId){
		return itemCatalog.findAll().filter(it -> it.getGroupid() == groupId);
	}

	/**
	 * Finds all sets of which a given item is a part of
	 *
	 * @param item An {@link Item}
	 * @return A list of {@link Set}s
	 */
	public List<Set> findAllSetsByItem(Item item) {
		List<Set> sets = new ArrayList<>();
		for (Item it : findAll()){
			if (it instanceof Set){
				Set set = (Set) it;
				if (set.getItems().contains(item)) {
					sets.add(set);
				}
			}
		}
		return sets;
	}

	public Optional<Item> createItemFromForm(ItemForm itemForm, long suppId) {
		Optional<Supplier> supplier = findSupplierById(suppId);
		if (supplier.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(new Piece(itemForm.getGroupId(),itemForm.getName(), Money.of(itemForm.getPrice(), Currencies.EURO), itemForm.getPicture(), itemForm.getVariant(), itemForm.getDescription(), supplier.get(), itemForm.getWeight(), itemForm.getCategory()));
	}

	public Item editItemFromForm(Item item, ItemForm itemForm) {
		return item;
	}

	public Optional<Supplier> findSupplierById(long id){
		return supplierService.findById(id);
	}

}
