package furnitureshop.lkw;

import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class LKWManager {

	private static final List<DayOfWeek> WORK_DAYS = Arrays.asList(
			DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
	);

	private final LKWCatalog lkwCatalog;

	LKWManager(LKWCatalog lkwCatalog) {
		Assert.notNull(lkwCatalog, "LKWCatalog must not be null!");

		this.lkwCatalog = lkwCatalog;
	}

	public LocalDate findNextAvailableDeliveryDate(LocalDate date, LKWType type) {
		while (!isDeliveryAvailable(date, type)) {
			date = date.plusDays(1);
		}
		return date;
	}

	public boolean isDeliveryAvailable(LocalDate date, LKWType type) {
		Assert.notNull(date, "Date must not be null!");
		Assert.notNull(type, "Type must not be null!");

		if (!WORK_DAYS.contains(date.getDayOfWeek())) {
			return false;
		}

		for (LKW lkw : findByType(type)) {
			final CalendarEntry entry = lkw.getCalendar().getEntry(date);

			if (entry instanceof DeliveryEntry) {
				final DeliveryEntry delivery = (DeliveryEntry) entry;
				final int quantity = delivery.getQuantity();

				if (delivery.getQuantity() < DeliveryEntry.MAX_DELIVERY) {
					return true;
				}
			} else if (entry == null) {
				return true;
			}
		}

		return false;
	}

	public LKW createDeliveryOrder(LocalDate date, LKWType type) {
		Assert.notNull(date, "Date must not be null!");
		Assert.notNull(type, "Type must not be null!");

		if (!WORK_DAYS.contains(date.getDayOfWeek())) {
			return null;
		}

		LKW available = null;

		for (LKW lkw : findByType(type)) {
			final CalendarEntry entry = lkw.getCalendar().getEntry(date);

			if (entry instanceof DeliveryEntry) {
				final DeliveryEntry delivery = (DeliveryEntry) entry;
				final int quantity = delivery.getQuantity();

				if (quantity < DeliveryEntry.MAX_DELIVERY) {
					delivery.setQuantity(quantity + 1);

					return lkw;
				}
			} else if (entry == null && available == null) {
				available = lkw;
			}
		}

		if (available != null) {
			available.getCalendar().addEntry(new DeliveryEntry(date));
		}

		return available;
	}

	public boolean isCharterAvailable(LocalDate date, LKWType type) {
		Assert.notNull(date, "Date must not be null!");
		Assert.notNull(type, "Type must not be null!");

		if (!WORK_DAYS.contains(date.getDayOfWeek())) {
			return false;
		}

		for (LKW lkw : findByType(type)) {
			final CalendarEntry entry = lkw.getCalendar().getEntry(date);

			if (entry == null) {
				return true;
			}
		}

		return false;
	}

	public LKW createCharterOrder(LocalDate date, LKWType type) {
		Assert.notNull(date, "Date must not be null!");
		Assert.notNull(type, "Type must not be null!");

		if (!WORK_DAYS.contains(date.getDayOfWeek())) {
			return null;
		}

		for (LKW lkw : findByType(type)) {
			final Calendar calendar = lkw.getCalendar();

			if (!calendar.hasEntry(date)) {
				calendar.addEntry(new CharterEntry(date));

				return lkw;
			}
		}

		return null;
	}

	public boolean cancelOrder(LKW lkw, LocalDate date) {
		Assert.notNull(lkw, "LKW must not be null!");
		Assert.notNull(date, "Date must not be null!");

		final Calendar calendar = lkw.getCalendar();
		final CalendarEntry entry = calendar.getEntry(date);

		if (entry == null) {
			return false;
		} else if (entry instanceof CharterEntry) {
			calendar.removeEntry(date);

			return true;
		} else if (entry instanceof DeliveryEntry) {
			final DeliveryEntry dilivery = (DeliveryEntry) entry;
			final int quantity = dilivery.getQuantity();

			if (quantity <= 1) {
				calendar.removeEntry(date);
				return true;
			}

			dilivery.setQuantity(quantity - 1);

			return true;
		}

		throw new IllegalStateException("Invalid CalenderEntry Type");
	}

	public Streamable<LKW> findByType(LKWType type) {
		return lkwCatalog.findAll().filter(l -> l.getType() == type);
	}

	public Streamable<LKW> findAll() {
		return lkwCatalog.findAll();
	}

}
