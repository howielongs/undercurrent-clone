create table InboundMessages
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT           not null,
    updated_date TEXT           not null,
    expiry_epoch BIGINT,
    channel_id   INT            not null
        constraint fk_InboundMessages_channel_id__id
            references Channels
            on update restrict on delete restrict,
    body         VARCHAR(10000) not null,
    timestamp    BIGINT         not null
);

