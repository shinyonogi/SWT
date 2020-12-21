package furnitureshop.inventory;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * This data class is used to parse the input of the user when adding and editing an {@link Item}
 */
public class ItemForm {

	private final int groupId, weight;
	private final String name, variant, description;
	private final double price;

	private final Category category;

	private final List<Item> items;

	/**
	 * Creates a new instance of an {@link ItemForm}
	 *
	 * @param groupId     The groupId of the item
	 * @param weight      The weight of the item
	 * @param name        The name of the item
	 * @param variant     The variant of the item
	 * @param description The description of the item
	 * @param price       The price of the item
	 * @param category    The {@link Category} of the item
	 * @param items       Collection of {@link Item} needed for {@link Set}
	 */
	public ItemForm(int groupId, int weight, String name, String variant, String description, double price, Category category, List<Item> items) {
		this.groupId = groupId;
		this.weight = weight;
		this.name = name;
		this.variant = variant;
		this.description = description;
		this.price = price;
		this.category = category;
		this.items = items;
	}

	public int getGroupId() {
		return groupId;
	}

	public String getName() {
		return name;
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

	public List<Item> getItems() {
		return items;
	}
}
