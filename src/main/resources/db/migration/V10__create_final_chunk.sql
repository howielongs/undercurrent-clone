CREATE TABLE IF NOT EXISTS StripeMapping
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000)  DEFAULT '' NOT NULL,
    created_date TEXT                      NOT NULL,
    updated_date TEXT                      NOT NULL,
    expiry_epoch BIGINT NULL,
    tag          VARCHAR(1000)  DEFAULT '' NOT NULL,
    "type"       VARCHAR(1000)  DEFAULT '' NOT NULL,
    memo         VARCHAR(1000)  DEFAULT '' NOT NULL,
    raw          VARCHAR(10000) DEFAULT '' NOT NULL,
    json         VARCHAR(10000) DEFAULT '' NOT NULL,
    vendor_id    INT                       NOT NULL,
    stripe_url   VARCHAR(10000) NULL,
    CONSTRAINT fk_StripeMapping_vendor_id__id FOREIGN KEY (vendor_id) REFERENCES Vendors (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS SwapOperation
(
    id                    INTEGER PRIMARY KEY AUTOINCREMENT,
    status                VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date          TEXT                     NOT NULL,
    updated_date          TEXT                     NOT NULL,
    expiry_epoch          BIGINT NULL,
    user_id               INT                      NOT NULL,
    "role"                VARCHAR(1000)            NOT NULL,
    from_exchange_rate_id INT                      NOT NULL,
    to_exchange_rate_id   INT                      NOT NULL,
    from_amount_id        INT                      NOT NULL,
    to_amount_id          INT                      NOT NULL,
    target_fiat_amount    VARCHAR(1000) NULL,
    fiat_type             VARCHAR(1000)            NOT NULL,
    from_address_id       INT                      NOT NULL,
    to_address_id         INT                      NOT NULL,
    CONSTRAINT fk_SwapOperation_user_id__id FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_SwapOperation_from_exchange_rate_id__id FOREIGN KEY (from_exchange_rate_id) REFERENCES ExchangeRates (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_SwapOperation_to_exchange_rate_id__id FOREIGN KEY (to_exchange_rate_id) REFERENCES ExchangeRates (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_SwapOperation_from_amount_id__id FOREIGN KEY (from_amount_id) REFERENCES CryptoAmounts (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_SwapOperation_to_amount_id__id FOREIGN KEY (to_amount_id) REFERENCES CryptoAmounts (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_SwapOperation_from_address_id__id FOREIGN KEY (from_address_id) REFERENCES DepositCryptoAddresses (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_SwapOperation_to_address_id__id FOREIGN KEY (to_address_id) REFERENCES DepositCryptoAddresses (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS JoinCodes
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    status        VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date  TEXT                     NOT NULL,
    updated_date  TEXT                     NOT NULL,
    expiry_epoch  BIGINT NULL,
    owner_user_id INT                      NOT NULL,
    parent_id     INT NULL,
    entity_id     INT                      NOT NULL,
    tag           VARCHAR(5000) NULL,
    "value"       VARCHAR(5000)            NOT NULL,
    entity_type   VARCHAR(5000)            NOT NULL,
    CONSTRAINT fk_JoinCodes_owner_user_id__id FOREIGN KEY (owner_user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_JoinCodes_parent_id__id FOREIGN KEY (parent_id) REFERENCES JoinCodes (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS JoinCodeUsages
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date TEXT                     NOT NULL,
    updated_date TEXT                     NOT NULL,
    expiry_epoch BIGINT NULL,
    user_id      INT                      NOT NULL,
    join_code_id INT                      NOT NULL,
    CONSTRAINT fk_JoinCodeUsages_user_id__id FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_JoinCodeUsages_join_code_id__id FOREIGN KEY (join_code_id) REFERENCES JoinCodes (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS StripeApiKeys
(
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    status            VARCHAR(1000)  DEFAULT '' NOT NULL,
    created_date      TEXT                      NOT NULL,
    updated_date      TEXT                      NOT NULL,
    expiry_epoch      BIGINT NULL,
    tag               VARCHAR(1000)  DEFAULT '' NOT NULL,
    "type"            VARCHAR(1000)  DEFAULT '' NOT NULL,
    memo              VARCHAR(1000)  DEFAULT '' NOT NULL,
    raw               VARCHAR(10000) DEFAULT '' NOT NULL,
    json              VARCHAR(10000) DEFAULT '' NOT NULL,
    storefront_id     INT                       NOT NULL,
    stripe_secret_key VARCHAR(10000) NULL,
    CONSTRAINT fk_StripeApiKeys_storefront_id__id FOREIGN KEY (storefront_id) REFERENCES Storefronts (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS StripePaymentLinks
(
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    status               VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date         TEXT                     NOT NULL,
    updated_date         TEXT                     NOT NULL,
    expiry_epoch         BIGINT NULL,
    order_id             INT                      NOT NULL,
    payment_link_id      VARCHAR(10000)           NOT NULL,
    payment_url          VARCHAR(10000)           NOT NULL,
    checkout_session_sid VARCHAR(10000) NULL,
    payment_intent_sid   VARCHAR(10000) NULL,
    CONSTRAINT fk_StripePaymentLinks_order_id__id FOREIGN KEY (order_id) REFERENCES DeliveryOrders (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS StripePrice
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date TEXT                     NOT NULL,
    updated_date TEXT                     NOT NULL,
    expiry_epoch BIGINT NULL,
    sale_item_id INT                      NOT NULL,
    price_sid    VARCHAR(10000)           NOT NULL,
    CONSTRAINT fk_StripePrice_sale_item_id__id FOREIGN KEY (sale_item_id) REFERENCES SaleItems (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
