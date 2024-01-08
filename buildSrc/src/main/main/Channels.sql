create table Channels
(
    id                  INTEGER
        primary key autoincrement,
    status              VARCHAR(1000),
    created_date        TEXT           not null,
    updated_date        TEXT           not null,
    expiry_epoch        BIGINT,
    user_id             INT            not null
        constraint fk_Channels_user_id__id
            references Users
            on update restrict on delete restrict,
    group_id_or_bot_sms VARCHAR(10000) not null,
    dbus_path           VARCHAR(10000) not null,
    label               VARCHAR(10000),
    type                VARCHAR(10000) not null
);

