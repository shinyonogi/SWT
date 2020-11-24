package furnitureshop.lkw;

import org.salespointframework.core.DataInitializer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class LKWDataInitializer implements DataInitializer {

	private final LKWCatalog lkwCatalog;

	LKWDataInitializer(LKWCatalog lkwCatalog) {
		Assert.notNull(lkwCatalog, "LKWCatalog must not be null!");

		this.lkwCatalog = lkwCatalog;
	}

	@Override
	public void initialize() {
		if (!lkwCatalog.findAll().isEmpty()) {
			return;
		}

		for (LKWType type : LKWType.values()) {
			for (int i = 0; i < 2; i++) {
				lkwCatalog.save(new LKW(type));
			}
		}
	}

}
