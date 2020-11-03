create table if not exists post (
   id serial primary key not null,
   name varchar(1024),
   text text,
   link varchar(1024) UNIQUE,
   authorLink varchar(1024),
   created timestamp
);