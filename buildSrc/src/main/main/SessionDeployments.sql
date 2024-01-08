create table SessionDeployments
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(1000),
    created_date  TEXT           not null,
    updated_date  TEXT           not null,
    expiry_epoch  BIGINT,
    bot_sms       VARCHAR(10000) not null,
    version       VARCHAR(10000) not null,
    env           VARCHAR(10000) not null,
    db_version    VARCHAR(10000),
    role          VARCHAR(10000) not null,
    min_log_level VARCHAR(10000) not null,
    is_test_mode  BOOLEAN
);

