package furnitureshop.inventory;

public class ItemForm {

	private final int groupId, weight;
	private final String name, picture, variant, description;
	private final double price;

	private final Category category;

	public ItemForm(int groupId, int weight, String name, String picture, String variant, String description, double price, Category category) {
		this.groupId = groupId;
		this.weight = weight;
		this.name = name;
		this.picture = picture;
		this.variant = variant;
		this.description = description;
		this.price = price;
		this.category = category;
	}

	public int getGroupId() {
		return groupId;
	}

	public String getName() {
		return name;
	}

	public String getPicture() {
		return picture;
	}

	public String getVariant() {
		return variant;
	}

	public String getDescription() {
		return description;
	}

	public double getPrice() {
		return price;
	}

	public Category getCategory() {
		return category;
	}

	public int getWeight() {
		return weight;
	}

}
