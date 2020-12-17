package furnitureshop.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.commons.lang.Assert;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import org.javamoney.moneta.Money;
import org.salespointframework.core.Currencies;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This class initialize {@link Item}s and stores them into the {@link ItemCatalog}
 */
@Order(20)
@Component
public class ItemDataInitializer implements DataInitializer {

	private final ItemCatalog itemCatalog;
	private final SupplierRepository supplierRepository;

	/**
	 * Creates a new instance of an {@link ItemDataInitializer}
	 *
	 * @param itemCatalog        The {@link ItemCatalog} for all {@link Item}s
	 * @param supplierRepository The Repository of the suppliers
	 *
	 * @throws IllegalArgumentException If the {@code itemCatalog} or {@code supplierRepository} is {@code null}
	 */
	ItemDataInitializer(ItemCatalog itemCatalog, SupplierRepository supplierRepository) {
		Assert.notNull(itemCatalog, "ItemCatalog must not be null!");
		Assert.notNull(supplierRepository, "SupplierRepository must not be null!");

		this.itemCatalog = itemCatalog;
		this.supplierRepository = supplierRepository;
	}

	/**
	 * This method initializes {@link Item}s and saves them into the {@link ItemCatalog}, if no {@link Item} exists
	 */
	@Override
	public void initialize() {
		if (itemCatalog.count() > 0) {
			return;
		}

		final JsonNode root;
		try {
			final File file = ResourceUtils.getFile("classpath:items.json");
			root = new ObjectMapper().readTree(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		final List<Item> items = new ArrayList<>();

		final Map<Integer, Item> registeredItem = new HashMap<>();
		final Map<String, Supplier> usedSuppliers = new HashMap<>();

		for (JsonNode piece : root.get("pieces")) {
			final int id = piece.get("id").asInt();
			final double price = piece.get("price").asDouble();
			final String imageName = piece.get("picture").asText();
			final String categoryName = piece.get("category").asText();
			final String supplierName = piece.get("supplier").asText();

			final Supplier supplier;
			if (usedSuppliers.containsKey(supplierName)) {
				supplier = usedSuppliers.get(supplierName);
			} else {
				supplier = findSupplierByName(supplierName).orElse(null);
				usedSuppliers.put(supplierName, supplier);
			}

			final byte[] image;
			try {
				final File file = ResourceUtils.getFile("classpath:static/resources/img/inventory/" + imageName);
				image = Files.readAllBytes(Path.of(file.toURI()));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			final Item item = new Piece(
					piece.get("groupid").asInt(),
					piece.get("name").asText(),
					Money.of(price, Currencies.EURO),
					image,
					piece.get("variant").asText(),
					piece.get("description").asText(),
					supplier,
					piece.get("weight").asInt(),
					Category.valueOf(categoryName.toUpperCase())
			);

			items.add(item);
			registeredItem.put(id, item);
		}

		final Supplier setSupplier = findSupplierByName("Set Supplier").orElse(null);

		for (JsonNode set : root.get("sets")) {
			final int id = set.get("id").asInt();
			final String imageName = set.get("picture").asText();
			final double price = set.get("price").asDouble();

			final List<Item> parts = new ArrayList<>();
			for (JsonNode part : set.get("items")) {
				parts.add(registeredItem.get(part.asInt()));
			}

			final byte[] image;
			try {
				final File file = ResourceUtils.getFile("classpath:static/resources/img/inventory/" + imageName);
				image = Files.readAllBytes(Path.of(file.toURI()));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			final Item item = new Set(
					set.get("groupid").asInt(),
					set.get("name").asText(),
					Money.of(price, Currencies.EURO),
					image,
					set.get("variant").asText(),
					set.get("description").asText(),
					setSupplier,
					parts
			);

			items.add(item);
			registeredItem.put(id, item);
		}

		itemCatalog.saveAll(items);
	}

	/**
	 * Finds a {@link Supplier} by their name.
	 *
	 * @param name Name of the supplier
	 *
	 * @return Returns {@link Supplier} or nothing
	 */
	private Optional<Supplier> findSupplierByName(String name) {
		for (Supplier s : supplierRepository.findAll()) {
			if (s.getName().equalsIgnoreCase(name)) {
				return Optional.of(s);
			}
		}

		return Optional.empty();
	}

}
