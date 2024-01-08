alter table msgs_old
    rename to msgs_all;

CREATE TABLE if not exists msgs_raw
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    created_date TEXT           NOT NULL,
    updated_date TEXT           NOT NULL,
    expiry_epoch BIGINT         NULL,
    body         VARCHAR(50000) NOT NULL,
    sender_sms   VARCHAR(10000) NOT NULL,
    receiver_sms VARCHAR(10000) NOT NULL,
    dbus_path    VARCHAR(10000) NOT NULL,
    "timestamp"  BIGINT         NOT NULL
);