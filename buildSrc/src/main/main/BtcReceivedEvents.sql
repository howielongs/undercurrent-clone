create table BtcReceivedEvents
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT           not null,
    updated_date TEXT           not null,
    expiry_epoch BIGINT,
    tag          VARCHAR(10000),
    type         VARCHAR(10000),
    memo         VARCHAR(10000),
    raw          VARCHAR(10000),
    amount       VARCHAR(10000) not null,
    rec_addr_str VARCHAR(10000)
);

