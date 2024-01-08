CREATE TABLE IF NOT EXISTS Users
(
    id           INTEGER
    primary key autoincrement,
    status       VARCHAR(1000) default ''         not null,
    created_date TEXT                             not null,
    updated_date TEXT                             not null,
    expiry_epoch BIGINT,
    number       VARCHAR(100)                     not null,
    role         VARCHAR(100)  default 'CUSTOMER' not null,
    uuid         VARCHAR(1000)
    );
