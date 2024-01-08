CREATE UNIQUE INDEX IF NOT EXISTS DeliveryOrders_order_code ON DeliveryOrders (order_code);
CREATE UNIQUE INDEX IF NOT EXISTS MobAccounts_account_id ON MobAccounts (account_id);
CREATE UNIQUE INDEX IF NOT EXISTS MobAccounts_name ON MobAccounts ("name");
CREATE UNIQUE INDEX IF NOT EXISTS JoinCodes_value ON JoinCodes ("value");
CREATE UNIQUE INDEX IF NOT EXISTS Users_number
    on Users (number);
CREATE TABLE IF NOT EXISTS ZipCodeLookups
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date TEXT                     NOT NULL,
    updated_date TEXT                     NOT NULL,
    expiry_epoch BIGINT                   NULL,
    zipcode      VARCHAR(5000)            NOT NULL,
    city         VARCHAR(5000)            NOT NULL,
    "state"      VARCHAR(5000)            NOT NULL,
    state_abbr   VARCHAR(5000)            NOT NULL,
    timezone     VARCHAR(5000)            NOT NULL
);
CREATE TABLE IF NOT EXISTS Channels
(
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    status              VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date        TEXT                     NOT NULL,
    updated_date        TEXT                     NOT NULL,
    expiry_epoch        BIGINT                   NULL,
    user_id             INT                      NOT NULL,
    group_id_or_bot_sms VARCHAR(10000)           NOT NULL,
    dbus_path           VARCHAR(10000)           NOT NULL,
    label               VARCHAR(10000)           NULL,
    "type"              VARCHAR(10000)           NOT NULL,
    CONSTRAINT fk_Channels_user_id__id FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);