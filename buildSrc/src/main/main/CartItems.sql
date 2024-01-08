create table CartItems
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT         not null,
    updated_date TEXT         not null,
    expiry_epoch BIGINT,
    order_id     INT default NULL
        constraint fk_CartItems_order_id__id
            references DeliveryOrders
            on update restrict on delete restrict,
    sku_id       INT          not null
        constraint fk_CartItems_sku_id__id
            references SaleItems
            on update restrict on delete restrict,
    customer_id  INT          not null
        constraint fk_CartItems_customer_id__id
            references CustomerLinkages
            on update restrict on delete restrict,
    notes        VARCHAR(500) not null,
    quantity     INT          not null
);

