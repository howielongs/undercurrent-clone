CREATE TABLE IF NOT EXISTS UserIntroEvent
(
    userId                  INTEGER,
    role                    TEXT,
    welcomeMsgSeenTimestamp BIGINT,
    termsMsgSeenTimestamp   BIGINT,
    termsApproved           INTEGER,
    uid                     INTEGER PRIMARY KEY AUTOINCREMENT,
    createdDate             TEXT,
    updatedDate             TEXT,
    expiryEpoch             TEXT
);

CREATE TABLE IF NOT EXISTS CryptoSendEvents
(
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    status           VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date     TEXT                     NOT NULL,
    updated_date     TEXT                     NOT NULL,
    expiry_epoch     BIGINT                   NULL,
    invoice_id       INT                      NOT NULL,
    dest_address_id  INT                      NOT NULL,
    amount_crypto_id INT                      NOT NULL,
    memo             VARCHAR(5000)            NOT NULL,
    raw              VARCHAR(5000)            NOT NULL,
    raw_amount       VARCHAR(5000)            NOT NULL,
    crypto_type      VARCHAR(5000)            NOT NULL,
    CONSTRAINT fk_CryptoSendEvents_invoice_id__id FOREIGN KEY (invoice_id) REFERENCES Invoices (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_CryptoSendEvents_dest_address_id__id FOREIGN KEY (dest_address_id) REFERENCES CryptoAddresses (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_CryptoSendEvents_amount_crypto_id__id FOREIGN KEY (amount_crypto_id) REFERENCES CryptoAmounts (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS DepositCryptoAddresses
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT ''    NOT NULL,
    created_date TEXT                        NOT NULL,
    updated_date TEXT                        NOT NULL,
    expiry_epoch BIGINT                      NULL,
    user_id      INT                         NULL,
    address      VARCHAR(10000)              NOT NULL,
    crypto_type  VARCHAR(1000) DEFAULT 'BTC' NOT NULL,
    CONSTRAINT fk_DepositCryptoAddresses_user_id__id FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS ReferralCodes
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date TEXT                     NOT NULL,
    updated_date TEXT                     NOT NULL,
    expiry_epoch BIGINT                   NULL
);
CREATE TABLE IF NOT EXISTS ScanEvents
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT ''  NOT NULL,
    created_date TEXT                      NOT NULL,
    updated_date TEXT                      NOT NULL,
    expiry_epoch BIGINT                    NULL,
    event_tag    VARCHAR(1000)             NOT NULL,
    env          VARCHAR(1000)             NOT NULL,
    scan_group   VARCHAR(1000)             NOT NULL,
    period       INT           DEFAULT 500 NOT NULL
);
CREATE TABLE IF NOT EXISTS TestInputs
(
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    status            VARCHAR(1000)  DEFAULT '' NOT NULL,
    created_date      TEXT                      NOT NULL,
    updated_date      TEXT                      NOT NULL,
    expiry_epoch      BIGINT                    NULL,
    message_id        INT                       NULL,
    body              VARCHAR(10000) DEFAULT '' NOT NULL,
    num_msgs_expected INT                       NULL,
    good_substrings   VARCHAR(10000) DEFAULT '' NOT NULL,
    bad_substrings    VARCHAR(10000) DEFAULT '' NOT NULL,
    case_sensitive    BOOLEAN        DEFAULT 1  NOT NULL,
    sender            VARCHAR(100)   DEFAULT '' NOT NULL,
    receiver          VARCHAR(100)   DEFAULT '' NOT NULL,
    "role"            VARCHAR(100)              NULL,
    "timestamp"       BIGINT                    NOT NULL,
    CONSTRAINT fk_TestInputs_message_id__id FOREIGN KEY (message_id) REFERENCES Messages (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS UserStats
(
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    status              VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date        TEXT                     NOT NULL,
    updated_date        TEXT                     NOT NULL,
    expiry_epoch        BIGINT                   NULL,
    user_id             INT                      NOT NULL,
    last_msg_sent_epoch BIGINT                   NULL,
    message_send_count  BIGINT        DEFAULT 0  NOT NULL,
    CONSTRAINT fk_UserStats_user_id__id FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);



CREATE TABLE IF NOT EXISTS Customers
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    status        VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date  TEXT                     NOT NULL,
    updated_date  TEXT                     NOT NULL,
    expiry_epoch  BIGINT                   NULL,
    storefront_id INT                      NOT NULL,
    user_id       INT                      NOT NULL,
    CONSTRAINT fk_Customers_storefront_id__id FOREIGN KEY (storefront_id) REFERENCES Storefronts (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_Customers_user_id__id FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS CryptoAddresses
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT ''    NOT NULL,
    created_date TEXT                        NOT NULL,
    updated_date TEXT                        NOT NULL,
    expiry_epoch BIGINT                      NULL,
    user_id      INT                         NULL,
    address      VARCHAR(10000)              NOT NULL,
    crypto_type  VARCHAR(1000) DEFAULT 'BTC' NOT NULL,
    CONSTRAINT fk_CryptoAddresses_user_id__id FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS Products
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    status        VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date  TEXT                     NOT NULL,
    updated_date  TEXT                     NOT NULL,
    expiry_epoch  BIGINT                   NULL,
    storefront_id INT                      NOT NULL,
    "name"        VARCHAR(100)             NOT NULL,
    details       VARCHAR(500)             NOT NULL,
    CONSTRAINT fk_Products_storefront_id__id FOREIGN KEY (storefront_id) REFERENCES Storefronts (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS SaleItems
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date TEXT                     NOT NULL,
    updated_date TEXT                     NOT NULL,
    expiry_epoch BIGINT                   NULL,
    product_id   INT                      NOT NULL,
    price        VARCHAR(500)             NOT NULL,
    unit_size    VARCHAR(500)             NOT NULL,
    CONSTRAINT fk_SaleItems_product_id__id FOREIGN KEY (product_id) REFERENCES Products (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);


CREATE TABLE IF NOT EXISTS CryptoAmounts
(
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    status               VARCHAR(1000)  DEFAULT ''  NOT NULL,
    created_date         TEXT                       NOT NULL,
    updated_date         TEXT                       NOT NULL,
    expiry_epoch         BIGINT                     NULL,
    exchange_rate_id     INT                        NOT NULL,
    crypto_atomic_amount VARCHAR(1000)  DEFAULT '0' NOT NULL,
    fiat_amount          VARCHAR(10000) DEFAULT '0' NOT NULL,
    CONSTRAINT fk_CryptoAmounts_exchange_rate_id__id FOREIGN KEY (exchange_rate_id) REFERENCES ExchangeRates (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS ExchangeRates
(
    id                                  INTEGER PRIMARY KEY AUTOINCREMENT,
    status                              VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date                        TEXT                     NOT NULL,
    updated_date                        TEXT                     NOT NULL,
    expiry_epoch                        BIGINT                   NULL,
    crypto_type                         VARCHAR(1000)            NOT NULL,
    fiat_type                           VARCHAR(1000)            NOT NULL,
    fiat_to_crypto_atomic_exchange_rate VARCHAR(1000)            NOT NULL
);
CREATE TABLE IF NOT EXISTS CartItems
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT ''   NOT NULL,
    created_date TEXT                       NOT NULL,
    updated_date TEXT                       NOT NULL,
    expiry_epoch BIGINT                     NULL,
    order_id     INT           DEFAULT NULL NULL,
    sku_id       INT                        NOT NULL,
    customer_id  INT                        NOT NULL,
    notes        VARCHAR(500)               NOT NULL,
    quantity     INT                        NOT NULL,
    CONSTRAINT fk_CartItems_order_id__id FOREIGN KEY (order_id) REFERENCES DeliveryOrders (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_CartItems_sku_id__id FOREIGN KEY (sku_id) REFERENCES SaleItems (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_CartItems_customer_id__id FOREIGN KEY (customer_id) REFERENCES Customers (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS Ancestors
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    status        VARCHAR(1000) DEFAULT ''         NOT NULL,
    created_date  TEXT                             NOT NULL,
    updated_date  TEXT                             NOT NULL,
    expiry_epoch  BIGINT                           NULL,
    owner_user_id INT                              NOT NULL,
    owner_role    VARCHAR(1000) DEFAULT 'CUSTOMER' NOT NULL,
    entity_type   VARCHAR(1000)                    NOT NULL,
    memo          VARCHAR(1000)                    NOT NULL,
    old_entity_id INT                              NOT NULL,
    new_entity_id INT                              NOT NULL,
    CONSTRAINT fk_Ancestors_owner_user_id__id FOREIGN KEY (owner_user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS Attachments
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    status        VARCHAR(1000) DEFAULT ''         NOT NULL,
    created_date  TEXT                             NOT NULL,
    updated_date  TEXT                             NOT NULL,
    expiry_epoch  BIGINT                           NULL,
    owner_user_id INT                              NOT NULL,
    owner_role    VARCHAR(1000) DEFAULT 'CUSTOMER' NOT NULL,
    "path"        VARCHAR(1000)                    NOT NULL,
    caption       VARCHAR(5000)                    NOT NULL,
    CONSTRAINT fk_Attachments_owner_user_id__id FOREIGN KEY (owner_user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS AttachmentLinks
(
    id                       INTEGER PRIMARY KEY AUTOINCREMENT,
    status                   VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date             TEXT                     NOT NULL,
    updated_date             TEXT                     NOT NULL,
    expiry_epoch             BIGINT                   NULL,
    parent_attachment_id     INT                      NOT NULL,
    attachment_type          VARCHAR(1000)            NOT NULL,
    parent_entity_id         INT                      NULL,
    parent_entity_class_name VARCHAR(1000)            NULL,
    caption                  VARCHAR(5000) DEFAULT '' NOT NULL,
    CONSTRAINT fk_AttachmentLinks_parent_attachment_id__id FOREIGN KEY (parent_attachment_id) REFERENCES Attachments (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS AttachmentViewEvents
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    status         VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date   TEXT                     NOT NULL,
    updated_date   TEXT                     NOT NULL,
    expiry_epoch   BIGINT                   NULL,
    attachment_id  INT                      NOT NULL,
    viewer_user_id INT                      NOT NULL,
    location_tag   VARCHAR(1000)            NOT NULL,
    raw_context    VARCHAR(5000)            NULL,
    CONSTRAINT fk_AttachmentViewEvents_attachment_id__id FOREIGN KEY (attachment_id) REFERENCES Attachments (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_AttachmentViewEvents_viewer_user_id__id FOREIGN KEY (viewer_user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS IntroEvents
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date TEXT                     NOT NULL,
    updated_date TEXT                     NOT NULL,
    expiry_epoch BIGINT                   NULL,
    user_id      INT                      NOT NULL,
    "role"       VARCHAR(100)             NOT NULL,
    event_type   VARCHAR(1000)            NOT NULL,
    memo         VARCHAR(5000)            NOT NULL,
    CONSTRAINT fk_IntroEvents_user_id__id FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS UserCreditLedger
(
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    status           VARCHAR(1000)  DEFAULT '' NOT NULL,
    created_date     TEXT                      NOT NULL,
    updated_date     TEXT                      NOT NULL,
    expiry_epoch     BIGINT                    NULL,
    tag              VARCHAR(1000)  DEFAULT '' NOT NULL,
    "type"           VARCHAR(1000)  DEFAULT '' NOT NULL,
    memo             VARCHAR(1000)  DEFAULT '' NOT NULL,
    raw              VARCHAR(10000) DEFAULT '' NOT NULL,
    json             VARCHAR(10000) DEFAULT '' NOT NULL,
    user_id          INT                       NOT NULL,
    exchange_rate_id INT                       NULL,
    invoice_id       INT                       NULL,
    "role"           VARCHAR(10000)            NULL,
    amount           VARCHAR(10000)            NOT NULL,
    currency_type    VARCHAR(10000)            NOT NULL,
    verified_date    TEXT                      NULL,
    CONSTRAINT fk_UserCreditLedger_user_id__id FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_UserCreditLedger_exchange_rate_id__id FOREIGN KEY (exchange_rate_id) REFERENCES ExchangeRates (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_UserCreditLedger_invoice_id__id FOREIGN KEY (invoice_id) REFERENCES Invoices (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
