# --- !Ups

create table abstract_resolution (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  parent_cusip              varchar(255),
  split_ratio               float,
  constraint pk_abstract_resolution primary key (id))
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


create sequence abstract_resolution_seq;

create sequence bookkeeping_seq;

alter table bookkeeping add constraint fk_bookkeeping_resolution_1 foreign key (resolution_id) references abstract_resolution (id);
create index ix_bookkeeping_resolution_1 on bookkeeping (resolution_id);

# --- !Downs

drop table if exists abstract_resolution cascade;

drop table if exists bookkeeping cascade;

drop sequence if exists abstract_resolution_seq;

drop sequence if exists bookkeeping_seq;
