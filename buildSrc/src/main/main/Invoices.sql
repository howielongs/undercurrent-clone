create table Invoices
(
    id                    INTEGER
        primary key autoincrement,
    status                VARCHAR(1000),
    created_date          TEXT           not null,
    updated_date          TEXT           not null,
    expiry_epoch          BIGINT,
    order_id              INT
        constraint fk_Invoices_order_id__id
            references DeliveryOrders
            on update restrict on delete restrict,
    exchange_rate_id      INT            not null
        constraint fk_Invoices_exchange_rate_id__id
            references ExchangeRates
            on update restrict on delete restrict,
    subtotal_amount_id    INT            not null
        constraint fk_Invoices_subtotal_amount_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    fees_amount_id        INT            not null
        constraint fk_Invoices_fees_amount_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    total_amount_id       INT            not null
        constraint fk_Invoices_total_amount_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    split_fees_amount_id  INT            not null
        constraint fk_Invoices_split_fees_amount_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    fee_pct               VARCHAR(1000)  not null,
    raw                   VARCHAR(10000) not null,
    receipt               VARCHAR(10000) not null,
    last_nudged_timestamp BIGINT
);

