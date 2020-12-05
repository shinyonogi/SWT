package furnitureshop.lkw;

import org.salespointframework.core.DataInitializer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * This class initialize {@link LKW}s and stores them into the {@link LKWCatalog}
 */
@Component
public class LKWDataInitializer implements DataInitializer {

	// The LKWCatalog to store all LKWs
	private final LKWCatalog lkwCatalog;

	/**
	 * Creates a new instance of an {@link LKWDataInitializer}
	 *
	 * @param lkwCatalog The {@link LKWCatalog} for all {@link LKW}s
	 *
	 * @throws IllegalArgumentException If the {@code lkwCatalog} is {@code null}
	 */
	LKWDataInitializer(LKWCatalog lkwCatalog) {
		Assert.notNull(lkwCatalog, "LKWCatalog must not be null!");

		this.lkwCatalog = lkwCatalog;
	}

	/**
	 * This method initializes {@link LKW}s and saves them into the {@link LKWCatalog}, if no {@link LKW} exists
	 */
	@Override
	public void initialize() {
		// Check if LKWs are already initialized
		if (lkwCatalog.count() > 0L) {
			return;
		}

		// Add LKWs to catalog
		for (LKWType type : LKWType.values()) {
			for (int i = 0; i < 2; i++) {
				lkwCatalog.save(new LKW(type));
			}
		}
	}

}
