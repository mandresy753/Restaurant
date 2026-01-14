import java.util.ArrayList;
import java.util.List;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private List<Ingredient> ingredients;

    public Dish(Integer id, String name, DishTypeEnum dishType) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = new ArrayList<>(); // initialisation vide
    }
    public Dish(){}

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

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public double getDishCost() {
        return ingredients.stream()
                .mapToDouble(ing -> {
                    if (ing.getRequiredQuantity() == null) {
                        throw new RuntimeException(
                                "Impossible de calculer le coût : quantité nécessaire inconnue pour l'ingrédient " + ing.getName()
                        );
                    }
                    return ing.getPrice() * ing.getRequiredQuantity();
                })
                .sum();
    }



    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", ingredients=" + ingredients +
                ", costs" + getDishCost() +
                '}';
    }
}
