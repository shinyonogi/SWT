package furnitureshop.lkw;

import org.salespointframework.core.DataInitializer;
import org.springframework.util.Assert;

public class LKWDataInitializer implements DataInitializer {

	private final LKWCatalog lkwCatalog;

	public LKWDataInitializer(LKWCatalog lkwCatalog) {
		Assert.notNull(lkwCatalog, "lkwManager must not be null!");

		this.lkwCatalog = lkwCatalog;
	}

	@Override
	public void initialize() {
			for (int j = 0; j < 7; j++){
				switch(j % 3) {
					case 0:
						lkwCatalog.save(new LKW(LKWType.SMALL));
					case 1:
						lkwCatalog.save(new LKW(LKWType.MEDIUM));
					case 2:
						lkwCatalog.save(new LKW(LKWType.LARGE));
				}
			}
	}
}
