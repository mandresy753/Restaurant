insert into dish (name, dish_type) values
('Salade fraîche', 'START'),
('Poulet grillé', 'MAIN'),
('Riz aux légumes', 'MAIN'),
('Gâteau au chocolat', 'DESSERT'),
('Salade de fruits', 'DESSERT');


INSERT INTO ingredient (name, price, category) VALUES
    ('Laitue', 800.00, 'VEGETABLE'),
    ('Tomate', 600.00, 'VEGETABLE'),
    ('Poulet', 4500.00, 'ANIMAL'),
    ('Chocolat', 3000.00, 'OTHER'),
    ('Beurre', 2500.00, 'DAIRY');

INSERT INTO dish_ingredient (id, id_dish, id_ingredient, quantity_required, unit) VALUES
    (1, 1, 1, 0.20, 'KG'),
    (2, 1, 2, 0.15, 'KG'),
    (3, 2, 3, 1.00, 'KG'),
    (4, 4, 4, 0.30, 'KG'),
    (5, 4, 5, 0.20, 'KG');

INSERT INTO Dish (id, name, dish_type, selling_price) VALUES
    (1, 'Salade fraîche', 'START', 3500.00),
    (2, 'Poulet grillé', 'MAIN', 12000.00),
    (3, 'Riz aux légumes', 'MAIN', NULL),
    (4, 'Gâteau au chocolat', 'DESSERT', 8000.00),
    (5, 'Salade de fruits', 'DESSERT', NULL);


UPDATE Ingredient
SET required_quantity = 1
WHERE name = 'Laitue';

UPDATE Ingredient
SET required_quantity = 2
WHERE name = 'Tomate';

UPDATE Ingredient
SET required_quantity = 0.5
WHERE name = 'Poulet';

UPDATE Ingredient
SET required_quantity = NULL
WHERE name IN ('Chocolat', 'Beurre');