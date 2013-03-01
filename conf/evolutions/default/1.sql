# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table account_history_response (
  id                        bigint not null,
  activity                  varchar(255),
  amount                    double,
  date                      timestamp,
  cusip                     varchar(255),
  constraint pk_account_history_response primary key (id))
;

create table position (
  id                        bigint not null,
  shares                    double,
  cost_basis                double,
  close_value               double,
  open_date                 timestamp,
  close_date                timestamp,
  is_closed                 boolean,
  symbol                    varchar(255),
  cusip                     varchar(255),
  desc                      varchar(255),
  type                      varchar(255),
  constraint pk_position primary key (id))
;

create sequence account_history_response_seq;

create sequence position_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists account_history_response;

drop table if exists position;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists account_history_response_seq;

drop sequence if exists position_seq;

