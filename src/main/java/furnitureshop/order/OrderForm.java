package furnitureshop.order;

public class OrderForm {

	private final String name;
	private final String address;
	private final String email;
	private final int index;

	/**
	 * Creates a new instance of {@link OrderForm}
	 *
	 * @param name    The name of the customer
	 * @param address The address of the customer
	 * @param email   The Email-address of the customer
	 * @param index   The selected index of the ordertype
	 */
	public OrderForm(String name, String address, String email, int index) {
		this.name = name;
		this.address = address;
		this.email = email;
		this.index = index;
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

	public int getIndex() {
		return index;
	}

}
