create type dish_type as enum ('START', 'MAIN', 'DESSERT');
create table dish (
    id serial primary key,
    name varchar(255) not null unique,
    dish_type dish_type,
    selling_price numeric(10,2)
);


create type category as enum ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
    create table ingredient (
    id serial primary key,
    name varchar(255) not null unique,
    price numeric(10,2),
    category category

);

create type unit_type as enum ('PCS', 'KG', 'L');

create table dish_ingredient (
    id serial primary key,
    id_dish int references dish(id),
    id_ingredient int references ingredient(id),
    quantity_required numeric(10,2),
    unit unit_type
);

create type mouvement_type as enum ('IN', 'OUT');
create table stock_movement (
    id serial primary key,
    id_ingredient int references ingredient(id),
    quantity numeric(10,2),
    type mouvement_type,
    unit unit_type,
    creation_datetmie timestamp default now()
);



GRANT USAGE, SELECT, UPDATE ON SEQUENCE dish_id_seq TO mini_dish_db_manager;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE dish_ingredient_id_seq TO mini_dish_db_manager;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE ingredient_id_seq TO mini_dish_db_manager;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE stock_movement_id_seq TO mini_dish_db_manager;