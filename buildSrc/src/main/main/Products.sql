create table Products
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(1000),
    created_date  TEXT           not null,
    updated_date  TEXT           not null,
    expiry_epoch  BIGINT,
    storefront_id INT            not null
        constraint fk_Products_storefront_id__id
            references Storefronts
            on update restrict on delete restrict,
    name          VARCHAR(10000) not null,
    details       VARCHAR(10000) not null
);

