create table SwapOperations
(
    id                    INTEGER
        primary key autoincrement,
    status                VARCHAR(1000),
    created_date          TEXT          not null,
    updated_date          TEXT          not null,
    expiry_epoch          BIGINT,
    user_id               INT           not null
        constraint fk_SwapOperations_user_id__id
            references Users
            on update restrict on delete restrict,
    role                  VARCHAR(1000) not null,
    from_exchange_rate_id INT           not null
        constraint fk_SwapOperations_from_exchange_rate_id__id
            references ExchangeRates
            on update restrict on delete restrict,
    to_exchange_rate_id   INT           not null
        constraint fk_SwapOperations_to_exchange_rate_id__id
            references ExchangeRates
            on update restrict on delete restrict,
    from_amount_id        INT           not null
        constraint fk_SwapOperations_from_amount_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    to_amount_id          INT           not null
        constraint fk_SwapOperations_to_amount_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    target_fiat_amount    VARCHAR(1000),
    fiat_type             VARCHAR(1000) not null,
    from_address_id       INT           not null
        constraint fk_SwapOperations_from_address_id__id
            references DepositCryptoAddresses
            on update restrict on delete restrict,
    to_address_id         INT           not null
        constraint fk_SwapOperations_to_address_id__id
            references DepositCryptoAddresses
            on update restrict on delete restrict
);

