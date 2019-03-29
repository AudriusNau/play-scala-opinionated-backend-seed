# --- !Ups

create table "actors" (
   "id" bigserial primary key,
   "firstName" varchar not null,
   "lastName" varchar not null,
   "dateOfBirth" DATE,
   "nationality" varchar not null,
   "height" int,
   "gender" varchar not null
);

# --- !Downs

drop table if exists "actors";
