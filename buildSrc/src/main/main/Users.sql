create table Users
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT           not null,
    updated_date TEXT           not null,
    expiry_epoch BIGINT,
    address      VARCHAR(10000) not null,
    number       VARCHAR(10000),
    uuid         VARCHAR(10000),
    tag          VARCHAR(10000)
);

create unique index Users_address
    on Users (address);

create unique index Users_uuid
    on Users (uuid);

