package furnitureshop.supplier;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

/**
 * This interface is used to store all {@link Supplier}s.
 * It uses the {@link CrudRepository} from {@code Salespoint} to manage search and more.
 */
public interface SupplierRepository extends CrudRepository<Supplier, Long> {

	@Override
	Streamable<Supplier> findAll();

}
