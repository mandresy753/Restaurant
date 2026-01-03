public class Ingredient {
    private Integer id;
    private String name;
    private double price;
    private CategoryEnum category;
    private Dish dish;

    public Ingredient(Integer id, String name, double price, CategoryEnum category, Dish dish) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.dish = dish;
    }
    public Ingredient(String name, double price, CategoryEnum category, Dish dish) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.dish = dish;
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

    public void setId(Integer id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setPrice(double price) {this.price = price;}
    public void setCategory(CategoryEnum category) {this.category = category;}
    public void setDish(Dish dish) {this.dish = dish;}

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                ", dish=" + (dish != null ? dish.getName() : null) +
                '}';
    }
}
