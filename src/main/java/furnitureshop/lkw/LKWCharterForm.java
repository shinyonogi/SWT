package furnitureshop.lkw;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * This data class is used to parse the input of the checkout, when renting a {@link LKW}
 */
class LKWCharterForm {

	private final String name;
	private final String address;
	private final String email;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private final LocalDate date;

	/**
	 * Creates a new instance of an {@link LKWCharterForm}
	 *
	 * @param name    The name of customer
	 * @param address The address of the customer
	 * @param email   The E-Mail address of the customer
	 * @param date    The {@link LocalDate} when to rent a {@link LKW}
	 */
	public LKWCharterForm(String name, String address, String email, LocalDate date) {
		this.name = name;
		this.address = address;
		this.email = email;
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getEmail() {
		return email;
	}

	public LocalDate getDate() {
		return date;
	}

}
