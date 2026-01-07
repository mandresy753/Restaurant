create type dish_type as enum ('START', 'MAIN', 'DESSERT');
create table dish (
    id serial primary key,
    name varchar(255) not null,
    dish_type dish_type
);

create type category as enum ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
create table ingredient (
    id serial primary key,
    name varchar(255) not null,
    price numeric(10,2),
    category category,
    id_dish int references dish(id)

);

ALTER TABLE Ingredient
    ADD COLUMN IF NOT EXISTS required_quantity DECIMAL(10,2) NULL;