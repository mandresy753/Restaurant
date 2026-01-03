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

    public Dish saveDish(Dish dishToSave) {
        String insertDishSql = """
        INSERT INTO dish (name, dish_type)
        VALUES (?, ?::dish_type)
        RETURNING id
    """;

        String updateDishSql = """
        UPDATE dish SET name = ?, dish_type = ?::dish_type
        WHERE id = ?
    """;

        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);

            if (dishToSave.getId() == null) {
                String checkSql = "SELECT id FROM dish WHERE name = ?";
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setString(1, dishToSave.getName());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            dishToSave.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            if (dishToSave.getId() == null) {
                try (PreparedStatement psInsert = conn.prepareStatement(insertDishSql)) {
                    psInsert.setString(1, dishToSave.getName());
                    psInsert.setObject(2, dishToSave.getDishType().name(), java.sql.Types.OTHER);
                    try (ResultSet rs = psInsert.executeQuery()) {
                        if (rs.next()) {
                            dishToSave.setId(rs.getInt("id"));
                        }
                    }
                }
            } else {
                try (PreparedStatement psUpdate = conn.prepareStatement(updateDishSql)) {
                    psUpdate.setString(1, dishToSave.getName());
                    psUpdate.setObject(2, dishToSave.getDishType().name(), java.sql.Types.OTHER);
                    psUpdate.setInt(3, dishToSave.getId());
                    psUpdate.executeUpdate();
                }
            }

            Set<String> existingIngredientsNames = new HashSet<>();
            List<Ingredient> updatedIngredients = new ArrayList<>();

            String selectIng = "SELECT id, name, price, category FROM ingredient WHERE id_dish = ?";
            try (PreparedStatement psSelect = conn.prepareStatement(selectIng)) {
                psSelect.setInt(1, dishToSave.getId());
                try (ResultSet rs = psSelect.executeQuery()) {
                    while (rs.next()) {
                        Ingredient ing = new Ingredient();
                        ing.setId(rs.getInt("id"));
                        ing.setName(rs.getString("name"));
                        ing.setPrice(rs.getDouble("price"));
                        String categoryStr = rs.getString("category");
                        if (categoryStr != null) {
                            ing.setCategory(CategoryEnum.valueOf(categoryStr));
                        }
                        ing.setDish(dishToSave);
                        existingIngredientsNames.add(ing.getName());
                        updatedIngredients.add(ing);
                    }
                }
            }

            String insertIng = """
            INSERT INTO ingredient(name, price, category, id_dish)
            VALUES (?, ?, ?, ?)
        """;
            try (PreparedStatement psInsertIng = conn.prepareStatement(insertIng)) {
                for (Ingredient ing : dishToSave.getIngredients()) {
                    if (!existingIngredientsNames.contains(ing.getName())) {
                        psInsertIng.setString(1, ing.getName());
                        psInsertIng.setDouble(2, ing.getPrice());
                        psInsertIng.setObject(3, ing.getCategory().name(), java.sql.Types.OTHER);
                        psInsertIng.setInt(4, dishToSave.getId());
                        psInsertIng.addBatch();

                        // Ajout dans la liste locale pour garder à jour
                        ing.setDish(dishToSave);
                        updatedIngredients.add(ing);
                    }
                }
                psInsertIng.executeBatch();
            }

            String deleteIng = "DELETE FROM ingredient WHERE id_dish = ? AND name = ?";
            try (PreparedStatement psDeleteIng = conn.prepareStatement(deleteIng)) {
                for (Ingredient ing : updatedIngredients) {
                    existingIngredientsNames.remove(ing.getName());
                }
                for (String toDelete : existingIngredientsNames) {
                    psDeleteIng.setInt(1, dishToSave.getId());
                    psDeleteIng.setString(2, toDelete);
                    psDeleteIng.addBatch();
                }
                psDeleteIng.executeBatch();
            }

            dishToSave.setIngredients(updatedIngredients);

            conn.commit();
            return dishToSave;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du plat", e);
        }
    }





}
