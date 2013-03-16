# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table abstract_resolution (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  parent_cusip              varchar(255),
  split_ratio               float,
  constraint pk_abstract_resolution primary key (id))
;

create table account_history_response (
  id                        bigint not null,
  activity                  varchar(255),
  amount                    float,
  date                      timestamp,
  cusip                     varchar(255),
  constraint pk_account_history_response primary key (id))
;

create table bookkeeping (
  id                        bigint not null,
  date                      timestamp,
  symbol                    varchar(255),
  cusip                     varchar(255),
  description               varchar(255),
  amount                    float,
  quantity                  float,
  resolution_id             bigint,
  constraint pk_bookkeeping primary key (id))
;

create table history_sequence (
  id                        bigint not null,
  symbol                    varchar(255),
  start_date                timestamp,
  end_date                  timestamp,
  constraint pk_history_sequence primary key (id))
;

create table position (
  id                        bigint not null,
  shares                    float,
  cost_basis                float,
  close_value               float,
  open_date                 timestamp,
  close_date                timestamp,
  closed                    boolean,
  symbol                    varchar(255),
  cusip                     varchar(255),
  description               varchar(255),
  security_type             varchar(255),
  constraint pk_position primary key (id))
;

create table position_tag (
  id                        bigint not null,
  tag                       varchar(255),
  constraint pk_position_tag primary key (id))
;

create table quote_history (
  id                        bigint not null,
  high                      float,
  low                       float,
  open                      float,
  close                     float,
  symbol                    varchar(255),
  date                      timestamp,
  constraint pk_quote_history primary key (id))
;


create table position_tag_position (
  position_tag_id                bigint not null,
  position_id                    bigint not null,
  constraint pk_position_tag_position primary key (position_tag_id, position_id))
;
create sequence abstract_resolution_seq;

create sequence account_history_response_seq;

create sequence bookkeeping_seq;

create sequence history_sequence_seq;

create sequence position_seq;

create sequence position_tag_seq;

create sequence quote_history_seq;

alter table bookkeeping add constraint fk_bookkeeping_resolution_1 foreign key (resolution_id) references abstract_resolution (id);
create index ix_bookkeeping_resolution_1 on bookkeeping (resolution_id);



alter table position_tag_position add constraint fk_position_tag_position_posi_01 foreign key (position_tag_id) references position_tag (id);

alter table position_tag_position add constraint fk_position_tag_position_posi_02 foreign key (position_id) references position (id);

# --- !Downs

drop table if exists abstract_resolution cascade;

drop table if exists account_history_response cascade;

drop table if exists bookkeeping cascade;

drop table if exists history_sequence cascade;

drop table if exists position cascade;

drop table if exists position_tag cascade;

drop table if exists position_tag_position cascade;

drop table if exists quote_history cascade;

drop sequence if exists abstract_resolution_seq;

drop sequence if exists account_history_response_seq;

drop sequence if exists bookkeeping_seq;

drop sequence if exists history_sequence_seq;

drop sequence if exists position_seq;

drop sequence if exists position_tag_seq;

drop sequence if exists quote_history_seq;

