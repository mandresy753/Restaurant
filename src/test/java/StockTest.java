import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

public class StockTest {
    public static void main(String[] args) {
        // Date de calcul
        Instant t = LocalDateTime.of(2024, 1, 6, 12, 0).toInstant(ZoneOffset.UTC);

        // Création des ingrédients avec stock initial (même si on n'a pas stocké directement la quantité)
        Ingredient laitue = new Ingredient(1, "Laitue", BigDecimal.valueOf(1.0), CategoryEnum.VEGETABLE, null, null, UnitType.KG);
        Ingredient tomate = new Ingredient(2, "Tomate", BigDecimal.valueOf(2.0), CategoryEnum.VEGETABLE, null, null, UnitType.KG);
        Ingredient poulet = new Ingredient(3, "Poulet", BigDecimal.valueOf(5.0), CategoryEnum.ANIMAL, null, null, UnitType.KG);
        Ingredient chocolat = new Ingredient(4, "Chocolat", BigDecimal.valueOf(3.0), CategoryEnum.OTHER, null, null, UnitType.KG);
        Ingredient beurre = new Ingredient(5, "Beurre", BigDecimal.valueOf(2.5), CategoryEnum.DAIRY, null, null, UnitType.KG);

        // Ajout des mouvements de sortie
        laitue.getStockMovementList().add(
                new StockMovement(6, new StockValue(0.2, UnitType.KG), MovementType.OUT, t)
        );
        tomate.getStockMovementList().add(
                new StockMovement(7, new StockValue(0.15, UnitType.KG), MovementType.OUT, t)
        );
        poulet.getStockMovementList().add(
                new StockMovement(8, new StockValue(1.0, UnitType.KG), MovementType.OUT, t)
        );
        chocolat.getStockMovementList().add(
                new StockMovement(9, new StockValue(0.3, UnitType.KG), MovementType.OUT, t)
        );
        beurre.getStockMovementList().add(
                new StockMovement(10, new StockValue(0.2, UnitType.KG), MovementType.OUT, t)
        );

        // Simulation des stocks initiaux par mouvement IN (pour que getStockValueAt fonctionne)
        laitue.getStockMovementList().add(0, new StockMovement(1, new StockValue(5.0, UnitType.KG), MovementType.IN, Instant.parse("2024-01-01T00:00:00Z")));
        tomate.getStockMovementList().add(0, new StockMovement(2, new StockValue(4.0, UnitType.KG), MovementType.IN, Instant.parse("2024-01-01T00:00:00Z")));
        poulet.getStockMovementList().add(0, new StockMovement(3, new StockValue(10.0, UnitType.KG), MovementType.IN, Instant.parse("2024-01-01T00:00:00Z")));
        chocolat.getStockMovementList().add(0, new StockMovement(4, new StockValue(3.0, UnitType.KG), MovementType.IN, Instant.parse("2024-01-01T00:00:00Z")));
        beurre.getStockMovementList().add(0, new StockMovement(5, new StockValue(2.5, UnitType.KG), MovementType.IN, Instant.parse("2024-01-01T00:00:00Z")));

        // Test des stocks à la date t
        Arrays.asList(laitue, tomate, poulet, chocolat, beurre).forEach(ingredient -> {
            StockValue stock = ingredient.getStockValueAt(t);
            System.out.println(ingredient.getName() + " : " + stock.getQuantity() + " " + stock.getUnit());
        });
    }
}
