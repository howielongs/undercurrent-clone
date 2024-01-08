create table StripePaymentLinks
(
    id                   INTEGER
        primary key autoincrement,
    status               VARCHAR(1000),
    created_date         TEXT           not null,
    updated_date         TEXT           not null,
    expiry_epoch         BIGINT,
    order_id             INT            not null
        constraint fk_StripePaymentLinks_order_id__id
            references DeliveryOrders
            on update restrict on delete restrict,
    payment_link_id      VARCHAR(10000) not null,
    payment_url          VARCHAR(10000) not null,
    checkout_session_sid VARCHAR(10000),
    payment_intent_sid   VARCHAR(10000)
);

