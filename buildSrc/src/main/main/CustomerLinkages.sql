create table CustomerLinkages
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(1000),
    created_date  TEXT not null,
    updated_date  TEXT not null,
    expiry_epoch  BIGINT,
    channel_id    INT  not null
        constraint fk_CustomerLinkages_channel_id__id
            references Channels
            on update restrict on delete restrict,
    storefront_id INT  not null
        constraint fk_CustomerLinkages_storefront_id__id
            references Storefronts
            on update restrict on delete restrict,
    join_code_id  INT  not null
        constraint fk_CustomerLinkages_join_code_id__id
            references ShopJoinCodes
            on update restrict on delete restrict
);

