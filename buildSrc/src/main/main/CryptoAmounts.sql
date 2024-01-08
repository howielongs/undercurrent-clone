create table CryptoAmounts
(
    id                   INTEGER
        primary key autoincrement,
    status               VARCHAR(1000),
    created_date         TEXT                       not null,
    updated_date         TEXT                       not null,
    expiry_epoch         BIGINT,
    exchange_rate_id     INT                        not null
        constraint fk_CryptoAmounts_exchange_rate_id__id
            references ExchangeRates
            on update restrict on delete restrict,
    crypto_atomic_amount VARCHAR(1000)  default '0' not null,
    fiat_amount          VARCHAR(10000) default '0' not null
);

