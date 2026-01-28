import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DataRetrieverTest {

    @Test
    void testFindDishById() {
        DataRetriever retriever = new DataRetriever();

        Dish dish = retriever.findDishById(1);

        assertNotNull(dish);
        assertEquals("Salade fraîche", dish.getName());
        assertEquals(new BigDecimal("3500.00"), dish.getSellingPrice());
        assertEquals(2, dish.getIngredients().size());
        assertTrue(dish.getIngredients().stream().anyMatch(i -> i.getName().equals("Laitue")));
        assertTrue(dish.getIngredients().stream().anyMatch(i -> i.getName().equals("Tomate")));

        // Vérifier UnitType
        Ingredient laitue = dish.getIngredients().stream()
                .filter(i -> i.getName().equals("Laitue"))
                .findFirst()
                .orElse(null);
        assertNotNull(laitue);
        assertEquals(UnitType.KG, laitue.getUnit());
        assertEquals(new BigDecimal("0.20"), laitue.getRequiredQuantity());
    }

    @Test
    void testFindDishByIdNotFound() {
        DataRetriever retriever = new DataRetriever();
        assertThrows(RuntimeException.class, () -> retriever.findDishById(999));
    }

    @Test
    void testFindIngredientsPagination() {
        DataRetriever retriever = new DataRetriever();

        List<Ingredient> ingredients = retriever.findIngredients(1, 2);

        assertNotNull(ingredients);
        assertEquals(2, ingredients.size());
        assertEquals("Laitue", ingredients.get(0).getName());
        assertEquals("Tomate", ingredients.get(1).getName());
    }

    @Test
    void testFindIngredientsPaginationNotFound() {
        DataRetriever retriever = new DataRetriever();

        List<Ingredient> ingredients = retriever.findIngredients(100, 10);

        assertNotNull(ingredients);
        assertEquals(0, ingredients.size());
    }

    @Test
    void testFindDishByIngredientName() {
        DataRetriever retriever = new DataRetriever();

        // "eur" dans "Beurre" -> trouve "Gâteau au chocolat"
        List<Dish> dishes = retriever.findDishsByIngredientName("eur");

        assertNotNull(dishes);
        assertFalse(dishes.isEmpty());
        assertEquals("Gâteau au chocolat", dishes.get(0).getName());
        assertEquals(new BigDecimal("8000.00"), dishes.get(0).getSellingPrice());
    }

    @Test
    void testFindIngredientsByCriteria_vegetableCategory() {
        DataRetriever retriever = new DataRetriever();

        // Recherche des ingrédients VEGETABLE
        List<Ingredient> result = retriever.findIngredientsByCriteria(
                null,                    // ingredientName
                CategoryEnum.VEGETABLE,  // category
                null,                    // dishName
                1,                       // page
                10,                      // size
                null                     // requiredQuantity
        );

        assertNotNull(result);
        assertEquals(2, result.size());  // Laitue et Tomate

        assertTrue(result.stream().anyMatch(i -> i.getName().equals("Laitue")));
        assertTrue(result.stream().anyMatch(i -> i.getName().equals("Tomate")));

        // Vérifier que Laitue a bien son UnitType
        Ingredient laitue = result.stream()
                .filter(i -> i.getName().equals("Laitue"))
                .findFirst()
                .orElse(null);
        assertNotNull(laitue);
        assertEquals(UnitType.KG, laitue.getUnit());
    }

    @Test
    void testFindIngredientsByCriteria_nameAndDish_noMatch() {
        DataRetriever retriever = new DataRetriever();

        // Recherche qui ne devrait rien trouver
        List<Ingredient> result = retriever.findIngredientsByCriteria(
                "NEXISTEPAS",      // ingredientName
                null,              // category
                "NEXISTEPAS",      // dishName
                1,                 // page
                10,                // size
                null               // requiredQuantity
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindIngredientsByCriteria_chocolateInCake() {
        DataRetriever retriever = new DataRetriever();

        // Recherche de "choc" dans le plat "gâteau"
        List<Ingredient> result = retriever.findIngredientsByCriteria(
                "choc",           // ingredientName
                null,             // category
                "gâteau",         // dishName
                1,                // page
                10,               // size
                null              // requiredQuantity
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Chocolat", result.get(0).getName());
        assertEquals(UnitType.KG, result.get(0).getUnit());
        assertEquals(new BigDecimal("0.30"), result.get(0).getRequiredQuantity());
    }

    @Test
    void testCreateIngredients_withUniqueNames() {
        DataRetriever retriever = new DataRetriever();

        // Récupérer un plat existant pour l'association
        Dish existingDish = retriever.findDishById(4);  // Gâteau au chocolat

        // Utiliser des noms UNIQUES avec timestamp pour éviter les conflits
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueName1 = "TestIngredient_" + timestamp + "_1";
        String uniqueName2 = "TestIngredient_" + timestamp + "_2";

        Ingredient ing1 = new Ingredient(
                null,
                uniqueName1,
                new BigDecimal("1500.00"),
                CategoryEnum.OTHER,
                existingDish,
                new BigDecimal("0.1"),
                UnitType.KG
        );

        Ingredient ing2 = new Ingredient(
                null,
                uniqueName2,
                new BigDecimal("2500.00"),
                CategoryEnum.DAIRY,
                existingDish,
                new BigDecimal("0.2"),
                UnitType.KG
        );

        List<Ingredient> result = retriever.createIngredients(List.of(ing1, ing2));

        assertNotNull(result);
        assertEquals(2, result.size());

        // Les ingrédients doivent maintenant avoir des IDs
        assertNotNull(result.get(0).getId());
        assertNotNull(result.get(1).getId());

        // Vérifier les noms
        assertEquals(uniqueName1, result.get(0).getName());
        assertEquals(uniqueName2, result.get(1).getName());
    }

    @Test
    void testCreateIngredients_existingIngredient_shouldThrowException() {
        DataRetriever retriever = new DataRetriever();

        Dish existingDish = retriever.findDishById(1);  // Salade fraîche

        // Essayer de créer un ingrédient qui existe DÉJÀ pour ce plat
        // "Laitue" existe déjà pour le plat ID 1
        Ingredient duplicate = new Ingredient(
                null,
                "Laitue",  // Nom déjà existant pour ce plat
                new BigDecimal("800.00"),
                CategoryEnum.VEGETABLE,
                existingDish,
                new BigDecimal("0.2"),
                UnitType.KG
        );

        // Doit lancer une exception car "Laitue" existe déjà pour le plat 1
        assertThrows(RuntimeException.class, () -> {
            retriever.createIngredients(List.of(duplicate));
        });
    }

    @Test
    void testSaveDish_createsNewDish() {
        DataRetriever retriever = new DataRetriever();

        // 1. D'abord, vérifier quel est le prochain ID disponible
        int nextId = retriever.getNextSequenceValue("dish_id_seq");
        System.out.println("Prochain ID dish disponible: " + nextId);

        // 2. Créer un plat avec un nom UNIQUE
        Dish newDish = new Dish();
        newDish.setName("Test Plat " + System.currentTimeMillis()); // Garanti unique
        newDish.setDishType(DishTypeEnum.MAIN);
        newDish.setSellingPrice(new BigDecimal("5000.00"));

        // 3. Appeler saveDish
        Dish savedDish = retriever.saveDish(newDish);

        // 4. Vérifications
        assertNotNull(savedDish);
        assertNotNull(savedDish.getId());
        System.out.println("Plat créé avec ID: " + savedDish.getId());
    }

    @Test
    void testSaveDish_updatesExistingDish() {
        DataRetriever retriever = new DataRetriever();

        // Récupérer un plat existant
        Dish existingDish = retriever.findDishById(1);  // Salade fraîche

        // Modifier son nom
        String originalName = existingDish.getName();
        existingDish.setName("Salade fraîche MODIFIÉE");

        Dish updatedDish = retriever.saveDish(existingDish);

        assertNotNull(updatedDish);
        assertEquals(1, updatedDish.getId());  // Même ID
        assertEquals("Salade fraîche MODIFIÉE", updatedDish.getName());

        // Rétablir le nom original pour ne pas affecter les autres tests
        existingDish.setName(originalName);
        retriever.saveDish(existingDish);
    }
}