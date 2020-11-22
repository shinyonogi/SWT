package furnitureshop.order;

public class ContactInformation {
	private final String name;
	private final String address;
	private final String email;

	ContactInformation(String name, String address, String email) {
		this.name = name;
		this.address = address;
		this.email = email;
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
}
