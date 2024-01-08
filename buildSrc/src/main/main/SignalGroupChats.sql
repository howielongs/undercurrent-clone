create table SignalGroupChats
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT not null,
    updated_date TEXT not null,
    expiry_epoch BIGINT,
    user_id      INT
        constraint fk_SignalGroupChats_user_id__id
            references Users
            on update restrict on delete restrict,
    name         VARCHAR(10000),
    group_id     VARCHAR(10000),
    dbus_path    VARCHAR(10000),
    group_id_b64 VARCHAR(10000),
    avatar_path  VARCHAR(10000),
    session_sms  VARCHAR(10000),
    invite_url   VARCHAR(10000),
    about_text   VARCHAR(10000),
    type         VARCHAR(10000)
);

