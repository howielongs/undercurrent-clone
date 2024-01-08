create table TestInboundMessages
(
    id              INTEGER
        primary key autoincrement,
    status          VARCHAR(1000),
    created_date    TEXT           not null,
    updated_date    TEXT           not null,
    expiry_epoch    BIGINT,
    channel_id      INT            not null
        constraint fk_TestInboundMessages_channel_id__id
            references Channels
            on update restrict on delete restrict,
    body            VARCHAR(10000) not null,
    timestamp       BIGINT         not null,
    inbound_msg_id  INT
        constraint fk_TestInboundMessages_inbound_msg_id__id
            references InboundMessages
            on update restrict on delete restrict,
    outbound_msg_id INT
        constraint fk_TestInboundMessages_outbound_msg_id__id
            references OutboundMessages
            on update restrict on delete restrict,
    notif_msg_id    INT
        constraint fk_TestInboundMessages_notif_msg_id__id
            references OutboundNotifications
            on update restrict on delete restrict
);

