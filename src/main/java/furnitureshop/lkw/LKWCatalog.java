package furnitureshop.lkw;

import org.salespointframework.catalog.Catalog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

interface LKWCatalog extends Catalog<LKW> {

	@Override
	Streamable<LKW> findAll();

}
