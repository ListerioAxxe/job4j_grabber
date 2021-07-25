create table post (
   id serial primary key,
   link varchar(200) not null unique,
   title varchar(200) not null,
   date timestamp,
   description text not null
)