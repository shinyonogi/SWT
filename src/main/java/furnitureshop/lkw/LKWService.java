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
	 * Finds the next available {@code Date} to deliver an order
	 *
	 * @param date The earliest {@code Date} to deliver
	 * @param type The type of the {@code LKW} to be used
	 *
	 * @return The next available {@code Date}
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
	 * Checks if an order can be deliver on that {@code Date}
	 *
	 * @param date The {@code Date} to be checked
	 * @param type The type of the {@code LKW} to be used
	 *
	 * @return {@code true} if an {@code LKW} was found
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
				if (delivery.getQuantity() < DeliveryEntry.MAX_DELIVERY) {
					return true;
				}
			}
		}

		// No available LKW was found
		return false;
	}

	/**
	 * Create a DeliveryOrder on a {@code Date} for a specific type of {@code LKW}
	 *
	 * @param date The {@code Date} to be used
	 * @param type The type of the {@code LKW} to be used
	 *
	 * @return The used {@code LKW}
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

					// Return found LKW
					return Optional.of(lkw);
				}
			}
		}

		// Use saved LKW, add order to entry
		if (available != null) {
			available.getCalendar().addEntry(new DeliveryEntry(date));
		}

		// Return found LKW or null
		return Optional.ofNullable(available);
	}

	/**
	 * Checks if an {@code LKW} can be rent on that {@code Date}
	 *
	 * @param date The {@code Date} to be checked
	 * @param type The type of the {@code LKW} to be used
	 *
	 * @return {@code true} if an {@code LKW} was found
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
	 * Create a CharterOrder on a {@code Date} for a specific type of {@code LKW}
	 *
	 * @param date The {@code Date} to be used
	 * @param type The type of the {@code LKW} to be used
	 *
	 * @return The used {@code LKW}
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
	 * Cancels a Order for specific {@code LKW} and {@code Date}
	 *
	 * @param lkw The {@code LKW} to be canceled
	 * @param date The {@code Date} of the Order
	 *
	 * @return {@code true} if the {@code LKW} was used on that {@code Date}
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

			return true;
		}
		// If entry is DeliveryEntry -> decrease deliver count
		else if (entry.get() instanceof DeliveryEntry) {
			final DeliveryEntry dilivery = (DeliveryEntry) entry.get();
			final int quantity = dilivery.getQuantity();

			// Check if this was the only order
			if (quantity <= 1) {
				// Remove entry from calender
				calendar.removeEntry(date);
				return true;
			}

			// Decrease delivery count
			dilivery.setQuantity(quantity - 1);

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
