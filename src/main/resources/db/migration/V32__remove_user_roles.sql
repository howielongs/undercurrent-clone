create table system_users_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    created_date TEXT           not null,
    updated_date TEXT           not null,
    expiry_epoch BIGINT,
    sms          VARCHAR(10000) not null,
    role         VARCHAR(100)  default 'CUSTOMER' not null,
    uuid         VARCHAR(10000)
);

insert into system_users_dg_tmp(id, created_date, updated_date, expiry_epoch, sms, role, uuid)
select id, created_date, updated_date, expiry_epoch, number, role, uuid
from system_users;

drop table system_users;

alter table system_users_dg_tmp
    rename to system_users;

create unique index system_users_uuid_uindex
    on system_users (uuid);

