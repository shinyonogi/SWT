package furnitureshop.lkw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LKWControllerIntegrationTests {

	@Autowired
	MockMvc mvc;

	LocalDate oldDate, weekendDate, validDate;

	@BeforeEach
	void setUp() {
		oldDate = LocalDate.of(2000, 1, 1);
		weekendDate = LocalDate.of(2023, 3, 19);
		validDate = LocalDate.of(2023, 3, 20);
	}

	@Test
	void returnsModelAndViewOnLkwOverview() throws Exception {
		mvc.perform(get("/lkws"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(model().attribute("types", is(LKWType.values())))
				.andExpect(view().name("lkws"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckout() throws Exception {
		mvc.perform(get("/lkw/checkout/{type}", LKWType.SMALL))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(0)))
				.andExpect(view().name("lkwCheckout"));
	}

	@Test
	void redirectsToLkwOverviewWithInvalidType() throws Exception {
		mvc.perform(get("/lkw/checkout/unknown"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/lkws"))
				.andExpect(view().name("redirect:/lkws"));
	}

	@Test
	void redirectsToLkwOverviewCheckWithInvalidType() throws Exception {
		mvc.perform(post("/lkw/checkout/unknown")
				.param("check", "").param("date", String.valueOf(oldDate)))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/lkws"))
				.andExpect(view().name("redirect:/lkws"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckWithInvalidDate() throws Exception {
		mvc.perform(post("/lkw/checkout/{type}", LKWType.SMALL)
				.param("check", "").param("date", String.valueOf(oldDate)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(4)))
				.andExpect(view().name("lkwCheckout"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckWithWeekend() throws Exception {
		mvc.perform(post("/lkw/checkout/{type}", LKWType.SMALL)
				.param("check", "").param("date", String.valueOf(weekendDate)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(5)))
				.andExpect(view().name("lkwCheckout"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckWithValidData() throws Exception {
		mvc.perform(post("/lkw/checkout/{type}", LKWType.SMALL)
				.param("check", "").param("date", String.valueOf(validDate)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(-1)))
				.andExpect(view().name("lkwCheckout"));
	}

	@Test
	void redirectsToLkwOverviewCheckoutWithInvalidType() throws Exception {
		mvc.perform(post("/lkw/checkout/unknown")
				.param("buy", "").param("date", String.valueOf(oldDate)))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/lkws"))
				.andExpect(view().name("redirect:/lkws"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckoutWithValidData() throws Exception {
		mvc.perform(post("/lkw/checkout/{type}", LKWType.SMALL)
				.param("buy", "").param("name", "name")
				.param("address", "address").param("email", "email@email.de")
				.param("date", String.valueOf(validDate)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(model().attribute("charterDate", is(validDate)))
				.andExpect(view().name("orderSummary"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckoutWithInvalidName() throws Exception {
		mvc.perform(post("/lkw/checkout/{type}", LKWType.SMALL)
				.param("buy", "").param("name", "")
				.param("address", "address").param("email", "email@email.de")
				.param("date", String.valueOf(validDate)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(1)))
				.andExpect(view().name("lkwCheckout"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckoutWithInvalidAddress() throws Exception {
		mvc.perform(post("/lkw/checkout/{type}", LKWType.SMALL)
				.param("buy", "").param("name", "name")
				.param("address", "").param("email", "email@email.de")
				.param("date", String.valueOf(validDate)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(2)))
				.andExpect(view().name("lkwCheckout"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckoutWithInvalidEmail() throws Exception {
		mvc.perform(post("/lkw/checkout/{type}", LKWType.SMALL)
				.param("buy", "").param("name", "name")
				.param("address", "address").param("email", "email")
				.param("date", String.valueOf(validDate)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(3)))
				.andExpect(view().name("lkwCheckout"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckoutWithInvalidDate() throws Exception {
		mvc.perform(post("/lkw/checkout/{type}", LKWType.SMALL)
				.param("buy", "").param("name", "name")
				.param("address", "address").param("email", "email@email.de")
				.param("date", String.valueOf(oldDate)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(4)))
				.andExpect(view().name("lkwCheckout"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckoutWithWeekend() throws Exception {
		mvc.perform(post("/lkw/checkout/{type}", LKWType.SMALL)
				.param("buy", "").param("name", "name")
				.param("address", "address").param("email", "email@email.de")
				.param("date", String.valueOf(weekendDate)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(5)))
				.andExpect(view().name("lkwCheckout"));
	}

}