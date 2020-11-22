package furnitureshop.lkw;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

class LKWCharterForm {

	private final String name;
	private final String address;
	private final String email;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private final LocalDate date;

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
