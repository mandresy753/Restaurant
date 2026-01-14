public class Ingredient {
    private Integer id;
    private String name;
    private double price;
    private CategoryEnum category;
    private Dish dish;
    private Double requiredQuantity;

    public Ingredient(Integer id, String name, double price, CategoryEnum category, Dish dish, Double requiredQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.dish = dish;
        this.requiredQuantity = requiredQuantity;
    }
    public Ingredient(String name, double price, CategoryEnum category, Dish dish, Double requiredQuantity) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.dish = dish;
        this.requiredQuantity = requiredQuantity;
    }

    public Ingredient() {}
    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    public CategoryEnum getCategory() {
        return category;
    }
    public Dish getDish() {
        return dish;
    }
    public Double getRequiredQuantity() { return requiredQuantity; }

    public void setId(Integer id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setPrice(double price) {this.price = price;}
    public void setCategory(CategoryEnum category) {this.category = category;}
    public void setDish(Dish dish) {this.dish = dish;}
    public void setRequiredQuantity(Double requiredQuantity) {this.requiredQuantity = requiredQuantity;}

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                ", dish=" + (dish != null ? dish.getName() : null) +
                ", requiredQuantity=" + requiredQuantity +
                '}';
    }
}
