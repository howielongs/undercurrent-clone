create table OutboundAttachmentMessages
(
    id                 INTEGER
        primary key autoincrement,
    status             VARCHAR(1000),
    created_date       TEXT           not null,
    updated_date       TEXT           not null,
    expiry_epoch       BIGINT,
    channel_id         INT            not null
        constraint fk_OutboundAttachmentMessages_channel_id__id
            references Channels
            on update restrict on delete restrict,
    body               VARCHAR(10000) not null,
    sent_date          TEXT,
    send_at_nano_epoch BIGINT,
    timestamp          BIGINT,
    attachment_id      INT            not null
        constraint fk_OutboundAttachmentMessages_attachment_id__id
            references Attachments
            on update restrict on delete restrict
);

