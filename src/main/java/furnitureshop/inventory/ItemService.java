package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.salespointframework.catalog.ProductIdentifier;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ItemService {

	private final ItemCatalog itemCatalog;

	public ItemService(ItemCatalog itemCatalog) {
		Assert.notNull(itemCatalog, "ItemCatalog must not be null!");

		this.itemCatalog = itemCatalog;
	}

	public void addItem(Item item) {
		Assert.notNull(item, "Item must not be null!");
		itemCatalog.save(item);
	}

	public void removeItem(Item item) {
		Assert.notNull(item, "Item must not be null!");
		for (Item it : findAllSetsByItem(item)){
			removeItem(it);
		}
		itemCatalog.delete(item);
	}

	public Streamable<Item> findAll() {
		return itemCatalog.findAll();
	}

	public Streamable<Item> findBySupplier(Supplier supplier) {
		Assert.notNull(supplier, "Supplier must not be null!");

		return itemCatalog.findAll().filter(it -> it.getSupplier() == supplier);
	}

	public Optional<Item> findById(ProductIdentifier id) {
		Assert.notNull(id, "Id must not be null!");

		return itemCatalog.findById(id);
	}

	public Streamable<Item> findAllByCategory(Category category) {
		Assert.notNull(category, "Category must not be null!");

		return itemCatalog.findAll().filter(it -> it.getCategory() == category);
	}

	public Streamable<Item> findAllByGroupId(int groupId){
		return itemCatalog.findAll().filter(it -> it.getGroupid() == groupId);
	}

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

}
