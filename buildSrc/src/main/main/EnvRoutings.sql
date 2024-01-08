create table EnvRoutings
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT           not null,
    updated_date TEXT           not null,
    expiry_epoch BIGINT,
    env          VARCHAR(10000) not null,
    role         VARCHAR(10000) not null,
    sms          VARCHAR(10000) not null,
    notes        VARCHAR(10000),
    is_disabled  BOOLEAN        not null,
    is_mac       BOOLEAN
);

