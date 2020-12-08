package furnitureshop.order;

import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ContactInformation {

	@Id @GeneratedValue
	private long id;

	private String name;
	private String address;
	private String email;

	@Deprecated
	protected ContactInformation() {}

	public ContactInformation(String name, String address, String email) {
		Assert.hasText(name, "Name must not be null!");
		Assert.notNull(address, "Address must not be null!");
		Assert.notNull(email, "Email must not be null!");

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
