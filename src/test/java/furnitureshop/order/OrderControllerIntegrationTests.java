package furnitureshop.order;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import furnitureshop.FurnitureShop;
import furnitureshop.inventory.Category;
import furnitureshop.inventory.Item;
import furnitureshop.inventory.Piece;
import furnitureshop.supplier.Supplier;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.core.Currencies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

/**
 * IntegrationTest for {@link OrderController}
 *
 * @author Shintaro Onogi
 */

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = FurnitureShop.class)
public class OrderControllerIntegrationTests {

	@Autowired MockMvc mvc;

	Supplier supplier;
	Item item;

	@BeforeEach
	void setUp() {
		supplier = new Supplier("supplier", 0.05);
		item = new Piece(1, "Stuhl 1", Money.of(59.99, Currencies.EURO), "/resources/img/chair_2.jpg", "schwarz",
				"Stuhl 1 in schwarz.", supplier, 5, Category.CHAIR);
	}

	/**
	 * cartWebIntegrationTest method
	 * Tests if you can reach the /cart
	 * Expects to reach it
	 *
	 * @throws Exception
	 */

	@Test
	void cartWebIntegrationTest() throws Exception {

		mvc.perform(get("/cart"))
			.andExpect(status().isOk())
			.andExpect(view().name("cart"));
	}

	/*
	@Test
	void addItemIntegrationTest() throws Exception {
		mvc.perform(post("/cart/{id}", item.getId())
				.param("number", "5"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", endsWith("/")));
	}*/
}