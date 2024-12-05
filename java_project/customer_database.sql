create database shop;
use shop;
create table customer(customer_id int(6) auto_increment, customer_name varchar(50), customer_password varchar(50), email varchar(100), 
address varchar(150), primary key (customer_id))auto_increment=100;

insert into customer(customer_name,customer_password,email,address) values('Jhon Mark','123456','jhon@cc.co','123 Street NY');
insert into customer(customer_name,customer_password,email,address) values('Rick Lin','a12354','rick@cc.co','3 Street IN');
insert into customer(customer_name,customer_password,email,address) values('Molly Merk','b123456','mm@cc.co','566 Street NJ');
insert into customer(customer_name,customer_password,email,address) values('Len Jonson','c123456','len@cc.co','123 Street DC');
insert into customer(customer_name,customer_password,email,address) values('Miller Job','d123456','mjob@cc.co','563 Street AZ');
insert into customer(customer_name,customer_password,email,address) values('Anny Ken','e123456','ken@cc.co','127 Street SD');