create table UserCreditLedgerTable
(
    id               INTEGER
        primary key autoincrement,
    status           VARCHAR(1000),
    created_date     TEXT           not null,
    updated_date     TEXT           not null,
    expiry_epoch     BIGINT,
    tag              VARCHAR(10000),
    type             VARCHAR(10000),
    memo             VARCHAR(10000),
    raw              VARCHAR(10000),
    user_id          INT            not null
        constraint fk_UserCreditLedgerTable_user_id__id
            references Users
            on update restrict on delete restrict,
    exchange_rate_id INT
        constraint fk_UserCreditLedgerTable_exchange_rate_id__id
            references ExchangeRates
            on update restrict on delete restrict,
    invoice_id       INT
        constraint fk_UserCreditLedgerTable_invoice_id__id
            references Invoices
            on update restrict on delete restrict,
    amount           VARCHAR(10000) not null,
    currency_type    VARCHAR(10000) not null,
    verified_date    TEXT
);

