import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                                null,
                                rsIng.getString("name"),
                                rsIng.getDouble("price"),
                                null,
                                null
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

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        String sql = """
        INSERT INTO ingredient(name, price, category, id_dish)
        VALUES (?, ?, ?::category, ?)
    """;

        Set<String> names = new HashSet<>();
        for (Ingredient ing : newIngredients) {
            if (!names.add(ing.getName())) {
                throw new RuntimeException("Doublon détecté dans la liste fournie : " + ing.getName());
            }
        }

        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Ingredient ing : newIngredients) {
                    if (ing.getDish() == null) {
                        throw new RuntimeException("Plat non défini pour : " + ing.getName());
                    }

                    String checkSql = "SELECT COUNT(*) FROM ingredient WHERE name = ? AND id_dish = ?";
                    try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                        psCheck.setString(1, ing.getName());
                        psCheck.setInt(2, ing.getDish().getId());
                        try (ResultSet rs = psCheck.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                throw new RuntimeException(
                                        "Ingrédient déjà existant pour ce plat : " + ing.getName()
                                );
                            }
                        }
                    }

                    ps.setString(1, ing.getName());
                    ps.setDouble(2, ing.getPrice());
                    if (ing.getCategory() != null) {
                        ps.setObject(3, ing.getCategory().name(), java.sql.Types.OTHER);
                    } else {
                        ps.setNull(3, java.sql.Types.OTHER);
                    }
                    ps.setInt(4, ing.getDish().getId());

                    ps.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erreur lors de l'insertion des ingrédients, rollback effectué", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion des ingrédients", e);
        }

        return newIngredients;
    }




}
