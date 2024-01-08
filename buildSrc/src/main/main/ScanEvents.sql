create table ScanEvents
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT            not null,
    updated_date TEXT            not null,
    expiry_epoch BIGINT,
    tag          VARCHAR(10000),
    type         VARCHAR(10000),
    memo         VARCHAR(10000),
    raw          VARCHAR(10000),
    event_tag    VARCHAR(1000)   not null,
    env          VARCHAR(1000)   not null,
    scan_group   VARCHAR(1000)   not null,
    period       INT default 500 not null
);

