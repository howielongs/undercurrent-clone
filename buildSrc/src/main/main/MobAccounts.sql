create table MobAccounts
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT           not null,
    updated_date TEXT           not null,
    expiry_epoch BIGINT,
    account_id   VARCHAR(1000)  not null,
    name         VARCHAR(1000)  not null,
    main_address VARCHAR(1000)  not null,
    raw_json     VARCHAR(10000) not null
);

create unique index MobAccounts_account_id
    on MobAccounts (account_id);

create unique index MobAccounts_name
    on MobAccounts (name);

