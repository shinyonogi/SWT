package furnitureshop.inventory;

import furnitureshop.order.ItemOrder;
import furnitureshop.order.ItemOrderEntry;
import furnitureshop.order.OrderService;
import furnitureshop.order.OrderStatus;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierService;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.core.Currencies;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;
import java.util.*;

/**
 * This class manages all methods to add, remove or find an {@link Item} by its attributes.
 */
@Service
@Transactional
public class ItemService {

	private final ItemCatalog itemCatalog;
	private final SupplierService supplierService;
	private final OrderService orderService;

	/**
	 * Creates a new instance of an {@link ItemService}
	 *
	 * @param itemCatalog     {@link ItemCatalog} which contains all items
	 * @param supplierService {@link SupplierService} reference to the SupplierService
	 * @param orderService    {@link OrderService} reference to the OrderService
	 *
	 * @throws IllegalArgumentException If {@code itemCatalog} is {@code null}
	 */
	public ItemService(ItemCatalog itemCatalog, @Lazy SupplierService supplierService, @Lazy OrderService orderService) {
		Assert.notNull(itemCatalog, "ItemCatalog must not be null!");
		Assert.notNull(supplierService, "SupplierService must not be null!");
		Assert.notNull(orderService, "OrderService must not be null!");

		this.itemCatalog = itemCatalog;
		this.supplierService = supplierService;
		this.orderService = orderService;
	}

	/**
	 * Adds or updates an {@link Item} in the catalog
	 *
	 * @param item A {@link Item} to add to the {@code ItemCatalog}
	 *
	 * @throws IllegalArgumentException If {@code item} is {@code null}
	 */
	public void addOrUpdateItem(Item item) {
		Assert.notNull(item, "Item must not be null!");

		itemCatalog.save(item);
	}

	/**
	 * Removes an {@link Item} from the catalog
	 *
	 * @param item A {@link Item} to remove from the {@code itemCatalog}
	 *
	 * @return {@code true} if the {@link Item} was removed
	 *
	 * @throws IllegalArgumentException If {@code item} is {@code null}
	 */
	public boolean removeItem(Item item) {
		Assert.notNull(item, "Item must not be null!");

		if (findById(item.getId()).isEmpty()) {
			return false;
		}

		for (Item it : findAllSetsByItem(item)) {
			removeItem(it);
		}

		itemCatalog.delete(item);

		return true;
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
	 * Finds all visibble items in the catalog
	 *
	 * @return Returns all visible items in the {@code itemCatalog}
	 */
	public Streamable<Item> findAllVisible() {
		return itemCatalog.findAll().filter(Item::isVisible);
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

		return findAll().filter(it -> it.getCategory() == category);
	}

	/**
	 * Finds all visible items of a specific category
	 *
	 * @param category A {@link Category}
	 *
	 * @return Returns a stream of visible {@link Item}s all with the same category
	 *
	 * @throws IllegalArgumentException If {@code category} is {@code null}
	 */
	public Streamable<Item> findAllVisibleByCategory(Category category) {
		Assert.notNull(category, "Category must not be null!");

		return findAllVisible().filter(it -> it.getCategory() == category);
	}

	/**
	 * Finds all items of a specific category
	 *
	 * @param groupId GroupId
	 *
	 * @return Returns a stream of {@link Item}s all with the same {@code groupId}
	 */
	public Streamable<Item> findAllByGroupId(int groupId) {
		return findAll().filter(it -> it.getGroupId() == groupId);
	}

	/**
	 * Finds all visible items of a specific category
	 *
	 * @param groupId GroupId
	 *
	 * @return Returns a stream of visible {@link Item}s all with the same {@code groupId}
	 */
	public Streamable<Item> findAllVisibleByGroupId(int groupId) {
		return findAllVisible().filter(it -> it.getGroupId() == groupId);
	}

	/**
	 * Finds all sets of which a given item is a part of
	 *
	 * @param item An {@link Item}
	 *
	 * @return A list of {@link Set}s
	 */
	public List<Set> findAllSetsByItem(Item item) {
		final List<Set> sets = new ArrayList<>();

		for (Item it : findAll()) {
			if (it instanceof Set) {
				Set set = (Set) it;
				if (set.getItems().contains(item)) {
					sets.add(set);
				}
			}
		}

		return sets;
	}

	/**
	 * Find a specific supplier with the given id
	 *
	 * @param id A {@link Supplier} id
	 *
	 * @return Returns a {@link Supplier}
	 */
	public Optional<Supplier> findSupplierById(long id) {
		return supplierService.findById(id);
	}

	public Map<Supplier, MonetaryAmount[]> analyse(LocalDateTime today) {
		final HashMap<Supplier, MonetaryAmount[]> supplierAmountMap = new HashMap<>();

		for (ItemOrder itemOrder : orderService.findAllItemOrders()) {
			final LocalDateTime orderDate = itemOrder.getDateCreated();

			final int index;
			if (orderDate.isBefore(today) && orderDate.isAfter(today.minusDays(30))) {
				index = 0;
			} else if (orderDate.isBefore(today.minusDays(30)) && orderDate.isAfter(today.minusDays(60))) {
				index = 1;
			} else {
				continue;
			}

			for (ItemOrderEntry entry : itemOrder.getOrderEntries()) {
				if (entry.getStatus().equals(OrderStatus.COMPLETED)) {
					if (entry.getItem() instanceof Set) {
						final Set set = (Set) entry.getItem();

						for (Pair<Item, MonetaryAmount> pair : set.getItemPartPrices()) {
							final Supplier supplier = pair.getFirst().getSupplier();

							if (!supplierAmountMap.containsKey(supplier)) {
								supplierAmountMap.put(supplier, new MonetaryAmount[]{Currencies.ZERO_EURO, Currencies.ZERO_EURO});
							}

							supplierAmountMap.get(supplier)[index] = supplierAmountMap.get(supplier)[index].add(pair.getSecond());
						}
					} else {
						final Supplier supplier = entry.getItem().getSupplier();

						if (!supplierAmountMap.containsKey(supplier)) {
							supplierAmountMap.put(supplier, new MonetaryAmount[]{Currencies.ZERO_EURO, Currencies.ZERO_EURO});
						}

						supplierAmountMap.get(supplier)[index] = supplierAmountMap.get(supplier)[index].add(entry.getItem().getPrice());
					}
				}
			}
		}

		for (Supplier supplier : supplierService.findAll()) {
			if (supplier.getName().equals("Set Supplier")) {
				continue;
			}

			if (!supplierAmountMap.containsKey(supplier)) {
				supplierAmountMap.put(supplier, new MonetaryAmount[]{Currencies.ZERO_EURO, Currencies.ZERO_EURO});
				continue;
			}

			supplierAmountMap.get(supplier)[1] = supplierAmountMap.get(supplier)[0].subtract(supplierAmountMap.get(supplier)[1]);
		}
		return supplierAmountMap;
	}
}
