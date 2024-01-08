create table ExchangeRates
(
    id                                  INTEGER
        primary key autoincrement,
    status                              VARCHAR(1000),
    created_date                        TEXT          not null,
    updated_date                        TEXT          not null,
    expiry_epoch                        BIGINT,
    crypto_type                         VARCHAR(1000) not null,
    fiat_type                           VARCHAR(1000) not null,
    fiat_to_crypto_atomic_exchange_rate VARCHAR(1000) not null
);

