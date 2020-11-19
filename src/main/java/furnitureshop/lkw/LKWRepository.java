package furnitureshop.lkw;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

interface LKWRepository extends CrudRepository<LKW, Long> {

	@Override
	Streamable<LKW> findAll();

}
