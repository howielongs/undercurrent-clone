CREATE TABLE IF NOT EXISTS StorefrontPrefs
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    status        VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date  TEXT                     NOT NULL,
    updated_date  TEXT                     NOT NULL,
    expiry_epoch  BIGINT                   NULL,
    storefront_id INT                      NOT NULL,
    "key"         VARCHAR(5000)            NOT NULL,
    "value"       VARCHAR(5000)            NOT NULL,
    datatype      VARCHAR(5000)            NOT NULL,
    CONSTRAINT fk_StorefrontPrefs_storefront_id__id FOREIGN KEY (storefront_id) REFERENCES Storefronts (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS MobReceivedEvents
(
    id                    INTEGER PRIMARY KEY AUTOINCREMENT,
    status                VARCHAR(1000)  DEFAULT '' NOT NULL,
    created_date          TEXT                      NOT NULL,
    updated_date          TEXT                      NOT NULL,
    expiry_epoch          BIGINT                    NULL,
    tag                   VARCHAR(1000)  DEFAULT '' NOT NULL,
    "type"                VARCHAR(1000)  DEFAULT '' NOT NULL,
    memo                  VARCHAR(1000)  DEFAULT '' NOT NULL,
    raw                   VARCHAR(10000) DEFAULT '' NOT NULL,
    json                  VARCHAR(10000) DEFAULT '' NOT NULL,
    mob_amount            VARCHAR(10000)            NOT NULL,
    pmob_amount           BIGINT                    NOT NULL,
    sender_sms            VARCHAR(1000)             NOT NULL,
    recipient_sms         VARCHAR(1000)             NULL,
    receipt_bytes         VARCHAR(10000)            NULL,
    receipt_b64           VARCHAR(10000)            NULL,
    receipt_note          VARCHAR(10000)            NULL,
    receipt_json          VARCHAR(10000)            NULL,
    receiver_request_json VARCHAR(10000)            NULL
);
