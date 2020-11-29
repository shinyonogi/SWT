package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.salespointframework.catalog.ProductIdentifier;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;

@Service
@Transactional
public class ItemService {

	private final ItemCatalog itemCatalog;

	public ItemService(ItemCatalog itemCatalog) {
		Assert.notNull(itemCatalog, "ItemCatalog must not be null!");

		this.itemCatalog = itemCatalog;
	}

	public Optional<Item> findById(ProductIdentifier id) {
		Assert.notNull(id, "Id must not be null!");

		return itemCatalog.findById(id);
	}

	public void addItem(Item item) {
		Assert.notNull(item, "Item must not be null!");

		itemCatalog.save(item);
	}

	public void removeItem(Item item) {
		Assert.notNull(item, "Item must not be null!");

		itemCatalog.delete(item);
	}

	public Streamable<Item> findBySupplier(Supplier supplier) {
		Assert.notNull(supplier, "Supplier must not be null!");

		return itemCatalog.findAll().filter(it -> it.getSupplier() == supplier);
	}

	public Streamable<Item> findAll() {
		return itemCatalog.findAll();
	}

	public Streamable<Item> findAllByCategory(Category category) {
		Assert.notNull(category, "Category must not be null!");

		return itemCatalog.findAll().filter(it -> it.getCategory() == category);
	}

	public Streamable<Item> findVariants(Optional<Item> item){
		return itemCatalog.findAll().filter(it -> it.getGroupid() == item.get().getGroupid());
	}

}
