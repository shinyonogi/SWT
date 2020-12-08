package furnitureshop.order;

import org.junit.jupiter.api.Test;

import javax.persistence.Entity;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContactInformationTests {

	@Test
	void testConstructorWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> new ContactInformation(null, "address", "email"),
				"ContactInformation() should throw an IllegalArgumentException if the name argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new ContactInformation("", "address", "email"),
				"ContactInformation() should throw an IllegalArgumentException if the name argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new ContactInformation("name", null, "email"),
				"ContactInformation() should throw an IllegalArgumentException if the address argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new ContactInformation("name", "address", null),
				"ContactInformation() should throw an IllegalArgumentException if the email argument is invalid!"
		);
	}

	@Test
	void testContactInformationIsEntity() {
		assertTrue(ContactInformation.class.isAnnotationPresent(Entity.class), "ContactInformation must have @Entity!");
	}

}
