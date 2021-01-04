package furnitureshop.lkw;

import furnitureshop.FurnitureShop;
import furnitureshop.order.OrderService;
import furnitureshop.order.ShopOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.OrderManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = FurnitureShop.class)
class LKWControllerIntegrationTests {

	@Autowired
	MockMvc mvc;

	@Autowired
	OrderService orderService;

	@Autowired
	OrderManagement<ShopOrder> orderManagement;

	@Autowired
	LKWCatalog lkwCatalog;

	LocalDate oldDate, weekendDate, validDate;

	@BeforeEach
	void setUp() {
		this.oldDate = LocalDate.of(2000, 1, 1);
		this.weekendDate = LocalDate.of(2023, 3, 19);
		this.validDate = LocalDate.of(2023, 3, 20);

		for (ShopOrder order : orderService.findAll()) {
			orderManagement.delete(order);
		}
		lkwCatalog.deleteAll();
		for (LKWType type : LKWType.values()) {
			for (int i = 0; i < 2; i++) {
				lkwCatalog.save(new LKW(type));
			}
		}
	}

	@Test
	void returnsModelAndViewOnLkwOverview() throws Exception {
		mvc.perform(get("/lkws"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("types", is(LKWType.values())))
				.andExpect(view().name("lkws"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckout() throws Exception {
		mvc.perform(get("/lkw/checkout/{type}", LKWType.SMALL))
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(0)))
				.andExpect(view().name("lkwCheckout"));
	}

	@Test
	void redirectsToLkwOverviewWithInvalidType() throws Exception {
		mvc.perform(get("/lkw/checkout/unknown"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/lkws"))
				.andExpect(view().name("redirect:/lkws"));
	}

	@Test
	void redirectsToLkwOverviewCheckWithInvalidType() throws Exception {
		mvc.perform(post("/lkw/checkout/unknown")
				.param("check", "").param("date", String.valueOf(oldDate)))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/lkws"))
				.andExpect(view().name("redirect:/lkws"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckWithInvalidDate() throws Exception {
		mvc.perform(post("/lkw/checkout/{type}", LKWType.SMALL)
				.param("check", "").param("date", String.valueOf(oldDate)))
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(4)))
				.andExpect(view().name("lkwCheckout"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckWithWeekend() throws Exception {
		mvc.perform(post("/lkw/checkout/{type}", LKWType.SMALL)
				.param("check", "").param("date", String.valueOf(weekendDate)))
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(5)))
				.andExpect(view().name("lkwCheckout"));
	}

	@Test
	void returnsModelAndViewOnLkwCheckWithValidData() throws Exception {
		mvc.perform(post("/lkw/checkout/{type}", LKWType.SMALL)
				.param("check", "").param("date", String.valueOf(validDate)))
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(-1)))
				.andExpect(view().name("lkwCheckout"));
	}

	@Test
	void redirectsToLkwOverviewCheckoutWithInvalidType() throws Exception {
		mvc.perform(post("/lkw/checkout/unknown")
				.param("buy", "").param("date", String.valueOf(oldDate)))
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
				.andExpect(status().isOk())
				.andExpect(model().attribute("type", is(LKWType.SMALL)))
				.andExpect(model().attribute("result", is(5)))
				.andExpect(view().name("lkwCheckout"));
	}

}