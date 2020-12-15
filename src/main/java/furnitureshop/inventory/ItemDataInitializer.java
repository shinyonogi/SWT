package furnitureshop.inventory;

import com.mysema.commons.lang.Assert;
import furnitureshop.supplier.Supplier;
import furnitureshop.supplier.SupplierRepository;
import org.javamoney.moneta.Money;
import org.salespointframework.core.Currencies;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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

		final List<Item> items = new ArrayList<>();

		final Supplier muellerSupplier = findSupplierByName("Müller Möbel").orElse(null);
		final Supplier moebelSupplier = findSupplierByName("Möbelmeister").orElse(null);
		final Supplier stuehleSupplier = findSupplierByName("Herberts schicke Stühle").orElse(null);

		//add chairs
		items.add(new Piece(2, "Classic", Money.of(49.99, Currencies.EURO), "chair_3_black.jpg", "schwarz holz",
				"Classic in schwarz mit Kunstleder und Eichenholz. Der Classic-Stuhl ist ein Allrounder und passt mit seinen neutralen Farben in jedes Zimmer.", stuehleSupplier, 5, Category.CHAIR));

		items.add(new Piece(2, "Classic", Money.of(49.99, Currencies.EURO), "chair_3_white.jpg", "weiß holz",
				"Classic in weiß mit Kunstleder und Eichenholz. Der Classic-Stuhl ist ein Allrounder und passt mit seinen neutralen Farben in jedes Zimmer.", stuehleSupplier, 5, Category.CHAIR));

		Piece chair_10_white = new Piece(10, "Basic", Money.of(39.99, Currencies.EURO), "chair_10_white.jpg", "weiß buchenholz",
				"Basic in weiß. Der Basic-Stuhl eignet sich gut als Küchenstuhl, da er sehr schlicht ist und eine spezielle wasserabweisende Beschichtung besitzt.", stuehleSupplier, 5, Category.CHAIR);

		items.add(chair_10_white);

		Piece chair_10_black = new Piece(10, "Basic", Money.of(39.99, Currencies.EURO), "chair_10_black.jpg", "schwarz buchenholz",
				"Basic in schwarz. Der Basic-Stuhl eignet sich gut als Küchenstuhl, da er sehr schlicht ist und eine spezielle wasserabweisende Beschichtung besitzt.", stuehleSupplier, 5, Category.CHAIR);

		items.add(chair_10_black);

		items.add(new Piece(10, "Basic+", Money.of(49.99, Currencies.EURO), "chair_2.jpg", "schwarz akazienholz",
				"BasicPlus in schwarz mit Akazienholz und Armlehnen für einen edlen Look. Der BasicPlus-Stuhl eignet sich gut als Küchenstuhl, da er sehr schlicht ist und eine spezielle wasserabweisende Beschichtung besitzt.", stuehleSupplier, 5, Category.CHAIR));

		items.add(new Piece(3, "Tropic", Money.of(89.99, Currencies.EURO), "chair_4.jpg", "blau palisander",
				"Tropic mit blauem Lederpolster und edlem Palisanderholz. Der Tropic-Stuhl eignet sich gut als Küchen- oder Wohnzimmerstuhl, um einen Kaffee mit alten Freunden zu genießen.", stuehleSupplier, 5, Category.CHAIR));

		items.add(new Piece(5, "Savanna", Money.of(89.99, Currencies.EURO), "chair_6.jpg", "beige balsaholz",
				"Savanna mit beigen Balsaholz. Der Savanna-Stuhl wurde den Farben der Savanne nachempfunden und ist ein Platz der Erholung. Er eignet sich gut für das Wohnzimmer.", stuehleSupplier, 5, Category.CHAIR));

		Piece chair_7 = new Piece(6, "Comfort", Money.of(99.99, Currencies.EURO), "chair_7.jpg", "grau stoff",
				"Comfort mit grauem Polster und Fußablage. Der Comfort-Sessel ist der Platz zum Abschalten nach der Arbeit.", stuehleSupplier, 5, Category.CHAIR);

		items.add(chair_7);

		items.add(new Piece(7, "Freedom", Money.of(69.99, Currencies.EURO), "chair_8.jpg", "helles rattan",
				"Freedom aus hellem Rattan und weißen Polstern. Der Freedom-Sessel eignet sich sowohl für den Außenbereich, als auch den Innenbereich. Er bietet ein luftiges Erlebnis mit weichen Polstern.", stuehleSupplier, 5, Category.CHAIR));

		items.add(new Piece(11, "Designer", Money.of(129.99, Currencies.EURO), "chair_11.jpg", "navy leder",
				"Designer mit blau gefärbtem Echtleder und Akazienholz. Der Designer-Stuhl ist ein hochwertiger aber schlicht gehaltener Stuhl.", stuehleSupplier, 5, Category.CHAIR));

		items.add(new Piece(8, "Oldtimer", Money.of(119.99, Currencies.EURO), "chair_9.jpg", "braun leder",
				"Oldtimer mit Lederbezug. Der Oldtimer-Stuhl zeichnet sich durch seine extrem gute Polsterung aus, welche auch nach dem schwersten Tag ein federleichtes Gefühl vermittelt.", stuehleSupplier, 5, Category.CHAIR));

		items.add(new Piece(4, "Focus", Money.of(149.99, Currencies.EURO), "chair_5.jpg", "schwarz leder",
				"Focus mit schwarzem Leder. Der Focus-Sessel schafft durch seine einzigartige Form eine eigene Welt, um Bücher, Musik oder Filme noch mehr genießen zu können.", stuehleSupplier, 5, Category.CHAIR));

		//add couches
		items.add(new Piece(23, "Classic", Money.of(799.99, Currencies.EURO), "sofa_4.jpg", "weiß stoff",
				"Classic in weiß. Die Classic-Couch ist ein Allrounder, dem es an nichts fehlt. Sie hat ein schlichtes, aber elegantes Design, gute Polster und Platz bis für 4 Leute.", muellerSupplier, 50, Category.COUCH));

		Piece sofa_8 = new Piece(27, "Basic", Money.of(399.99, Currencies.EURO), "sofa_8.jpg", "grau stoff",
				"Basic in grau. Die Basic-Couch ist das Einsteigermodell in die Welt der Sofas. Sie bietet Platz für zwei Personen.", muellerSupplier, 50, Category.COUCH);

		items.add(sofa_8);

		items.add(new Piece(24, "Basic+", Money.of(559.99, Currencies.EURO), "sofa_5.jpg", "grau stoff",
				"Basic+ in grau. Die BasicPlus-Couch ist ein Einsteigermodell in die Welt der Sofas. Sie bietet Platz für drei Personen.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(22, "Vintage", Money.of(999.99, Currencies.EURO), "sofa_3.jpg", "braun leder",
				"Vintage mit hellem Leder. Die Vintage-Couch sieht nicht nur cool aus, sondern besitzt auch besonders weiche Polster.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(25, "Oldtimer", Money.of(899.99, Currencies.EURO), "sofa_6.jpg", "braun leder",
				"Oldtimer mit Leder. Die Oldtimer-Couch zeichnet sich durch ihre extrem gute Polsterung und ihren Vintage Look aus.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(21, "Prestige", Money.of(1499.99, Currencies.EURO), "sofa_2_black.jpg", "schwarz leder",
				"Prestige mit schwarzem Leder. Die Prestige-Couch aus edlem Echtleder bietet zahlreiche Funktionen, wie verstellbare Kopflehnen.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(21, "Prestige", Money.of(1499.99, Currencies.EURO), "sofa_2_brown.jpg", "braun leder",
				"Prestige mit braunem Leder. Die Prestige-Couch aus edlem Echtleder bietet zahlreiche Funktionen, wie verstellbare Kopflehnen.", muellerSupplier, 50, Category.COUCH));

		items.add(new Piece(26, "Prestige-Comfort", Money.of(1699.99, Currencies.EURO), "sofa_7.jpg", "weiß",
				"Prestige-Comfort in weiß. Die Prestige-Comfort-Couch kommt mit extra weichen Lehnen und Kissen und bietet wie die Prestige-Couch ausreichend Platz.", muellerSupplier, 50, Category.COUCH));

		//add tables
		items.add(new Piece(38, "Classic", Money.of(479.99, Currencies.EURO), "table_10.jpg", "Eichenholz",
				"Classic aus Eichenholz. Der Classic-Tisch, welcher nur aus Eichenholz besteht, ist das Standardmodell, und gibt dem Raum eine natürliche Note. Er bietet ausreichend Platz für die ganze Familie.", moebelSupplier, 30, Category.TABLE));

		Piece table_9 = new Piece(37, "Basic", Money.of(49.99, Currencies.EURO), "table_9.jpg", "schwarz",
				"Basic mit schwarzer quadratischer Tischplatte. Der Basic-Tisch eignet sich für ein bis zwei Personen.", moebelSupplier, 30, Category.TABLE);

		items.add(table_9);

		Piece table_7 = new Piece(35, "Basic+", Money.of(54.99, Currencies.EURO), "table_7.jpg", "weiß",
				"Basic+ mit weißer runder Tischplatte. Der BasicPlus-Tisch bietet mehr Fläche als der Basic-Tisch und eignet sich für bis zu 3 Personen.", moebelSupplier, 30, Category.TABLE);

		items.add(table_7);

		items.add(new Piece(30, "Focus", Money.of(119.99, Currencies.EURO), "table_2.jpg", "weiß",
				"Focus in weiß mit eingebauten Schubladen. Der Focus-Tisch hat ein effizientes Schubladendesign für mehr Produktivität und Ordnung. Er eignet sich hervorragend als Arbeitstisch.", moebelSupplier, 30, Category.TABLE));

		items.add(new Piece(31, "Designer", Money.of(449.99, Currencies.EURO), "table_3.jpg", "massiv wildeiche",
				"Designer mit massiver Wildeiche. Der Designer-Tisch bietet eine modische alternative zu herkömmlichen Tischen. Durch seine massive Bauart wird er zum Mittelpunkt des Raumes.", moebelSupplier, 30, Category.TABLE));

		items.add(new Piece(32, "Vintage", Money.of(369.99, Currencies.EURO), "table_4.jpg", "Glas",
				"Vintage mit Glasplatte. Der Vintage-Tisch bietet viel Platz und wirkt durch seine runde Glasplatte einladend.", moebelSupplier, 30, Category.TABLE));

		items.add(new Piece(33, "Oldtimer", Money.of(579.99, Currencies.EURO), "table_5.jpg", "Walnuss",
				"Oldtimer aus Walnussholz. Der Oldtimer-Tisch ist ein großer Familientisch und bietet Platz für 8 Personen.", moebelSupplier, 30, Category.TABLE));

		items.add(new Piece(34, "Ceramic", Money.of(269.99, Currencies.EURO), "table_6.jpg", "weiße Keramik",
				"Ceramic mit weißer Keramikplatte. Der Ceramic-Tisch ist ein flacher stylischer Tisch fürs Wohnzimmer.", moebelSupplier, 30, Category.TABLE));

		items.add(new Piece(36, "Exquisite", Money.of(629.99, Currencies.EURO), "table_8.jpg", "Ahorn",
				"Exquisite mit Ahornholz. Der Exquisite-Tisch ist ein großer hochwertiger Tisch mit Platz für 6 Personen.", moebelSupplier, 30, Category.TABLE));

		//add sets
		final Supplier setSupplier = findSupplierByName("Set Supplier").orElse(null);

		items.add(new Set(40, "Wohnzimmer-Set", Money.of(399.99, Currencies.EURO), "set_1.jpg", "schwarz",
				"Set bestehend aus 1xBasic-Couch und 1xBasicPlus-Stuhl.", setSupplier, Arrays.asList(chair_7, sofa_8)));

		items.add(new Set(41, "Küchen-Set-1", Money.of(93.99, Currencies.EURO), "set_2.jpg", "schwarz",
				"Set bestehend aus 1xBasic-Stuhl(weiß) und 1xBasicPlus-Tisch.", setSupplier, Arrays.asList(chair_10_black, table_9)));

		items.add(new Set(41, "Küchen-Set-2", Money.of(99.99, Currencies.EURO), "set_3.jpg", "weiß",
				"Set bestehend aus 1xBasic-Stuhl(schwarz) und 1xBasic-Tisch.", setSupplier, Arrays.asList(chair_10_white, table_7)));

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
