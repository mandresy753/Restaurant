import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    public Dish findDishById(int id) {
        String dishSql = """
            SELECT id, name, dish_type
            FROM dish
            WHERE id = ?
        """;

        String ingredientSql = """
            SELECT name, price
            FROM ingredient
            WHERE id_dish = ?
        """;

        try (Connection conn = new DBConnection().getDBConnection()) {

            Dish dish = null;
            try (PreparedStatement psDish = conn.prepareStatement(dishSql)) {
                psDish.setInt(1, id);
                try (ResultSet rsDish = psDish.executeQuery()) {
                    if (rsDish.next()) {
                        dish = new Dish(
                                rsDish.getInt("id"),
                                rsDish.getString("name"),
                                DishTypeEnum.valueOf(rsDish.getString("dish_type"))
                        );
                    } else {
                        return null;
                    }
                }
            }

            try (PreparedStatement psIng = conn.prepareStatement(ingredientSql)) {
                psIng.setInt(1, id);
                try (ResultSet rsIng = psIng.executeQuery()) {
                    while (rsIng.next()) {
                        Ingredient ing = new Ingredient(
                                null,                   // id non utilisé
                                rsIng.getString("name"), // nom
                                rsIng.getDouble("price"),// prix
                                null,                   // category non utilisé
                                null                    // dish non utilisé
                        );
                        dish.getIngredients().add(ing);
                    }
                }
            }

            return dish;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du findDishById", e);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        String sql = """
                select name, price
                from ingredient
                order by id
                limit ? offset ?
                """;
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, size);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingredient ing = new Ingredient(
                            null,
                            rs.getString("name"),
                            rs.getDouble("price"),
                            null,
                            null
                    );
                    ingredients.add(ing);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération paginée des ingrédients", e);
        }

        return ingredients;
    }
}
