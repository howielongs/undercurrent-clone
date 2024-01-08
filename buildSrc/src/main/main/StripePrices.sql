create table StripePrices
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT           not null,
    updated_date TEXT           not null,
    expiry_epoch BIGINT,
    sale_item_id INT            not null
        constraint fk_StripePrices_sale_item_id__id
            references SaleItems
            on update restrict on delete restrict,
    price_sid    VARCHAR(10000) not null
);

