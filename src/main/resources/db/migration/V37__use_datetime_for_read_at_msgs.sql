

create table msgs_in_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    created_date TEXT           not null,
    updated_date TEXT           not null,
    expiry_epoch BIGINT,
    body         VARCHAR(50000) not null,
    sender_sms   VARCHAR(10000) not null,
    receiver_sms VARCHAR(10000) not null,
    dbus_path    VARCHAR(10000) not null,
    timestamp    BIGINT         not null,
    uuid         VARCHAR(10000),
    read_at      TEXT
);

insert into msgs_in_dg_tmp(id, created_date, updated_date, expiry_epoch, body, sender_sms, receiver_sms, dbus_path,
                           timestamp, uuid, read_at)
select id,
       created_date,
       updated_date,
       expiry_epoch,
       body,
       sender_sms,
       receiver_sms,
       dbus_path,
       timestamp,
       uuid,
       read_at
from msgs_in;

drop table msgs_in;

alter table msgs_in_dg_tmp
    rename to msgs_in;

create unique index msgs_raw_timestamp
    on msgs_in (timestamp);







create table msgs_out_notifications_dg_tmp
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
    sent_at          TEXT,
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
       send_after,
       sent_at,
       timestamp_server,
       environment
from msgs_out_notifications;

drop table msgs_out_notifications;

alter table msgs_out_notifications_dg_tmp
    rename to msgs_out_notifications;






create table msgs_out_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    user_id      INT
        constraint fk_Messages_user_id__id
            references system_users
            on update restrict on delete restrict,
    body         VARCHAR(50000)            not null,
    sender       VARCHAR(10000)            not null,
    receiver     VARCHAR(10000)            not null,
    timestamp    BIGINT,
    role         VARCHAR(10000),
    uuid         VARCHAR(10000),
    send_after   BIGINT,
    sent_at      TEXT
);

insert into msgs_out_dg_tmp(id, status, created_date, updated_date, expiry_epoch, user_id, body, sender, receiver,
                            timestamp, role, uuid, send_after, sent_at)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       user_id,
       body,
       sender,
       receiver,
       timestamp,
       role,
       uuid,
       send_after,
       sent_at
from msgs_out;

drop table msgs_out;

alter table msgs_out_dg_tmp
    rename to msgs_out;


