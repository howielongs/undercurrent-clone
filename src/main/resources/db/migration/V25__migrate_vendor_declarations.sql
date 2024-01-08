create table shop_vendors_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    user_id      INT                       not null
        constraint fk_Vendors_user_id__id
            references system_users
            on update restrict on delete restrict,
    nickname     VARCHAR(10000)            not null
);

insert into shop_vendors_dg_tmp(id, status, created_date, updated_date, expiry_epoch, user_id, nickname)
select id, status, created_date, updated_date, expiry_epoch, user_id, name_tag
from shop_vendors;

drop table shop_vendors;

alter table shop_vendors_dg_tmp
    rename to shop_vendors;

