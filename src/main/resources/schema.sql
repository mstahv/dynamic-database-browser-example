DROP TABLE customers_test IF EXISTS;

CREATE TABLE customers_test (id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255));

INSERT INTO customers_test (first_name, last_name) VALUES ('Matti' ,'Tahvonen');
INSERT INTO customers_test (first_name, last_name) VALUES ('Jorma' ,'Jormalainen');
INSERT INTO customers_test (first_name, last_name) VALUES ('Mikko' ,'Mikkolainen');