-- auto-generated definition
create table if not exists shop_order_events
(
    id               INTEGER
        primary key autoincrement,
    created_date     TEXT           not null,
    updated_date     TEXT           not null,
    expiry_epoch     BIGINT,
    shop_order_id    INT            not null
        constraint fk_shop_order_events_shop_order_id__id
            references shop_orders
            on update restrict on delete restrict,
    event_type       VARCHAR(10000) not null,
    event_date       TEXT           not null,
    event_epoch_nano BIGINT         not null,
    notes            VARCHAR(10000) not null,
    memo             VARCHAR(10000) not null
);

