create table OldComplexChannels
(
    id              INTEGER
        primary key autoincrement,
    status          VARCHAR(1000),
    created_date    TEXT           not null,
    updated_date    TEXT           not null,
    expiry_epoch    BIGINT,
    user_id         INT            not null
        constraint fk_OldComplexChannels_user_id__id
            references Users
            on update restrict on delete restrict,
    parent_id       INT
        constraint fk_OldComplexChannels_parent_id__id
            references OldComplexChannels
            on update restrict on delete restrict,
    conversation_id INT
        constraint fk_OldComplexChannels_conversation_id__id
            references SignalGroupChats
            on update restrict on delete restrict,
    group_id        VARCHAR(10000),
    json            VARCHAR(10000),
    name            VARCHAR(10000) not null,
    channel_type    VARCHAR(10000) not null,
    user_addr       VARCHAR(10000) not null,
    bot_addr        VARCHAR(10000),
    root_sms        VARCHAR(10000) not null,
    group_url       VARCHAR(10000)
);

