import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        /*DataRetriever retriever = new DataRetriever();

        // =========================
        // 1️⃣ Salade fraîche (id=1)
        // =========================
        Dish salade = retriever.findDishById(1);

        for (Ingredient ing : salade.getIngredients()) {
            switch (ing.getName()) {
                case "Laitue" -> ing.setRequiredQuantity(1.0);
                case "Tomate" -> ing.setRequiredQuantity(2.0);
            }
        }

        retriever.saveDish(salade);

        // =========================
        // 2️⃣ Poulet grillé (id=2)
        // =========================
        Dish pouletGrille = retriever.findDishById(2);

        for (Ingredient ing : pouletGrille.getIngredients()) {
            if (ing.getName().equals("Poulet")) {
                ing.setRequiredQuantity(0.5);
            }
        }

        retriever.saveDish(pouletGrille);

        // =========================
        // 3️⃣ Gâteau au chocolat (id=4)
        // =========================
        Dish gateauChoco = retriever.findDishById(4);

        for (Ingredient ing : gateauChoco.getIngredients()) {
            if (ing.getName().equals("Chocolat") || ing.getName().equals("Beurre")) {
                ing.setRequiredQuantity(null);
            }
        }

        retriever.saveDish(gateauChoco);

        // =========================
        // Affichage des résultats
        // =========================
        List<Dish> plats = List.of(salade, pouletGrille, gateauChoco);
        System.out.println("=== Quantités mises à jour ===");
        for (Dish d : plats) {
            System.out.println(d.getName() + " :");
            for (Ingredient ing : d.getIngredients()) {
                System.out.println("- " + ing.getName() + " : " + (ing.getRequiredQuantity() != null ? ing.getRequiredQuantity() : "nulle"));
            }
        }*/
        DataRetriever retriever = new DataRetriever();
        Dish salade = retriever.findDishById(1); // Salade fraîche
        try {
            double cost = salade.getDishCost();
            System.out.println("Coût total : " + cost);
        } catch (RuntimeException e) {
            System.out.println("Erreur : " + e.getMessage());
        }

    }
}
