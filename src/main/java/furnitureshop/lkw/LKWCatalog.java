package furnitureshop.lkw;

import org.salespointframework.catalog.Catalog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

/**
 * This interface is used to store all {@link LKW}s.
 * It uses the {@link Catalog} from {@code Salespoint} to manage search and more.
 */
public interface LKWCatalog extends Catalog<LKW> {

	@Override
	Streamable<LKW> findAll();

}
