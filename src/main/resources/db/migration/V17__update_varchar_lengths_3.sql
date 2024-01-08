create table JoinCodes_dg_tmp
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(10000) default '' not null,
    created_date  TEXT                      not null,
    updated_date  TEXT                      not null,
    expiry_epoch  BIGINT,
    owner_user_id INT                       not null
        constraint fk_JoinCodes_owner_user_id__id
            references Users
            on update restrict on delete restrict,
    parent_id     INT
        constraint fk_JoinCodes_parent_id__id
            references JoinCodes
            on update restrict on delete restrict,
    entity_id     INT                       not null,
    tag           VARCHAR(10000),
    value         VARCHAR(10000)            not null,
    entity_type   VARCHAR(10000)            not null
);

insert into JoinCodes_dg_tmp(id, status, created_date, updated_date, expiry_epoch, owner_user_id, parent_id, entity_id,
                             tag, value, entity_type)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       owner_user_id,
       parent_id,
       entity_id,
       tag,
       value,
       entity_type
from JoinCodes;

drop table JoinCodes;

alter table JoinCodes_dg_tmp
    rename to JoinCodes;

create unique index JoinCodes_value
    on JoinCodes (value);





create table MobAccounts_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    account_id   VARCHAR(10000)            not null,
    name         VARCHAR(10000)            not null,
    main_address VARCHAR(10000)            not null,
    raw_json     VARCHAR(10000)            not null
);

insert into MobAccounts_dg_tmp(id, status, created_date, updated_date, expiry_epoch, account_id, name, main_address,
                               raw_json)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       account_id,
       name,
       main_address,
       raw_json
from MobAccounts;

drop table MobAccounts;

alter table MobAccounts_dg_tmp
    rename to MobAccounts;

create unique index MobAccounts_account_id
    on MobAccounts (account_id);

create unique index MobAccounts_name
    on MobAccounts (name);





create table CryptoAmounts_dg_tmp
(
    id                   INTEGER
        primary key autoincrement,
    status               VARCHAR(10000) default ''  not null,
    created_date         TEXT                       not null,
    updated_date         TEXT                       not null,
    expiry_epoch         BIGINT,
    exchange_rate_id     INT                        not null
        constraint fk_CryptoAmounts_exchange_rate_id__id
            references ExchangeRates
            on update restrict on delete restrict,
    crypto_atomic_amount VARCHAR(10000) default '0' not null,
    fiat_amount          VARCHAR(10000) default '0' not null
);

insert into CryptoAmounts_dg_tmp(id, status, created_date, updated_date, expiry_epoch, exchange_rate_id,
                                 crypto_atomic_amount, fiat_amount)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       exchange_rate_id,
       crypto_atomic_amount,
       fiat_amount
from CryptoAmounts;

drop table CryptoAmounts;

alter table CryptoAmounts_dg_tmp
    rename to CryptoAmounts;

