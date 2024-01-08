CREATE TABLE IF NOT EXISTS BtcReceivedEvents
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000)  DEFAULT '' NOT NULL,
    created_date TEXT                      NOT NULL,
    updated_date TEXT                      NOT NULL,
    expiry_epoch BIGINT                    NULL,
    tag          VARCHAR(1000)  DEFAULT '' NOT NULL,
    "type"       VARCHAR(1000)  DEFAULT '' NOT NULL,
    memo         VARCHAR(1000)  DEFAULT '' NOT NULL,
    raw          VARCHAR(10000) DEFAULT '' NOT NULL,
    json         VARCHAR(10000) DEFAULT '' NOT NULL,
    amount       VARCHAR(10000)            NOT NULL,
    rec_addr_str VARCHAR(10000)            NULL
);
CREATE TABLE IF NOT EXISTS BtcWalletEvents
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date TEXT                     NOT NULL,
    updated_date TEXT                     NOT NULL,
    expiry_epoch BIGINT                   NULL,
    balance_sat  BIGINT                   NOT NULL,
    raw          VARCHAR(10000)           NULL,
    memo         VARCHAR(10000)           NULL
);

CREATE TABLE IF NOT EXISTS CryptoReceiveEvent
(
    receivingAddress TEXT,
    amount           TEXT,
    currencyType     TEXT,
    transactionData  TEXT,
    uid              INTEGER PRIMARY KEY AUTOINCREMENT,
    createdDate      TEXT,
    updatedDate      TEXT,
    expiryEpoch      TEXT
);

CREATE TABLE IF NOT EXISTS CryptoSendEvent
(
    invoiceId   INTEGER,
    destAddress TEXT,
    amountSat   TEXT,
    memo        TEXT,
    status      TEXT,
    uid         INTEGER PRIMARY KEY AUTOINCREMENT,
    createdDate TEXT,
    updatedDate TEXT,
    expiryEpoch TEXT
);
CREATE TABLE IF NOT EXISTS CryptoWalletEvent
(
    invoiceId     INTEGER,
    walletAddress TEXT,
    amountSat     TEXT,
    memo          TEXT,
    type          TEXT,
    uid           INTEGER PRIMARY KEY AUTOINCREMENT,
    createdDate   TEXT,
    updatedDate   TEXT,
    expiryEpoch   TEXT
);
CREATE TABLE IF NOT EXISTS ScanEvent
(
    eventTag      TEXT,
    env           TEXT,
    scanGroup     TEXT,
    status        TEXT,
    periodSeconds INTEGER,
    timestamp     BIGINT,
    uid           INTEGER PRIMARY KEY AUTOINCREMENT,
    createdDate   TEXT,
    updatedDate   TEXT,
    expiryEpoch   TEXT
);
CREATE TABLE IF NOT EXISTS TextField
(
    tag             TEXT,
    body            TEXT,
    status          TEXT,
    displayName     TEXT,
    timestamp       BIGINT,
    ownerId         INTEGER,
    ownerType       TEXT,
    createdByUserId INTEGER,
    createdByRole   TEXT,
    uid             INTEGER PRIMARY KEY AUTOINCREMENT,
    createdDate     TEXT,
    updatedDate     TEXT,
    expiryEpoch     TEXT
);
