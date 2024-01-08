create table SaleItems
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT         not null,
    updated_date TEXT         not null,
    expiry_epoch BIGINT,
    product_id   INT          not null
        constraint fk_SaleItems_product_id__id
            references Products
            on update restrict on delete restrict,
    price        VARCHAR(500) not null,
    unit_size    VARCHAR(500) not null
);

