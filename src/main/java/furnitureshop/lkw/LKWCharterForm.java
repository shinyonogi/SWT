package furnitureshop.lkw;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

class LKWCharterForm {

	private final String name;
	private final String email;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private final LocalDate date;

	public LKWCharterForm(String name, String email, LocalDate date) {
		this.name = name;
		this.email = email;
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public LocalDate getDate() {
		return date;
	}

}
