create table OldMessages
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT           not null,
    updated_date TEXT           not null,
    expiry_epoch BIGINT,
    body         VARCHAR(50000) not null,
    user_id      INT            not null
        constraint fk_OldMessages_user_id__id
            references Users
            on update restrict on delete restrict,
    sender       VARCHAR(1000)  not null,
    receiver     VARCHAR(1000)  not null,
    root_sms     VARCHAR(1000)  not null,
    group_id     VARCHAR(1000),
    timestamp    BIGINT         not null,
    user_uuid    VARCHAR(1000)
);

