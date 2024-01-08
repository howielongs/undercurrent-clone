create table UserStats
(
    id                  INTEGER
        primary key autoincrement,
    status              VARCHAR(1000),
    created_date        TEXT             not null,
    updated_date        TEXT             not null,
    expiry_epoch        BIGINT,
    user_id             INT              not null
        constraint fk_UserStats_user_id__id
            references Users
            on update restrict on delete restrict,
    last_msg_sent_epoch BIGINT,
    message_send_count  BIGINT default 0 not null
);

