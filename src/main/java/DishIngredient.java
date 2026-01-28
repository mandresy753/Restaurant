import java.math.BigDecimal;

public class DishIngredient {
    private Integer id;
    private Dish dish;
    private Ingredient ingredient;
    private BigDecimal quantityRequired;
    private UnitType unit; // ENUM: PCS, KG, L

    // Constructeurs
    public DishIngredient() {}

    public DishIngredient(Dish dish, Ingredient ingredient, BigDecimal quantityRequired, UnitType unit) {
        this.dish = dish;
        this.ingredient = ingredient;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    // Getters et setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public BigDecimal getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(BigDecimal quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public UnitType getUnit() {
        return unit;
    }

    public void setUnit(UnitType unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "DishIngredient{" +
                "id=" + id +
                ", dish=" + (dish != null ? dish.getName() : "null") +
                ", ingredient=" + (ingredient != null ? ingredient.getName() : "null") +
                ", quantityRequired=" + quantityRequired +
                ", unit=" + unit +
                '}';
    }
}