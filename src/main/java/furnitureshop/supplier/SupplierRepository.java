package furnitureshop.supplier;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

public interface SupplierRepository extends CrudRepository<Supplier, Long> {

	@Override
	Streamable<Supplier> findAll();

}
