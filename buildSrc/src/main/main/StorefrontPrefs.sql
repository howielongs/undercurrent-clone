create table StorefrontPrefs
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(1000),
    created_date  TEXT           not null,
    updated_date  TEXT           not null,
    expiry_epoch  BIGINT,
    storefront_id INT            not null
        constraint fk_StorefrontPrefs_storefront_id__id
            references Storefronts
            on update restrict on delete restrict,
    key           VARCHAR(10000) not null,
    value         VARCHAR(10000) not null,
    datatype      VARCHAR(10000) not null
);

