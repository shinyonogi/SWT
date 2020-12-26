package furnitureshop.inventory;

import furnitureshop.supplier.Supplier;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product;
import org.salespointframework.core.Currencies;
import org.springframework.data.util.Pair;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTests {

	Supplier supplier, surchargeSupplier;

	Piece piece1, piece2, piece3, piece4;
	Piece surchargePiece;

	Set set1, set2;

	@BeforeEach
	void setUp() {
		this.supplier = new Supplier("test", 0);
		this.surchargeSupplier = new Supplier("test2", 0.5);

		this.piece1 = new Piece(0, "piece1", Money.of(10, Currencies.EURO),
				new byte[0], "", "", supplier, 10, Category.CHAIR);
		this.piece2 = new Piece(0, "piece2", Money.of(20, Currencies.EURO),
				new byte[0], "", "", supplier, 20, Category.CHAIR);
		this.piece3 = new Piece(0, "piece3", Money.of(30, Currencies.EURO),
				new byte[0], "", "", supplier, 30, Category.CHAIR);
		this.piece4 = new Piece(0, "piece4", Money.of(40, Currencies.EURO),
				new byte[0], "", "", supplier, 40, Category.CHAIR);

		this.surchargePiece = new Piece(1, "piece5", Money.of(50, Currencies.EURO),
				new byte[0], "", "", surchargeSupplier, 50, Category.COUCH);

		this.set1 = new Set(2, "set1", Money.of(50, Currencies.EURO),
				new byte[0], "", "", supplier, Arrays.asList(piece1, piece2, piece3));
		this.set2 = new Set(2, "set2", Money.of(80, Currencies.EURO),
				new byte[0], "", "", supplier, Arrays.asList(set1, piece4));
	}

	@Test
	void testGetWeight() {
		assertEquals(10, piece1.getWeight(), "getWeight() should return the correct value!");
		assertEquals(60, set1.getWeight(), "getWeight() should return the correct value!");
		assertEquals(100, set2.getWeight(), "getWeight() should return the correct value!");
	}

	@Test
	void testGetPrice() {
		assertEquals(Money.of(10, Currencies.EURO), piece1.getPrice(), "getPrice() should return the correct value!");
		assertEquals(Money.of(50, Currencies.EURO), set1.getPrice(), "getPrice() should return the correct value!");
		assertEquals(Money.of(80, Currencies.EURO), set2.getPrice(), "getPrice() should return the correct value!");

		assertEquals(Money.of(75, Currencies.EURO), surchargePiece.getPrice(), "getPrice() should return the correct value!");
	}

	@Test
	void testGetSupplierPrice() {
		assertEquals(Money.of(10, Currencies.EURO), piece1.getSupplierPrice(), "getSupplierPrice() should return the correct value!");
		assertEquals(Money.of(50, Currencies.EURO), set1.getSupplierPrice(), "getSupplierPrice() should return the correct value!");
		assertEquals(Money.of(80, Currencies.EURO), set2.getSupplierPrice(), "getSupplierPrice() should return the correct value!");

		assertEquals(Money.of(50, Currencies.EURO), surchargePiece.getSupplierPrice(), "getSupplierPrice() should return the correct value!");
	}

	@Test
	void testGetPartTotal() {
		assertEquals(Money.of(10, Currencies.EURO), piece1.getPartTotal(), "getPartTotal() should return the correct value!");
		assertEquals(Money.of(60, Currencies.EURO), set1.getPartTotal(), "getPartTotal() should return the correct value!");
		assertEquals(Money.of(90, Currencies.EURO), set2.getPartTotal(), "getPartTotal() should return the correct value!");

		assertEquals(Money.of(75, Currencies.EURO), surchargePiece.getPartTotal(), "getPartTotal() should return the correct value!");
	}

	@Test
	void testGetPieceTotal() {
		assertEquals(Money.of(60, Currencies.EURO), set1.getPieceTotal(), "getPieceTotal() should return the correct value!");
		assertEquals(Money.of(100, Currencies.EURO), set2.getPieceTotal(), "getPieceTotal() should return the correct value!");
	}

	@Test
	void testGetPiecePrices() {
		final List<Pair<Piece, MonetaryAmount>> prices1 = set1.getPiecePrices();
		assertEquals(3, prices1.size(), "getPiecePrices() should return the correct amount of prices!");

		for (Pair<Piece, MonetaryAmount> pair : prices1) {    //60 -> 50
			if (pair.getFirst().equals(piece1)) {           //10 -> 1/6 -> 50 / 6 -> 8.333
				assertEquals(
						50 / 6.0,
						pair.getSecond().getNumber().doubleValue(),
						1e-12, "getPiecePrices() should return the correct prices!"
				);
			} else if (pair.getFirst().equals(piece2)) {    //20 -> 2/6 -> 50 / 3 -> 16.667
				assertEquals(
						50 / 3.0,
						pair.getSecond().getNumber().doubleValue(),
						1e-12, "getPiecePrices() should return the correct prices!"
				);
			} else if (pair.getFirst().equals(piece3)) {    //30 -> 3/6 -> 50 / 2 -> 25.000
				assertEquals(
						25.0,
						pair.getSecond().getNumber().doubleValue(),
						1e-12, "getPiecePrices() should return the correct prices!"
				);
			} else {
				fail("getPiecePrices() should return the corret Items!");
			}
		}

		final List<Pair<Piece, MonetaryAmount>> prices2 = set2.getPiecePrices();
		assertEquals(4, prices2.size(), "getPiecePrices() should return the correct amount of prices!");

		for (Pair<Piece, MonetaryAmount> pair : prices2) {    //90 -> 80
			if (pair.getFirst().equals(piece1)) {            //30 -> 5/54 -> 200 / 27 -> 7.407
				assertEquals(
						200 / 27.0,
						pair.getSecond().getNumber().doubleValue(),
						1e-12, "getPiecePrices() should return the correct prices!"
				);
			} else if (pair.getFirst().equals(piece3)) {    //10 -> 5/18 -> 200 / 9 -> 22.222
				assertEquals(
						200 / 9.0,
						pair.getSecond().getNumber().doubleValue(),
						1e-12, "getPiecePrices() should return the correct prices!"
				);
			} else if (pair.getFirst().equals(piece2)) {    //20 -> 5/27 -> 400 / 27 -> 14.815
				assertEquals(
						400 / 27.0,
						pair.getSecond().getNumber().doubleValue(),
						1e-12, "getPiecePrices() should return the correct prices!"
				);
			} else if (pair.getFirst().equals(piece4)) {    //40 -> 4/9 -> 320 / 9 -> 35.556
				assertEquals(
						320 / 9.0,
						pair.getSecond().getNumber().doubleValue(),
						1e-12, "getPiecePrices() should return the correct prices!"
				);
			} else {
				fail("getPiecePrices() should return the corret Items!");
			}
		}
	}

	@Test
	void testPieceConstructorWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> new Piece(0, null, Money.of(10, Currencies.EURO), new byte[0], "", "", supplier, 10, Category.CHAIR),
				"Piece() should throw an IllegalArgumentException if the name argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Piece(0, "piece", null, new byte[0], "", "", supplier, 10, Category.CHAIR),
				"Piece() should throw an IllegalArgumentException if the price argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Piece(0, "piece", Money.of(10, Currencies.EURO), null, "", "", supplier, 10, Category.CHAIR),
				"Piece() should throw an IllegalArgumentException if the picture argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Piece(0, "piece", Money.of(10, Currencies.EURO), new byte[0], null, "", supplier, 10, Category.CHAIR),
				"Piece() should throw an IllegalArgumentException if the variant argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Piece(0, "piece", Money.of(10, Currencies.EURO), new byte[0], "", null, supplier, 10, Category.CHAIR),
				"Piece() should throw an IllegalArgumentException if the description argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Piece(0, "piece", Money.of(10, Currencies.EURO), new byte[0], "", "", null, 10, Category.CHAIR),
				"Piece() should throw an IllegalArgumentException if the supplier argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Piece(0, "piece", Money.of(10, Currencies.EURO), new byte[0], "", "", supplier, -1, Category.CHAIR),
				"Piece() should throw an IllegalArgumentException if the weight argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Piece(0, "piece", Money.of(10, Currencies.EURO), new byte[0], "", "", supplier, 10, null),
				"Piece() should throw an IllegalArgumentException if the category argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Piece(0, "piece", Money.of(10, Currencies.EURO), new byte[0], "", "", supplier, 10, Category.SET),
				"Piece() should throw an IllegalArgumentException if the category argument is invalid!"
		);
	}

	@Test
	void testSetConstructorWithInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> new Set(0, null, Money.of(10, Currencies.EURO), new byte[0], "", "", supplier, Collections.singletonList(piece1)),
				"Set() should throw an IllegalArgumentException if the name argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Set(0, "piece", null, new byte[0], "", "", supplier, Collections.singletonList(piece1)),
				"Set() should throw an IllegalArgumentException if the price argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Set(0, "piece", Money.of(10, Currencies.EURO), null, "", "", supplier, Collections.singletonList(piece1)),
				"Set() should throw an IllegalArgumentException if the picture argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Set(0, "piece", Money.of(10, Currencies.EURO), new byte[0], null, "", supplier, Collections.singletonList(piece1)),
				"Set() should throw an IllegalArgumentException if the variant argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Set(0, "piece", Money.of(10, Currencies.EURO), new byte[0], "", null, supplier, Collections.singletonList(piece1)),
				"Set() should throw an IllegalArgumentException if the description argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Set(0, "piece", Money.of(10, Currencies.EURO), new byte[0], "", "", null, Collections.singletonList(piece1)),
				"Set() should throw an IllegalArgumentException if the supplier argument is invalid!"
		);
		assertThrows(IllegalArgumentException.class, () -> new Set(0, "piece", Money.of(10, Currencies.EURO), new byte[0], "", "", supplier, null),
				"Set() should throw an IllegalArgumentException if the weight items is invalid!"
		);
	}

	@Test
	void testItemIsAbstract() {
		assertTrue(Modifier.isAbstract(Item.class.getModifiers()), "Item should be an abstract class!");
	}

	@Test
	void testItemIsProduct() {
		assertTrue(Product.class.isAssignableFrom(Item.class), "Item must extends Product!");
	}

	@Test
	void testItemIsChild() {
		assertTrue(Item.class.isAssignableFrom(Piece.class), "Piece must extends Item!");
		assertTrue(Item.class.isAssignableFrom(Set.class), "Set must extends Item!");
	}

	@Test
	void testItemIsEntity() {
		assertTrue(Item.class.isAnnotationPresent(Entity.class), "Item must have @Entity!");
		assertTrue(Piece.class.isAnnotationPresent(Entity.class), "Piece must have @Entity!");
		assertTrue(Set.class.isAnnotationPresent(Entity.class), "Set must have @Entity!");
	}

}
