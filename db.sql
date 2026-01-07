create database mini_dish_db;
create user mini_dish_db_manager with password '1234560';
grant connect on database mini_dish_db to mini_dish_db_manager;
grant create on schema public to mini_dish_db_manager;
grant insert, select, update, delete on all tables in schema public to mini_dish_db_manager;
alter default privileges in schema public
grant insert, select, update, delete on tables to mini_dish_db_manager;

grant select, usage on sequence ingredient_id_seq TO mini_dish_db_manager; //for the serial type
grant select, usage on sequence dish_id_seq to mini_dish_db_manager; //for the serial type