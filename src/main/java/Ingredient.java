import java.math.BigDecimal;
import java.time.Instant;

public class Ingredient {
    private Integer id;
    private String name;
    private BigDecimal price;
    private CategoryEnum category;
    private Dish dish;
    private BigDecimal requiredQuantity;
    private UnitType unit;
    public StockValue getStockValueAt(Instant time){
        return null;
    }


    public Ingredient(Integer id, String name, BigDecimal price, CategoryEnum category, Dish dish, BigDecimal requiredQuantity, UnitType unit) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.dish = dish;
        this.requiredQuantity = requiredQuantity;
        this.unit = unit;
    }

    public Ingredient() {}

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public Dish getDish() {
        return dish;
    }

    public BigDecimal getRequiredQuantity() {
        return requiredQuantity;
    }

    public UnitType getUnit() {
        return unit;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public void setRequiredQuantity(BigDecimal requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public void setUnit(UnitType unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                ", dish=" + (dish != null ? dish.getName() : null) +
                ", requiredQuantity=" + requiredQuantity +
                ", unit=" + unit +
                '}';
    }
}