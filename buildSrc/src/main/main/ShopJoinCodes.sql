create table ShopJoinCodes
(
    id                   INTEGER
        primary key autoincrement,
    status               VARCHAR(1000),
    created_date         TEXT           not null,
    updated_date         TEXT           not null,
    expiry_epoch         BIGINT,
    owner_user_id        INT            not null
        constraint fk_ShopJoinCodes_owner_user_id__id
            references Users
            on update restrict on delete restrict,
    value                VARCHAR(10000) not null,
    tag                  VARCHAR(10000),
    storefront_id        INT            not null
        constraint fk_ShopJoinCodes_storefront_id__id
            references Storefronts
            on update restrict on delete restrict,
    parent_storefront_id INT
        constraint fk_ShopJoinCodes_parent_storefront_id__id
            references ShopJoinCodes
            on update restrict on delete restrict
);

create unique index ShopJoinCodes_value
    on ShopJoinCodes (value);

