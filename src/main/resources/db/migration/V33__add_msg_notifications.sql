-- auto-generated definition
create table if not exists msg_notifications
(
    id               INTEGER
        primary key autoincrement,
    created_date     TEXT                      not null,
    updated_date     TEXT                      not null,
    expiry_epoch     BIGINT,
    user_id          INT                       not null
        constraint fk_msg_notifications_user_id__id
            references system_users
            on update restrict on delete restrict,
    body             VARCHAR(50000)            not null,
    role             VARCHAR(10000),
    bot_sender_sms   VARCHAR(10000)            not null,
    human_addr       VARCHAR(10000)            not null,
    human_addr_2     VARCHAR(10000),
    dbus_path        VARCHAR(10000)            not null,
    sendAt           BIGINT,
    sentAt           BIGINT,
    timestamp_server BIGINT,
    environment      VARCHAR(10000)            not null
);

