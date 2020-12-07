package furnitureshop.inventory;

import org.salespointframework.catalog.Catalog;

/**
 * This interface is used to store all {@link Item}s.
 * It uses the {@link Catalog} from {@code Salespoint} to manage search and more.
 */
public interface ItemCatalog extends Catalog<Item> {
}
