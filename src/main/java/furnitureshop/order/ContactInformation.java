package furnitureshop.order;

import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

/**
 * This class represents the {@link ContactInformation} of the customer which is needed for an order
 */
@Entity
public class ContactInformation {

	@Id @GeneratedValue
	private long id;

	private String name;
	private String address;
	private String email;

	@Deprecated
	protected ContactInformation() {}

	/**
	 * Creates a new instance of {@link ContactInformation}
	 *
	 * @param name    name of the customer
	 * @param address address of the customer
	 * @param email   email-address of the customer
	 *
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
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
