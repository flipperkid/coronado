# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table option_contract (
  id                        bigint not null,
  contract_size             integer,
  open_interest             integer,
  exchange                  varchar(255),
  underlying_symbol         varchar(255),
  expiration_date           timestamp,
  strike_price              decimal(38),
  option_type               integer,
  constraint ck_option_contract_option_type check (option_type in (0,1)),
  constraint pk_option_contract primary key (id))
;

create table position (
  id                        bigint not null,
  quantity                  double,
  open_quantity             double,
  amount                    double,
  profit_loss               double,
  date                      timestamp,
  symbol                    varchar(255),
  cusip                     varchar(255),
  desc                      varchar(255),
  type                      varchar(255),
  constraint pk_position primary key (id))
;

create table position_close (
  id                        bigint not null,
  quantity                  double,
  amount                    double,
  date                      timestamp,
  constraint pk_position_close primary key (id))
;

create sequence option_contract_seq;

create sequence position_seq;

create sequence position_close_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists option_contract;

drop table if exists position;

drop table if exists position_close;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists option_contract_seq;

drop sequence if exists position_seq;

drop sequence if exists position_close_seq;

