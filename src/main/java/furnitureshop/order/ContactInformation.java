package furnitureshop.order;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ContactInformation {

	@Id	@GeneratedValue
	private long id;

	private String name;
	private String address;
	private String email;

	ContactInformation(String name, String address, String email) {
		this.name = name;
		this.address = address;
		this.email = email;
	}

	protected ContactInformation() {}

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
