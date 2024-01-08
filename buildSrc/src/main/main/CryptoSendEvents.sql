create table CryptoSendEvents
(
    id               INTEGER
        primary key autoincrement,
    status           VARCHAR(1000),
    created_date     TEXT           not null,
    updated_date     TEXT           not null,
    expiry_epoch     BIGINT,
    invoice_id       INT            not null
        constraint fk_CryptoSendEvents_invoice_id__id
            references Invoices
            on update restrict on delete restrict,
    dest_address_id  INT            not null
        constraint fk_CryptoSendEvents_dest_address_id__id
            references CryptoAddresses
            on update restrict on delete restrict,
    amount_crypto_id INT            not null
        constraint fk_CryptoSendEvents_amount_crypto_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    memo             VARCHAR(10000) not null,
    raw              VARCHAR(10000) not null,
    raw_amount       VARCHAR(10000) not null,
    crypto_type      VARCHAR(10000) not null
);

