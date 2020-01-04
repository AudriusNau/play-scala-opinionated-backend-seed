# --- !Ups

create table "movies" (
   "id" bigserial primary key,
   "title" varchar not null,
    "description" varchar not null,
   "releaseDate" DATE,
    "country" varchar,
    "language" varchar
);
create table "directors" (
    "id" bigserial primary key,
    "firstName" varchar not null,
    "lastName" varchar not null,
    "dateOfBirth" DATE,
    "nationality" varchar not null,
    "height" int,
    "gender" varchar not null
);


create table "actors" (
   "id" bigserial primary key,
   "firstName" varchar not null,
   "lastName" varchar not null,
   "dateOfBirth" DATE,
   "nationality" varchar not null,
   "height" int,
   "gender" varchar (1) not null
);

create table "genres" (
  "id" bigserial primary key,
  "title" varchar not null
);

insert into "genres" ("title") values ('comedy');
insert into "genres" ("title") values ('romance');
insert into "genres" ("title") values ('horror');
insert into "genres" ("title") values ('drama');
insert into "genres" ("title") values ('action');

insert into "actors" ("firstName", "lastName", "dateOfBirth", "nationality", "height", "gender")
	values ('John', 'Makinli', '1940-12-15', 'PL', '190', 'm');
insert into "actors" ("firstName", "lastName", "dateOfBirth", "nationality", "height", "gender")
	values ('Rovan', 'Atkinson', '1981-10-27', 'PL', '174', 'm');
insert into "actors" ("firstName", "lastName", "dateOfBirth", "nationality", "height", "gender")
	values ('Siegel', 'Makamas', '2002-09-01', 'LT', '161', 'f');
insert into "actors" ("firstName", "lastName", "dateOfBirth", "nationality", "height", "gender")
	values ('Kevin', 'Rodriguez', '1964-01-16', 'AU', '185', 'm');
insert into "actors" ("firstName", "lastName", "dateOfBirth", "nationality", "height", "gender")
	values ('Clementine', 'Johannes', '1989-04-18', 'PL', '178', 'f');
	insert into "actors" ("firstName", "lastName", "dateOfBirth", "nationality", "height", "gender")
	values ('Mandarin', 'Hannes', '1989-04-18', 'LT', '203', 'm');

insert into "directors" ("firstName", "lastName", "dateOfBirth", "nationality", "height", "gender")
	values ('Kevin', 'Rodriguez', '1964-01-16', 'AU', '185', 'm');
insert into "directors" ("firstName", "lastName", "dateOfBirth", "nationality", "height", "gender")
	values ('Clementine', 'Johannes', '1989-04-18', 'PL', '178', 'f');
	insert into "directors" ("firstName", "lastName", "dateOfBirth", "nationality", "height", "gender")
	values ('Mandarin', 'Hannes', '1989-04-18', 'LT', '203', 'm');

insert into "movies" ("title", "description", "releaseDate", "country", "language")
	values ('Narnia movie', 'Fantastic movie about 4 siblings', '2014-06-15', 'PL', 'EN');
insert into "movies" ("title", "description", "releaseDate", "country", "language")
	values ('Poland-Lithuanian', 'Poland-Lithuanian thousand years history film', '1998-04-21', 'LT', 'EN');
insert into "movies" ("title", "description", "releaseDate", "country", "language")
	values ('Home alone', 'Kevin alone at home', '2018-08-01', 'AU', 'LT');
insert into "movies" ("title", "description", "releaseDate", "country", "language")
	values ('Harry Potter', 'Harry and friends', '1980-12-10', 'SE', 'SE');
insert into "movies" ("title", "description", "releaseDate", "country", "language")
	values ('Thor', 'Fantastic story', '2009-06-14', 'SE', 'LT');


# --- !Downs

drop table if exists "movies";
drop table if exists "directors";
drop table if exists "actors";
drop table if exists "genres";