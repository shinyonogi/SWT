/* package videoshop.orders;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTests {

	@Autowired MockMvc mvc;

	@Test
	void cartWebIntegrationTest() throws Exception {

		mvc.perform(get("/cart"))
			.andExpect(status().isOk())
			.andExpect(view().name("cart"));
	}
}

 */