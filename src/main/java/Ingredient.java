import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Ingredient {
    private Integer id;
    private String name;
    private BigDecimal price;
    private CategoryEnum category;
    private Dish dish;
    private BigDecimal requiredQuantity;
    private UnitType unit;
    private List<StockMovement> stockMovementList;

    public Ingredient(Integer id, String name, BigDecimal price, CategoryEnum category,
                      Dish dish, BigDecimal requiredQuantity, UnitType unit) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.dish = dish;
        this.requiredQuantity = requiredQuantity;
        this.unit = unit;
        this.stockMovementList = new ArrayList<>();
    }

    public Ingredient() {
        this.stockMovementList = new ArrayList<>();
    }

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

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
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

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    public void addStockMovement(StockMovement sm) {
        if (this.stockMovementList == null) {
            this.stockMovementList = new ArrayList<>();
        }
        this.stockMovementList.add(sm);
    }

    public StockValue getStockValueAt(Instant instant) {
        double quantity = 0.0;

        for (StockMovement sm : stockMovementList) {
            if (!sm.getCreationDateTime().isAfter(instant)) {
                if (sm.getType() == MovementType.IN) {
                    quantity += sm.getValue().getQuantity();
                } else if (sm.getType() == MovementType.OUT) {
                    quantity -= sm.getValue().getQuantity();
                }
            }
        }

        return new StockValue(quantity, UnitType.KG);
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
                ", stockMovementList=" + stockMovementList +
                '}';
    }
}