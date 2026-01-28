import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class DataRetriever {

    public Dish findDishById(int id) {
        String dishSql = """
            SELECT id, name, dish_type, selling_price
            FROM dish
            WHERE id = ?
        """;

        String dishIngredientSql = """
            SELECT i.id, i.name, i.price, i.category,
                   di.quantity_required, di.unit
            FROM dish_ingredient di
            JOIN ingredient i ON di.id_ingredient = i.id
            WHERE di.id_dish = ?
        """;

        try (Connection conn = new DBConnection().getDBConnection()) {
            Dish dish;

            try (PreparedStatement psDish = conn.prepareStatement(dishSql)) {
                psDish.setInt(1, id);
                try (ResultSet rs = psDish.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Plat introuvable");
                    }
                    dish = new Dish(
                            rs.getInt("id"),
                            rs.getString("name"),
                            DishTypeEnum.valueOf(rs.getString("dish_type")),
                            rs.getBigDecimal("selling_price")
                    );
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(dishIngredientSql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Ingredient ing = new Ingredient(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getBigDecimal("price"),
                                rs.getString("category") != null ? CategoryEnum.valueOf(rs.getString("category")) : null,
                                null,
                                rs.getBigDecimal("quantity_required"),
                                rs.getString("unit") != null ? UnitType.valueOf(rs.getString("unit")) : null
                        );

                        DishIngredient di = new DishIngredient();
                        di.setDish(dish);
                        di.setIngredient(ing);
                        di.setQuantityRequired(ing.getRequiredQuantity());
                        di.setUnit(ing.getUnit());
                        dish.getDishIngredients().add(di);
                    }
                }
            }
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        String sql = """
            SELECT id, name, price, category
            FROM ingredient
            ORDER BY id
            LIMIT ? OFFSET ?
            """;
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, size);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String categoryStr = rs.getString("category");
                    CategoryEnum category = (categoryStr != null) ? CategoryEnum.valueOf(categoryStr) : null;

                    Ingredient ing = new Ingredient(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getBigDecimal("price"),
                            category,
                            null,
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
        String ingredientSql = "INSERT INTO ingredient(name, price, category) VALUES (?, ?, ?::category) RETURNING id";
        String dishIngredientSql = """
            INSERT INTO dish_ingredient(id_dish, id_ingredient, quantity_required, unit) 
            VALUES (?, ?, ?, ?::unit_type)
        """;

        Set<String> names = new HashSet<>();
        for (Ingredient ing : newIngredients) {
            if (!names.add(ing.getName())) {
                throw new RuntimeException("Doublon détecté : " + ing.getName());
            }
        }

        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);

            synchronizeSequences(conn);

            for (Ingredient ing : newIngredients) {
                if (ing.getDish() == null) {
                    throw new RuntimeException("Plat non défini pour : " + ing.getName());
                }

                int ingredientId;
                try (PreparedStatement psIng = conn.prepareStatement(ingredientSql)) {
                    psIng.setString(1, ing.getName());
                    psIng.setBigDecimal(2, ing.getPrice());
                    if (ing.getCategory() != null) {
                        psIng.setObject(3, ing.getCategory().name(), Types.OTHER);
                    } else {
                        psIng.setNull(3, Types.OTHER);
                    }

                    try (ResultSet rs = psIng.executeQuery()) {
                        rs.next();
                        ingredientId = rs.getInt(1);
                        ing.setId(ingredientId);
                    }
                }

                try (PreparedStatement psDishIng = conn.prepareStatement(dishIngredientSql)) {
                    psDishIng.setInt(1, ing.getDish().getId());
                    psDishIng.setInt(2, ingredientId);
                    psDishIng.setBigDecimal(3, ing.getRequiredQuantity());
                    if (ing.getUnit() != null) {
                        psDishIng.setObject(4, ing.getUnit().name(), Types.OTHER);
                    } else {
                        psDishIng.setNull(4, Types.OTHER);
                    }
                    psDishIng.executeUpdate();
                }
            }

            conn.commit();
            return newIngredients;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion des ingrédients", e);
        }
    }

    public Dish saveDish(Dish dish) {
        String insertDish = """
            INSERT INTO dish(name, dish_type, selling_price)
            VALUES (?, ?, ?)
            RETURNING id
        """;

        String updateDish = """
            UPDATE dish SET name = ?, dish_type = ?, selling_price = ?
            WHERE id = ?
        """;

        String selectDishIngredients = """
            SELECT di.id, i.id AS ingredient_id
            FROM dish_ingredient di
            JOIN ingredient i ON di.id_ingredient = i.id
            WHERE di.id_dish = ?
        """;

        String insertIngredient = """
            INSERT INTO ingredient(name, price, category)
            VALUES (?, ?, ?::category)
            RETURNING id
        """;

        String updateIngredient = """
            UPDATE ingredient SET price = ?, category = ?::category WHERE id = ?
        """;

        String insertDishIngredient = """
            INSERT INTO dish_ingredient(id_dish, id_ingredient, quantity_required, unit)
            VALUES (?, ?, ?, ?::unit_type)
            RETURNING id
        """;

        String updateDishIngredient = """
            UPDATE dish_ingredient
            SET quantity_required = ?, unit = ?::unit_type
            WHERE id_dish = ? AND id_ingredient = ?
        """;

        String deleteDishIngredient = """
            DELETE FROM dish_ingredient WHERE id_dish = ? AND id_ingredient = ?
        """;

        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);

            if (dish.getId() == null) {
                try (PreparedStatement ps = conn.prepareStatement(insertDish)) {
                    ps.setString(1, dish.getName());
                    ps.setObject(2, dish.getDishType().name(), Types.OTHER);
                    ps.setBigDecimal(3, dish.getSellingPrice());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        dish.setId(rs.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(updateDish)) {
                    ps.setString(1, dish.getName());
                    ps.setObject(2, dish.getDishType().name(), Types.OTHER);
                    ps.setBigDecimal(3, dish.getSellingPrice());
                    ps.setInt(4, dish.getId());
                    ps.executeUpdate();
                }
            }

            Map<Integer, Integer> existing = new HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(selectDishIngredients)) {
                ps.setInt(1, dish.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        existing.put(rs.getInt("ingredient_id"), rs.getInt("id"));
                    }
                }
            }

            for (DishIngredient di : dish.getDishIngredients()) {
                Ingredient ing = di.getIngredient();

                if (ing.getId() == null) {
                    try (PreparedStatement ps = conn.prepareStatement(insertIngredient)) {
                        ps.setString(1, ing.getName());
                        ps.setBigDecimal(2, ing.getPrice());
                        ps.setObject(3, ing.getCategory() != null ? ing.getCategory().name() : null, Types.OTHER);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            ing.setId(rs.getInt(1));
                        }
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(updateIngredient)) {
                        ps.setBigDecimal(1, ing.getPrice());
                        ps.setObject(2, ing.getCategory() != null ? ing.getCategory().name() : null, Types.OTHER);
                        ps.setInt(3, ing.getId());
                        ps.executeUpdate();
                    }
                }

                if (existing.containsKey(ing.getId())) {
                    try (PreparedStatement ps = conn.prepareStatement(updateDishIngredient)) {
                        ps.setBigDecimal(1, di.getQuantityRequired());
                        ps.setObject(2, di.getUnit() != null ? di.getUnit().name() : null, Types.OTHER);
                        ps.setInt(3, dish.getId());
                        ps.setInt(4, ing.getId());
                        ps.executeUpdate();
                    }
                    existing.remove(ing.getId());
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(insertDishIngredient)) {
                        ps.setInt(1, dish.getId());
                        ps.setInt(2, ing.getId());
                        ps.setBigDecimal(3, di.getQuantityRequired());
                        ps.setObject(4, di.getUnit() != null ? di.getUnit().name() : null, Types.OTHER);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            di.setId(rs.getInt(1));
                        }
                    }
                }
                di.setDish(dish);
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteDishIngredient)) {
                for (Integer ingredientId : existing.keySet()) {
                    ps.setInt(1, dish.getId());
                    ps.setInt(2, ingredientId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int getLastInsertId(Connection conn, String sequenceName) throws SQLException {
        String sql = "SELECT currval(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            String fallbackSql = "SELECT MAX(id) FROM " + sequenceName.replace("_id_seq", "");
            try (PreparedStatement ps = conn.prepareStatement(fallbackSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Impossible de récupérer le dernier ID inséré pour la séquence: " + sequenceName);
    }

    public List<Dish> findDishsByIngredientName(String IngredientName) {
        String sql = """
            SELECT DISTINCT d.id, d.name, d.dish_type, d.selling_price 
            FROM dish d
            INNER JOIN dish_ingredient di ON d.id = di.id_dish
            INNER JOIN ingredient i ON di.id_ingredient = i.id
            WHERE i.name ILIKE ?
            """;

        List<Dish> dishes = new ArrayList<>();
        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + IngredientName + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal sellingPrice = rs.getBigDecimal("selling_price");
                    if (rs.wasNull()) {
                        sellingPrice = null;
                    }

                    Dish dish = new Dish(
                            rs.getInt("id"),
                            rs.getString("name"),
                            DishTypeEnum.valueOf(rs.getString("dish_type")),
                            sellingPrice
                    );

                    List<DishIngredient> dishIngredients = findDishIngredientsByDishId(dish.getId());
                    dish.setDishIngredients(dishIngredients);

                    dishes.add(dish);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des plats par ingrédient", e);
        }
        return dishes;
    }

    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            CategoryEnum category,
            String dishName,
            int page,
            int size,
            BigDecimal requiredQuantity
    ) {
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        StringBuilder sql = new StringBuilder("""
        SELECT i.id, i.name, i.price, i.category, 
               d.id as dish_id, d.name as dish_name, d.dish_type, d.selling_price,
               di.quantity_required, di.unit
        FROM ingredient i
        INNER JOIN dish_ingredient di ON i.id = di.id_ingredient
        INNER JOIN dish d ON di.id_dish = d.id
        WHERE 1=1
    """);

        if (ingredientName != null && !ingredientName.isBlank()) {
            sql.append(" AND i.name ILIKE ? ");
        }

        if (category != null) {
            sql.append(" AND i.category = ?::category ");
        }

        if (dishName != null && !dishName.isBlank()) {
            sql.append(" AND d.name ILIKE ? ");
        }

        if (requiredQuantity != null) {
            sql.append(" AND di.quantity_required = ? ");
        }

        sql.append(" ORDER BY i.id LIMIT ? OFFSET ?");

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (ingredientName != null && !ingredientName.isBlank()) {
                ps.setString(index++, "%" + ingredientName + "%");
            }

            if (category != null) {
                ps.setObject(index++, category.name(), java.sql.Types.OTHER);
            }

            if (dishName != null && !dishName.isBlank()) {
                ps.setString(index++, "%" + dishName + "%");
            }

            if (requiredQuantity != null) {
                ps.setBigDecimal(index++, requiredQuantity);
            }

            ps.setInt(index++, size);
            ps.setInt(index, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal sellingPrice = rs.getBigDecimal("selling_price");
                    if (rs.wasNull()) {
                        sellingPrice = null;
                    }

                    Dish dish = new Dish(
                            rs.getInt("dish_id"),
                            rs.getString("dish_name"),
                            DishTypeEnum.valueOf(rs.getString("dish_type")),
                            sellingPrice
                    );

                    String categoryStr = rs.getString("category");
                    CategoryEnum cat = categoryStr != null ? CategoryEnum.valueOf(categoryStr) : null;

                    String unitStr = rs.getString("unit");
                    UnitType unit = unitStr != null ? UnitType.valueOf(unitStr) : null;

                    Ingredient ing = new Ingredient(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getBigDecimal("price"),
                            cat,
                            null,
                            rs.getBigDecimal("quantity_required"),
                            unit
                    );

                    ingredients.add(ing);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des ingrédients par critères", e);
        }
        return ingredients;
    }

    private void synchronizeSequences(Connection conn) throws SQLException {
        String[] queries = {
                "SELECT setval('dish_id_seq', COALESCE((SELECT MAX(id) FROM dish), 0) + 1, false)",
                "SELECT setval('ingredient_id_seq', COALESCE((SELECT MAX(id) FROM ingredient), 0) + 1, false)",
                "SELECT setval('dish_ingredient_id_seq', COALESCE((SELECT MAX(id) FROM dish_ingredient), 0) + 1, false)"
        };
        for (String query : queries) {
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.execute();
            }
        }
    }

    public int getNextSequenceValue(String sequenceName) {
        try {
            String sql = "SELECT nextval(?)";
            try (Connection conn = new DBConnection().getDBConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sequenceName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Impossible d'accéder à la séquence " + sequenceName + ", utilisation de l'approche alternative: " + e.getMessage());

            String tableName = sequenceName.replace("_id_seq", "");
            String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM " + tableName;
            try (Connection conn = new DBConnection().getDBConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException ex) {
                throw new RuntimeException("Impossible de récupérer la valeur de la séquence: " + sequenceName, ex);
            }
        }
        throw new RuntimeException("Impossible de récupérer la valeur de la séquence: " + sequenceName);
    }

    public List<DishIngredient> findDishIngredientsByDishId(Integer dishId) {
        List<DishIngredient> dishIngredients = new ArrayList<>();
        String sql = """
            SELECT di.id, di.quantity_required, di.unit,
                   i.id as ingredient_id, i.name as ingredient_name, i.price, i.category,
                   d.id as dish_id, d.name as dish_name, d.dish_type, d.selling_price
            FROM dish_ingredient di
            JOIN ingredient i ON di.id_ingredient = i.id
            JOIN dish d ON di.id_dish = d.id
            WHERE di.id_dish = ?
            """;

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal sellingPrice = rs.getBigDecimal("selling_price");
                    if (rs.wasNull()) {
                        sellingPrice = null;
                    }

                    Dish dish = new Dish(
                            rs.getInt("dish_id"),
                            rs.getString("dish_name"),
                            DishTypeEnum.valueOf(rs.getString("dish_type")),
                            sellingPrice
                    );

                    String categoryStr = rs.getString("category");
                    CategoryEnum category = categoryStr != null ? CategoryEnum.valueOf(categoryStr) : null;

                    Ingredient ingredient = new Ingredient(
                            rs.getInt("ingredient_id"),
                            rs.getString("ingredient_name"),
                            rs.getBigDecimal("price"),
                            category,
                            null,
                            null,
                            null
                    );

                    String unitStr = rs.getString("unit");
                    UnitType unit = unitStr != null ? UnitType.valueOf(unitStr) : null;

                    DishIngredient dishIngredient = new DishIngredient();
                    dishIngredient.setId(rs.getInt("id"));
                    dishIngredient.setDish(dish);
                    dishIngredient.setIngredient(ingredient);
                    dishIngredient.setQuantityRequired(rs.getBigDecimal("quantity_required"));
                    dishIngredient.setUnit(unit);

                    dishIngredients.add(dishIngredient);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des DishIngredient par dishId", e);
        }
        return dishIngredients;
    }

    public void saveDishIngredient(DishIngredient dishIngredient) {
        String insertSql = """
        INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
        VALUES (?, ?, ?, ?::unit_type)
        RETURNING id
    """;

        String updateSql = """
        UPDATE dish_ingredient
        SET quantity_required = ?, unit = ?::unit_type
        WHERE id_dish = ? AND id_ingredient = ?
    """;

        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);

            String checkSql = "SELECT id FROM dish_ingredient WHERE id_dish = ? AND id_ingredient = ?";
            Integer existingId = null;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, dishIngredient.getDish().getId());
                psCheck.setInt(2, dishIngredient.getIngredient().getId());
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getInt("id");
                    }
                }
            }

            if (existingId != null) {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setBigDecimal(1, dishIngredient.getQuantityRequired());
                    ps.setObject(2, dishIngredient.getUnit() != null ? dishIngredient.getUnit().name() : null, java.sql.Types.OTHER);
                    ps.setInt(3, dishIngredient.getDish().getId());
                    ps.setInt(4, dishIngredient.getIngredient().getId());
                    ps.executeUpdate();
                }
                dishIngredient.setId(existingId);
            } else {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, dishIngredient.getDish().getId());
                    ps.setInt(2, dishIngredient.getIngredient().getId());
                    ps.setBigDecimal(3, dishIngredient.getQuantityRequired());
                    ps.setObject(4, dishIngredient.getUnit() != null ? dishIngredient.getUnit().name() : null, java.sql.Types.OTHER);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        dishIngredient.setId(rs.getInt(1));
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du DishIngredient", e);
        }
    }


    public DishIngredient findDishIngredientById(Integer id) {
        String sql = """
            SELECT di.id, di.quantity_required, di.unit,
                   i.id as ingredient_id, i.name as ingredient_name, i.price, i.category,
                   d.id as dish_id, d.name as dish_name, d.dish_type, d.selling_price
            FROM dish_ingredient di
            JOIN ingredient i ON di.id_ingredient = i.id
            JOIN dish d ON di.id_dish = d.id
            WHERE di.id = ?
            """;

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal sellingPrice = rs.getBigDecimal("selling_price");
                    if (rs.wasNull()) {
                        sellingPrice = null;
                    }

                    Dish dish = new Dish(
                            rs.getInt("dish_id"),
                            rs.getString("dish_name"),
                            DishTypeEnum.valueOf(rs.getString("dish_type")),
                            sellingPrice
                    );

                    String categoryStr = rs.getString("category");
                    CategoryEnum category = categoryStr != null ? CategoryEnum.valueOf(categoryStr) : null;

                    Ingredient ingredient = new Ingredient(
                            rs.getInt("ingredient_id"),
                            rs.getString("ingredient_name"),
                            rs.getBigDecimal("price"),
                            category,
                            null,
                            null,
                            null
                    );

                    String unitStr = rs.getString("unit");
                    UnitType unit = unitStr != null ? UnitType.valueOf(unitStr) : null;

                    DishIngredient dishIngredient = new DishIngredient();
                    dishIngredient.setId(rs.getInt("id"));
                    dishIngredient.setDish(dish);
                    dishIngredient.setIngredient(ingredient);
                    dishIngredient.setQuantityRequired(rs.getBigDecimal("quantity_required"));
                    dishIngredient.setUnit(unit);

                    return dishIngredient;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du DishIngredient par id", e);
        }
        return null;
    }

    public Ingredient saveIngredient(Ingredient toSave) {
        String insertIngredientSql = """
            INSERT INTO ingredient(name, price, category)
            VALUES (?, ?, ?::category) on conflict(id)  do update 
                set name = excluded.name,
                category = excluded.category,
                price = excluded.price
            RETURNING id
        """;

        String updateIngredientSql = """
            UPDATE ingredient
            SET name = ?, price = ?, category = ?::category
            WHERE id = ?
        """;

        String insertStockMovementSql = """
            INSERT INTO stock_movement(id_ingredient, quantity, type, unit, creation_datetime)
            VALUES (?, ?, ?::movement_type, ?::unit_type, ?)
            ON CONFLICT (id) DO NOTHING
            RETURNING id
        """;

        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);

            // 1️⃣ Sauvegarde ou mise à jour de l'ingrédient
            if (toSave.getId() == null) {
                try (PreparedStatement ps = conn.prepareStatement(insertIngredientSql)) {
                    ps.setString(1, toSave.getName());
                    ps.setBigDecimal(2, toSave.getPrice());
                    ps.setObject(3, toSave.getCategory() != null ?
                            toSave.getCategory().name() : null, Types.OTHER);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            toSave.setId(rs.getInt(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(updateIngredientSql)) {
                    ps.setString(1, toSave.getName());
                    ps.setBigDecimal(2, toSave.getPrice());
                    ps.setObject(3, toSave.getCategory() != null ?
                            toSave.getCategory().name() : null, Types.OTHER);
                    ps.setInt(4, toSave.getId());
                    ps.executeUpdate();
                }
            }

            // 2️⃣ Sauvegarde des mouvements de stock
            if (toSave.getStockMovementList() != null) {
                for (StockMovement sm : toSave.getStockMovementList()) {
                    try (PreparedStatement ps = conn.prepareStatement(insertStockMovementSql)) {
                        ps.setInt(1, toSave.getId());
                        ps.setDouble(2, sm.getValue().getQuantity());
                        ps.setString(3, sm.getType().name());
                        ps.setString(4, sm.getValue().getUnit().name());
                        ps.setTimestamp(5, Timestamp.from(sm.getCreationDateTime()));

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                sm.setId(rs.getInt(1)); // récupère l'id si insertion effectuée
                            }
                        }
                    }
                }
            }

            conn.commit();
            return toSave;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de l'ingrédient", e);
        }
    }
    private void insertIngredientStockMovements(Connection conn, Ingredient ingredient) {

    }
}