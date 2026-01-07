import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class DataRetrieverTest {
    @Test
    void testFindDishById() {
        DataRetriever retriever = new DataRetriever();

        Dish dish = retriever.findDishById(1);

        assertNotNull(dish);
        assertEquals("Salade fraîche", dish.getName());
        assertEquals(2, dish.getIngredients().size());
        assertTrue(dish.getIngredients().stream().anyMatch(i -> i.getName().equalsIgnoreCase("Laitue")));
        assertTrue(dish.getIngredients().stream().anyMatch(i -> i.getName().equalsIgnoreCase("Tomate")));
    }
    @Test
    void testFindDishByIdNotFound() {
        DataRetriever retriever = new DataRetriever();

        assertThrows(RuntimeException.class, () -> {
            retriever.findDishById(999);
        });
    }
    @Test
    void testFindIngredientsPagination() {
        DataRetriever retriever = new DataRetriever();

        int page = 2;
        int size = 2;

        List<Ingredient> ingredients = retriever.findIngredients(page, size);

        assertNotNull(ingredients);
        assertEquals(2, ingredients.size());

        assertEquals("Poulet", ingredients.get(0).getName());
        assertEquals("Chocolat", ingredients.get(1).getName());
    }
    @Test
    void testFindIngredientsPaginationNotFound() {
        DataRetriever retriever = new DataRetriever();

        int page = 3;
        int size = 5;

        List<Ingredient> ingredients = retriever.findIngredients(page, size);

        assertEquals(0, ingredients.size());

    }
    @Test
    void testFindDishByIngredientName() {
        String ingredientName = "eur";
        DataRetriever retriever = new DataRetriever();

        assertEquals("Gâteau au chocolat", retriever.findDishsByIngredientName(ingredientName).get(0).getName());
    }
    @Test
    void testFindIngredientsByCriteria() {
        DataRetriever retriever = new DataRetriever();
        String ingredientName = null;
        CategoryEnum category = CategoryEnum.VEGETABLE;
        String dishName = null;
        int page = 1;
        int size = 10;

        List<Ingredient> result = retriever.findIngredientsByCriteria(
                ingredientName,
                category,
                dishName,
                page,
                size
        );

        assertNotNull(result);
        assertEquals(2, result.size());

        List<String> ingredientNames = result.stream()
                .map(Ingredient::getName)
                .toList();

        assertTrue(ingredientNames.contains("Laitue"));
        assertTrue(ingredientNames.contains("Tomate"));

    }
    @Test
    void testFindIngredientsByCriteria_nameAndDish_noMatch() {
        DataRetriever retriever = new DataRetriever();

        String ingredientName = "cho";
        CategoryEnum category = null;
        String dishName = "Sal";
        int page = 1;
        int size = 10;

        List<Ingredient> result = retriever.findIngredientsByCriteria(
                ingredientName,
                category,
                dishName,
                page,
                size
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0,result.size() );
    }
    @Test
    void findIngredientsByCriteria_categoryNull() {
        DataRetriever retriever = new DataRetriever();

        String ingredientName = "cho";
        CategoryEnum category = null;
        String dishName = "gâteau";
        int page = 1;
        int size =10;

        List<Ingredient> result = retriever.findIngredientsByCriteria(
                ingredientName,
                category,
                dishName,
                page,
                size
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Chocolat", result.get(0).getName());
    }
    @Test
    void testCreateIngredients() {
        DataRetriever retriever = new DataRetriever();

        Dish dish = new Dish();
        dish.setId(1); // plat existant en base

        Ingredient fromage = new Ingredient(
                null,
                "Fromage",
                1200.0,
                CategoryEnum.DAIRY,
                dish
        );

        Ingredient oignon = new Ingredient(
                null,
                "Oignon",
                500.0,
                CategoryEnum.VEGETABLE,
                dish
        );

        List<Ingredient> result = retriever.createIngredients(
                List.of(fromage, oignon)
        );

        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(result.stream().anyMatch(i -> i.getName().equalsIgnoreCase("Fromage")));
        assertTrue(result.stream().anyMatch(i -> i.getName().equalsIgnoreCase("Oignon")));
    }
    @Test
    void testCreateIngredients_existingIngredient_shouldThrowException() {
        DataRetriever retriever = new DataRetriever();

        Dish dish = new Dish();

        Ingredient carotte = new Ingredient(
                null,
                "Carotte",
                2000.0,
                CategoryEnum.VEGETABLE,
                dish
        );

        Ingredient laitue = new Ingredient(
                null,
                "Laitue",
                2000.0,
                CategoryEnum.VEGETABLE,
                dish
        );

        assertThrows(RuntimeException.class, () -> {
            retriever.createIngredients(List.of(carotte, laitue));
        });
    }
    @Test
    void testSaveDish_createsDishWithIngredient() {
        DataRetriever retriever = new DataRetriever();

        Dish dishToSave = new Dish();
        dishToSave.setName("Soupe de légumes");
        dishToSave.setDishType(DishTypeEnum.START);
        dishToSave.setIngredients(List.of(
                new Ingredient(null, "Oignon", 500.0, CategoryEnum.VEGETABLE, null)
        ));

        Dish savedDish = retriever.saveDish(dishToSave);

        assertNotNull(savedDish);
        assertNotNull(savedDish.getId(), "Le plat doit avoir un ID généré");
        assertEquals("Soupe de légumes", savedDish.getName());
        assertEquals(DishTypeEnum.START, savedDish.getDishType());

        List<Ingredient> ingredients = savedDish.getIngredients();
        assertNotNull(ingredients);
        assertEquals(1, ingredients.size());

        Ingredient ing = ingredients.get(0);
        assertEquals("Oignon", ing.getName());
        assertEquals(CategoryEnum.VEGETABLE, ing.getCategory(), "La catégorie doit être VEGETABLE");
        assertEquals(savedDish, ing.getDish(), "L'ingrédient doit être lié au plat");
    }
    @Test
    void testSaveDish_updatesIngredients() {
        DataRetriever retriever = new DataRetriever();

        Dish existingDish = new Dish();
        existingDish.setId(1); // ID connu
        existingDish.setName("Salade fraîche");
        existingDish.setDishType(DishTypeEnum.START);

        existingDish.setIngredients(List.of(
                new Ingredient(null, "Laitue", 200.0, CategoryEnum.VEGETABLE, existingDish),
                new Ingredient(null, "Tomate", 300.0, CategoryEnum.VEGETABLE, existingDish)
        ));

        List<Ingredient> updatedIngredients = List.of(
                new Ingredient(null, "Laitue", 200.0, CategoryEnum.VEGETABLE, existingDish),
                new Ingredient(null, "Tomate", 300.0, CategoryEnum.VEGETABLE, existingDish),
                new Ingredient(null, "Oignon", 500.0, CategoryEnum.VEGETABLE, existingDish),
                new Ingredient(null, "Fromage", 1200.0, CategoryEnum.DAIRY, existingDish)
        );
        existingDish.setIngredients(updatedIngredients);

        Dish updatedDish = retriever.saveDish(existingDish);

        assertNotNull(updatedDish, "Le plat ne doit pas être null");
        assertEquals(1, updatedDish.getId(), "L'ID doit rester le même");
        assertEquals("Salade fraîche", updatedDish.getName());
        assertEquals(DishTypeEnum.START, updatedDish.getDishType());

        List<String> ingredientNames = updatedDish.getIngredients().stream()
                .map(Ingredient::getName)
                .toList();
        assertTrue(ingredientNames.containsAll(List.of("Laitue", "Tomate", "Oignon", "Fromage")),
                "Les ingrédients doivent être mis à jour correctement");

        assertEquals(4, updatedDish.getIngredients().size());
    }

}
