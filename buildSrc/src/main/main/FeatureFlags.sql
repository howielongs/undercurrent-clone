create table FeatureFlags
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT           not null,
    updated_date TEXT           not null,
    expiry_epoch BIGINT,
    session_id   INT            not null
        constraint fk_FeatureFlags_session_id__id
            references SessionDeployments
            on update restrict on delete restrict,
    name         VARCHAR(10000) not null,
    value        VARCHAR(10000) not null,
    memo         VARCHAR(10000),
    bot_sms      VARCHAR(10000) not null,
    version      VARCHAR(10000) not null,
    env          VARCHAR(10000) not null,
    role         VARCHAR(10000) not null
);

