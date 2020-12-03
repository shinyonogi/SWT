package furnitureshop.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import furnitureshop.admin.AdminController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * IntegrationTest for {@link OrderController}
 *
 * @author Shintaro Onogi
 */

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIntegrationTests {

	@Autowired MockMvc mvc;

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
}