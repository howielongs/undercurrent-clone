CREATE TABLE IF NOT EXISTS MobAccounts
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date TEXT                     NOT NULL,
    updated_date TEXT                     NOT NULL,
    expiry_epoch BIGINT                   NULL,
    account_id   VARCHAR(1000)            NOT NULL,
    "name"       VARCHAR(1000)            NOT NULL,
    main_address VARCHAR(1000)            NOT NULL,
    raw_json     VARCHAR(10000)           NOT NULL
);

CREATE TABLE IF NOT EXISTS DeliveryOrders
(
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    status           VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date     TEXT                     NOT NULL,
    updated_date     TEXT                     NOT NULL,
    expiry_epoch     BIGINT                   NULL,
    order_code       VARCHAR(1000)            NOT NULL,
    invoice_id       INT                      NOT NULL,
    customer_id      INT                      NOT NULL,
    vendor_id        INT                      NOT NULL,
    zipcode          VARCHAR(20)              NOT NULL,
    delivery_address VARCHAR(1000)            NOT NULL,
    delivery_name    VARCHAR(1000)            NOT NULL,
    note_to_vendor   VARCHAR(5000)            NOT NULL,
    confirmed_date   TEXT                     NULL,
    declined_date    TEXT                     NULL,
    note_to_customer VARCHAR(1000)            NULL,
    tracking_number  VARCHAR(1000)            NULL,
    CONSTRAINT fk_DeliveryOrders_invoice_id__id FOREIGN KEY (invoice_id) REFERENCES Invoices (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_DeliveryOrders_customer_id__id FOREIGN KEY (customer_id) REFERENCES Customers (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_DeliveryOrders_vendor_id__id FOREIGN KEY (vendor_id) REFERENCES Vendors (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS Invoices
(
    id                    INTEGER PRIMARY KEY AUTOINCREMENT,
    status                VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date          TEXT                     NOT NULL,
    updated_date          TEXT                     NOT NULL,
    expiry_epoch          BIGINT                   NULL,
    order_id              INT                      NULL,
    exchange_rate_id      INT                      NOT NULL,
    subtotal_amount_id    INT                      NOT NULL,
    fees_amount_id        INT                      NOT NULL,
    total_amount_id       INT                      NOT NULL,
    split_fees_amount_id  INT                      NOT NULL,
    fee_pct               VARCHAR(1000)            NOT NULL,
    raw                   VARCHAR(10000)           NOT NULL,
    receipt               VARCHAR(10000)           NOT NULL,
    last_nudged_timestamp BIGINT                   NULL,
    CONSTRAINT fk_Invoices_order_id__id FOREIGN KEY (order_id) REFERENCES DeliveryOrders (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_Invoices_exchange_rate_id__id FOREIGN KEY (exchange_rate_id) REFERENCES ExchangeRates (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_Invoices_subtotal_amount_id__id FOREIGN KEY (subtotal_amount_id) REFERENCES CryptoAmounts (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_Invoices_fees_amount_id__id FOREIGN KEY (fees_amount_id) REFERENCES CryptoAmounts (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_Invoices_total_amount_id__id FOREIGN KEY (total_amount_id) REFERENCES CryptoAmounts (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_Invoices_split_fees_amount_id__id FOREIGN KEY (split_fees_amount_id) REFERENCES CryptoAmounts (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
