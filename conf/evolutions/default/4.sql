# --- !Ups

create table history_sequence (
  id                        bigint not null,
  symbol                    varchar(255),
  start_date                timestamp,
  end_date                  timestamp,
  constraint pk_history_sequence primary key (id))
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

create sequence history_sequence_seq;

create sequence quote_history_seq;

# --- !Downs

drop table if exists history_sequence cascade;

drop table if exists quote_history cascade;

drop sequence if exists history_sequence_seq;

drop sequence if exists quote_history_seq;

