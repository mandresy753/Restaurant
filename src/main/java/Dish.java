import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private BigDecimal sellingPrice;
    private List<DishIngredient> dishIngredients; // Changé de List<Ingredient> à List<DishIngredient>

    public Dish(Integer id, String name, DishTypeEnum dishType, BigDecimal sellingPrice) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.sellingPrice = sellingPrice;
        this.dishIngredients = new ArrayList<>(); // Initialisé avec dishIngredients
    }

    public Dish() {
        this.dishIngredients = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    // REMPLACER les méthodes getIngredients() et setIngredients()
    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        this.dishIngredients = dishIngredients;
    }

    // Méthode pour récupérer seulement les ingrédients (si nécessaire pour compatibilité)
    public List<Ingredient> getIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        if (dishIngredients != null) {
            for (DishIngredient dishIngredient : dishIngredients) {
                if (dishIngredient.getIngredient() != null) {
                    ingredients.add(dishIngredient.getIngredient());
                }
            }
        }
        return ingredients;
    }

    // Mettre à jour getDishCost() selon les exigences
    public BigDecimal getDishCost() {
        BigDecimal totalCost = BigDecimal.ZERO;

        if (dishIngredients == null || dishIngredients.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        for (DishIngredient dishIngredient : dishIngredients) {
            if (dishIngredient.getQuantityRequired() != null &&
                    dishIngredient.getIngredient() != null &&
                    dishIngredient.getIngredient().getPrice() != null) {

                // Multiplier le prix unitaire de l'ingrédient par la quantité requise
                BigDecimal itemCost = dishIngredient.getIngredient().getPrice()
                        .multiply(dishIngredient.getQuantityRequired());
                totalCost = totalCost.add(itemCost);
            }
        }

        return totalCost.setScale(2, RoundingMode.HALF_UP);
    }

    // Ajouter la méthode getGrossMargin()
    public BigDecimal getGrossMargin() {
        if (sellingPrice == null) {
            throw new IllegalStateException(
                    "Le prix de vente n'est pas défini pour le plat : " + name
            );
        }

        BigDecimal cost = getDishCost();
        return sellingPrice.subtract(cost).setScale(2, RoundingMode.HALF_UP);
    }

    // Méthode utilitaire pour ajouter un ingrédient avec sa quantité
    public void addIngredient(Ingredient ingredient, BigDecimal quantityRequired, UnitType unit) {
        if (ingredient == null || quantityRequired == null || unit == null) {
            throw new IllegalArgumentException("Tous les paramètres doivent être non nuls");
        }

        DishIngredient dishIngredient = new DishIngredient();
        dishIngredient.setDish(this);
        dishIngredient.setIngredient(ingredient);
        dishIngredient.setQuantityRequired(quantityRequired);
        dishIngredient.setUnit(unit);

        if (dishIngredients == null) {
            dishIngredients = new ArrayList<>();
        }

        dishIngredients.add(dishIngredient);
    }

    @Override
    public String toString() {
        BigDecimal cost = getDishCost();
        String marginInfo;

        try {
            marginInfo = ", marge=" + getGrossMargin();
        } catch (IllegalStateException e) {
            marginInfo = ", marge=EXCEPTION (prix null)";
        }

        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", sellingPrice=" + sellingPrice +
                ", dishIngredientsCount=" + (dishIngredients != null ? dishIngredients.size() : 0) +
                ", cost=" + cost +
                marginInfo +
                '}';
    }
}