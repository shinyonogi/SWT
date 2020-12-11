package furnitureshop.lkw;

import furnitureshop.order.ContactInformation;
import furnitureshop.order.LKWCharter;
import org.salespointframework.time.BusinessTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

/**
 * This class manages all HTTP Requests for LKWs
 */
@Controller
public class LKWController {

	// A reference to the LKWService to access system information
	private final LKWService lkwService;

	// A refernce to the BusinessTime to access the current system time
	private final BusinessTime businessTime;

	/**
	 * Creates a new instance of an {@link LKWController}
	 *
	 * @param lkwService   The {@link LKWService} to access system information
	 * @param businessTime The {@link BusinessTime} to get the current time
	 *
	 * @throws IllegalArgumentException If any argument is {@code null}
	 */
	LKWController(LKWService lkwService, BusinessTime businessTime) {
		Assert.notNull(lkwService, "LKWService must not be null!");
		Assert.notNull(businessTime, "BusinessTime must not be null!");

		this.lkwService = lkwService;
		this.businessTime = businessTime;
	}

	/**
	 * Handles all GET-Request for '/lkws'.
	 * It returns the page with a list of all available {@link LKWType}s
	 *
	 * @param model The {@code Spring} Page {@link Model}
	 *
	 * @return The overview page of all {@link LKWType}s
	 */
	@GetMapping("/lkws")
	String getLKWList(Model model) {
		// Add all types to the model to be displayed
		model.addAttribute("types", LKWType.values());

		return "lkws";
	}

	/**
	 * Handles all GET-Request for '/lkw/checkout/{type}'.
	 * It returns the checkout page if the type exists or redirects to the overview.
	 *
	 * @param typeName The name of the {@link LKWType}
	 * @param model    The {@code Spring} Page {@link Model}
	 *
	 * @return The checkout page for a {@link LKWType} or redirects to the overview page
	 * of the {@link LKWType} don't exist
	 */
	@GetMapping("/lkw/checkout/{lkwtype}")
	String getLKWCheckout(@PathVariable("lkwtype") String typeName, Model model) {
		// Get type by name, used to ensure case insensitivity
		final Optional<LKWType> type = LKWType.getByName(typeName);

		// Check if no type is found -> redirect to type overview
		if (type.isEmpty()) {
			return "redirect:/lkws";
		}

		// Add attributes to model to display form with default values and no errors
		model.addAttribute("lkwform", new LKWCharterForm("", "", "", businessTime.getTime().toLocalDate()));
		model.addAttribute("type", type.get());
		model.addAttribute("result", 0);

		return "lkwCheckout";
	}

	/**
	 * Handles all POST-Request for '/lkw/checkout/{type}'.
	 * It manages the check, if an {@link LKW} is on a specific {@link java.time.LocalDate LocalDate} available.
	 * If an {@link LKW} is available it displays a positive message, or a negative if not.
	 *
	 * @param typeName The name of the {@link LKWType}
	 * @param form     The {@link LKWCharterForm} with the checking {@link java.time.LocalDate LocalDate}
	 * @param model    The {@code Spring} Page {@link Model}
	 *
	 * @return The checkout page with information about the availability of {@link LKW}s
	 */
	@PostMapping(value = "/lkw/checkout/{lkwtype}", params = "check")
	String checkLKWDate(@PathVariable("lkwtype") String typeName, @ModelAttribute("lkwform") LKWCharterForm form, Model model) {
		// Get type by name, used to ensure case insensitivity
		final Optional<LKWType> type = LKWType.getByName(typeName);

		// Check if no type is found -> redirect to type overview
		if (type.isEmpty()) {
			return "redirect:/lkws";
		}

		// Add old attributes to model to keep input
		model.addAttribute("lkwform", form);
		model.addAttribute("type", type.get());

		// Check if date is invalid or before current date
		if (form.getDate() == null || !form.getDate().isAfter(businessTime.getTime().toLocalDate())) {
			// Display error message
			model.addAttribute("result", 4);
			return "lkwCheckout";
		}
		// Check if LKW with given type is available on the date
		if (!lkwService.isCharterAvailable(form.getDate(), type.get())) {
			// Display error message
			model.addAttribute("result", 5);
			return "lkwCheckout";
		}

		// Display success message
		model.addAttribute("result", -1);

		return "lkwCheckout";
	}

	/**
	 * Handles all POST-Request for '/lkw/checkout/{type}'.
	 * It manages the buying, for a specific {@link LKWType}.
	 * It will be checked if the information from the customer are valid and if a {@link LKW} is available.
	 * If a {@link LKW} is available, it will be ordered. If not, there will be an error message displayed.
	 *
	 * @param typeName The name of the {@link LKWType}
	 * @param form     The {@link LKWCharterForm} with the information about customer and {@link java.time.LocalDate LocalDate}
	 * @param model    The {@code Spring} Page {@link Model}
	 *
	 * @return Redirect to the Order Summary if information are correct or displays Error message
	 */
	@PostMapping(value = "/lkw/checkout/{lkwtype}", params = "buy")
	String checkoutLKW(@PathVariable("lkwtype") String typeName, @ModelAttribute("lkwform") LKWCharterForm form, Model model) {
		// Get type by name, used to ensure case insensitivity
		final Optional<LKWType> type = LKWType.getByName(typeName);

		// Check if no type is found -> redirect to type overview
		if (type.isEmpty()) {
			return "redirect:/lkws";
		}

		// Add old attributes to model to keep input
		model.addAttribute("lkwform", form);
		model.addAttribute("type", type.get());

		// Check if name is invalid
		if (!StringUtils.hasText(form.getName())) {
			// Display error message
			model.addAttribute("result", 1);
			return "lkwCheckout";
		}
		// Check if address is invalid
		if (!StringUtils.hasText(form.getAddress())) {
			// Display error message
			model.addAttribute("result", 2);
			return "lkwCheckout";
		}
		// Check if email is invalid
		if (!StringUtils.hasText(form.getEmail()) || !form.getEmail().matches(".+@.+")) {
			// Display error message
			model.addAttribute("result", 3);
			return "lkwCheckout";
		}
		// Check if date is invalid or before current date
		if (form.getDate() == null || !form.getDate().isAfter(businessTime.getTime().toLocalDate())) {
			// Display error message
			model.addAttribute("result", 4);
			return "lkwCheckout";
		}

		// Create Calender entry and get used LKW
		final Optional<LKW> lkw = lkwService.createCharterLKW(form.getDate(), type.get());

		// Check if lkw with given type is available on the date
		if (lkw.isEmpty()) {
			// Display error message
			model.addAttribute("result", 5);
			return "lkwCheckout";
		}

		// Create the contact information
		final ContactInformation contactInformation = new ContactInformation(form.getName(), form.getAddress(), form.getEmail());

		// Create the LKW Order
		final Optional<LKWCharter> order = lkwService.createLKWOrder(lkw.get(), form.getDate(), contactInformation);

		// Should never be empty
		if (order.isEmpty()) {
			// Display error message
			model.addAttribute("result", 6);
			return "lkwCheckout";
		}

		// Construct summary for the order
		model.addAttribute("lkw", lkw.get());
		model.addAttribute("order", order.get());
		model.addAttribute("charterDate", form.getDate());

		return "orderSummary";
	}

}
