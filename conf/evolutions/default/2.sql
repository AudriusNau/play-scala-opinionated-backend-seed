# --- !Ups

create table "movies" (
   "id" bigserial primary key,
   "title" varchar not null,
    "description" varchar not null,
   "releaseDate" DATE,
    "country" varchar not null,
    "language" varchar not null
);

# --- !Downs

drop table if exists "movies";
