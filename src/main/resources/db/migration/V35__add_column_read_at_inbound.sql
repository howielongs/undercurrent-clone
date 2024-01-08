alter table msgs_in
    add read_at BIGINT;


create table if not exists msgs_out_notifications_dg_tmp
(
    id               INTEGER
        primary key autoincrement,
    created_date     TEXT           not null,
    updated_date     TEXT           not null,
    expiry_epoch     BIGINT,
    user_id          INT            not null
        constraint fk_msg_notifications_user_id__id
            references system_users
            on update restrict on delete restrict,
    body             VARCHAR(50000) not null,
    role             VARCHAR(10000),
    bot_sender_sms   VARCHAR(10000) not null,
    human_addr       VARCHAR(10000) not null,
    human_addr_2     VARCHAR(10000),
    dbus_path        VARCHAR(10000) not null,
    send_after       BIGINT,
    sent_at          BIGINT,
    timestamp_server BIGINT,
    environment      VARCHAR(10000) not null
);

insert into msgs_out_notifications_dg_tmp(id, created_date, updated_date, expiry_epoch, user_id, body, role,
                                          bot_sender_sms, human_addr, human_addr_2, dbus_path, send_after, sent_at,
                                          timestamp_server, environment)
select id,
       created_date,
       updated_date,
       expiry_epoch,
       user_id,
       body,
       role,
       bot_sender_sms,
       human_addr,
       human_addr_2,
       dbus_path,
       sendAt,
       sentAt,
       timestamp_server,
       environment
from msgs_out_notifications;

drop table msgs_out_notifications;

alter table msgs_out_notifications_dg_tmp
    rename to msgs_out_notifications;

