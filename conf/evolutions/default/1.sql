# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table account_history_response (
  id                        bigint not null,
  activity                  varchar(255),
  amount                    float,
  date                      timestamp,
  cusip                     varchar(255),
  constraint pk_account_history_response primary key (id))
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


create table position_tag_position (
  position_tag_id                bigint not null,
  position_id                    bigint not null,
  constraint pk_position_tag_position primary key (position_tag_id, position_id))
;
create sequence account_history_response_seq;

create sequence position_seq;

create sequence position_tag_seq;




alter table position_tag_position add constraint fk_position_tag_position_posi_01 foreign key (position_tag_id) references position_tag (id);

alter table position_tag_position add constraint fk_position_tag_position_posi_02 foreign key (position_id) references position (id);

# --- !Downs

drop table if exists account_history_response cascade;

drop table if exists position cascade;

drop table if exists position_tag cascade;

drop table if exists position_tag_position cascade;

drop sequence if exists account_history_response_seq;

drop sequence if exists position_seq;

drop sequence if exists position_tag_seq;

