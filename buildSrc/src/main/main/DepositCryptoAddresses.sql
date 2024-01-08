create table DepositCryptoAddresses
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT                        not null,
    updated_date TEXT                        not null,
    expiry_epoch BIGINT,
    user_id      INT
        constraint fk_DepositCryptoAddresses_user_id__id
            references Users
            on update restrict on delete restrict,
    address      VARCHAR(10000)              not null,
    crypto_type  VARCHAR(1000) default 'BTC' not null
);

