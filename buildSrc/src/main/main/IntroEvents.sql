create table IntroEvents
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT           not null,
    updated_date TEXT           not null,
    expiry_epoch BIGINT,
    user_id      INT            not null
        constraint fk_IntroEvents_user_id__id
            references Users
            on update restrict on delete restrict,
    role         VARCHAR(100)   not null,
    event_type   VARCHAR(1000)  not null,
    memo         VARCHAR(10000) not null
);

