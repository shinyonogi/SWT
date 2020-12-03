package furnitureshop.lkw;

import furnitureshop.order.ContactInformation;
import furnitureshop.order.LKWCharter;
import furnitureshop.order.OrderService;
import org.salespointframework.catalog.ProductIdentifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LKWService {

	// Days where LKWs are available
	private static final List<DayOfWeek> WORK_DAYS = Arrays.asList(
			DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
	);

	private final LKWCatalog lkwCatalog;
	private final OrderService orderService;

	LKWService(LKWCatalog lkwCatalog, @Lazy OrderService orderService) {
		Assert.notNull(lkwCatalog, "LKWCatalog must not be null!");
		Assert.notNull(orderService, "OrderService must not be null!");

		this.lkwCatalog = lkwCatalog;
		this.orderService = orderService;
	}

	/**
	 * Finds the next available {@link LocalDate} to deliver an order
	 *
	 * @param date The earliest {@link LocalDate} to deliver
	 * @param type The {@link LKWType} of the {@link LKW} to be used
	 *
	 * @return The next available {@link LocalDate}
	 */
	public LocalDate findNextAvailableDeliveryDate(LocalDate date, LKWType type) {
		Assert.notNull(date, "Date must not be null!");
		Assert.notNull(type, "Type must not be null!");

		// Check if an LKW is available on that date
		while (!isDeliveryAvailable(date, type)) {
			// If not, check next date
			date = date.plusDays(1);
		}

		// Return found date
		return date;
	}

	/**
	 * Checks if an order can be deliver on that {@link LocalDate}
	 *
	 * @param date The {@link LocalDate} to be checked
	 * @param type The type of the {@link LKW} to be used
	 *
	 * @return {@code true} if an {@link LKW} was found
	 */
	public boolean isDeliveryAvailable(LocalDate date, LKWType type) {
		Assert.notNull(date, "Date must not be null!");
		Assert.notNull(type, "Type must not be null!");

		// Check if the date is a work day
		if (!WORK_DAYS.contains(date.getDayOfWeek())) {
			return false;
		}

		// Check every available LKW in catalog
		for (LKW lkw : findByType(type)) {
			// Get entry of calender of that date
			final Optional<CalendarEntry> entry = lkw.getCalendar().getEntry(date);

			// If no entry was found -> LKW is available
			if (entry.isEmpty()) {
				return true;
			}
			// If entry is a DeliveryEntry -> check if order could be added
			else if (entry.get() instanceof DeliveryEntry) {
				final DeliveryEntry delivery = (DeliveryEntry) entry.get();
				final int quantity = delivery.getQuantity();

				// Check if amount of deliveries is smaller that the maximum
				if (quantity < DeliveryEntry.MAX_DELIVERY) {
					return true;
				}
			}
		}

		// No available LKW was found
		return false;
	}

	/**
	 * Create a {@link DeliveryEntry} on a {@link LocalDate} for a specific type of {@link LKW}
	 *
	 * @param date The {@link LocalDate} to be used
	 * @param type The {@link LKWType} of the {@link LKW} to be used
	 *
	 * @return The used {@link LKW}
	 */
	public Optional<LKW> createDeliveryLKW(LocalDate date, LKWType type) {
		Assert.notNull(date, "Date must not be null!");
		Assert.notNull(type, "Type must not be null!");

		// Check if the date is a work day
		if (!WORK_DAYS.contains(date.getDayOfWeek())) {
			return Optional.empty();
		}

		LKW available = null;

		// Check every available LKW in catalog
		for (LKW lkw : findByType(type)) {
			final Optional<CalendarEntry> entry = lkw.getCalendar().getEntry(date);

			// If no entry was found, save for late
			if (entry.isEmpty()) {
				if (available == null) {
					available = lkw;
				}
			}
			// If entry is a DeliveryEntry -> check if order can be added -> Fewer LKWs
			else if (entry.get() instanceof DeliveryEntry) {
				final DeliveryEntry delivery = (DeliveryEntry) entry.get();
				final int quantity = delivery.getQuantity();

				// Check if amount of deliveries is smaller that the maximum
				if (quantity < DeliveryEntry.MAX_DELIVERY) {
					// Add order to entry
					delivery.setQuantity(quantity + 1);
					lkwCatalog.save(lkw);

					// Return found LKW
					return Optional.of(lkw);
				}
			}
		}

		// Use saved LKW, add order to entry
		if (available != null) {
			final DeliveryEntry delivery = new DeliveryEntry(date);
			delivery.setQuantity(1);

			available.getCalendar().addEntry(delivery);
			lkwCatalog.save(available);
		}

		// Return found LKW or null
		return Optional.ofNullable(available);
	}

	/**
	 * Checks if an {@link LKW} can be rent on that {@link LocalDate}
	 *
	 * @param date The {@link LocalDate} to be checked
	 * @param type The {@link LKWType} of the {@link LKW} to be used
	 *
	 * @return {@code true} if an {@link LKW} was found
	 */
	public boolean isCharterAvailable(LocalDate date, LKWType type) {
		Assert.notNull(date, "Date must not be null!");
		Assert.notNull(type, "Type must not be null!");

		// Check if the date is a work day
		if (!WORK_DAYS.contains(date.getDayOfWeek())) {
			return false;
		}

		// Check every available LKW in catalog
		for (LKW lkw : findByType(type)) {
			final Optional<CalendarEntry> entry = lkw.getCalendar().getEntry(date);

			// Check if no entry exists -> LKW available for rent
			if (entry.isEmpty()) {
				return true;
			}
		}

		// No LKW was found
		return false;
	}


	/**
	 * Create a {@link CharterEntry} on a {@link LocalDate} for a specific type of {@link LKW}
	 *
	 * @param date The {@link LocalDate} to be used
	 * @param type The {@link LKWType} of the {@link LKW} to be used
	 *
	 * @return The used {@link LKW}
	 */
	public Optional<LKW> createCharterLKW(LocalDate date, LKWType type) {
		Assert.notNull(date, "Date must not be null!");
		Assert.notNull(type, "Type must not be null!");

		// Check if the date is a work day
		if (!WORK_DAYS.contains(date.getDayOfWeek())) {
			return Optional.empty();
		}

		// Check every available LKW in catalog
		for (LKW lkw : findByType(type)) {
			final Calendar calendar = lkw.getCalendar();

			// Check if no entry exists -> LKW available for rent
			if (!calendar.hasEntry(date)) {
				// Add order to entry
				calendar.addEntry(new CharterEntry(date));
				lkwCatalog.save(lkw);

				// Return found LKW
				return Optional.of(lkw);
			}
		}

		// No LKW was found
		return Optional.empty();
	}

	public Optional<LKWCharter> createLKWOrder(LKW lkw, LocalDate date, ContactInformation contactInformation) {
		return orderService.orderLKW(lkw, date, contactInformation);
	}

	/**
	 * Cancels a Order for specific {@link LKW} and {@link LocalDate}
	 *
	 * @param lkw  The {@link LKW} to be canceled
	 * @param date The {@link LocalDate} of the Order
	 *
	 * @return {@code true} if the {@link LKW} was used on that {@link LocalDate}
	 */
	public boolean cancelOrder(LKW lkw, LocalDate date) {
		Assert.notNull(lkw, "LKW must not be null!");
		Assert.notNull(date, "Date must not be null!");

		// Get calender and entry of date
		final Calendar calendar = lkw.getCalendar();
		final Optional<CalendarEntry> entry = calendar.getEntry(date);

		// If entry doesn't exists -> can't be canceled
		if (entry.isEmpty()) {
			return false;
		}
		// If entry is CharterEntry -> remove entry
		else if (entry.get() instanceof CharterEntry) {
			calendar.removeEntry(date);
			lkwCatalog.save(lkw);

			return true;
		}
		// If entry is DeliveryEntry -> decrease deliver count
		else if (entry.get() instanceof DeliveryEntry) {
			final DeliveryEntry delivery = (DeliveryEntry) entry.get();
			final int quantity = delivery.getQuantity();

			// Check if this was the only order
			if (quantity <= 1) {
				// Remove entry from calender
				calendar.removeEntry(date);
				lkwCatalog.save(lkw);
				return true;
			}

			// Decrease delivery count
			delivery.setQuantity(quantity - 1);
			lkwCatalog.save(lkw);

			return true;
		}

		// Unknow Entrytype
		throw new IllegalStateException("Invalid CalenderEntry Type");
	}

	public Streamable<LKW> findByType(LKWType type) {
		return lkwCatalog.findAll().filter(l -> l.getType() == type);
	}

	public Streamable<LKW> findAll() {
		return lkwCatalog.findAll();
	}

	public Optional<LKW> findById(ProductIdentifier productId) {
		return lkwCatalog.findById(productId);
	}

}
