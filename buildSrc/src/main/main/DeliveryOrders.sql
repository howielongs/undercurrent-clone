create table DeliveryOrders
(
    id               INTEGER
        primary key autoincrement,
    status           VARCHAR(1000),
    created_date     TEXT           not null,
    updated_date     TEXT           not null,
    expiry_epoch     BIGINT,
    order_code       VARCHAR(1000)  not null,
    invoice_id       INT            not null
        constraint fk_DeliveryOrders_invoice_id__id
            references Invoices
            on update restrict on delete restrict,
    customer_id      INT            not null
        constraint fk_DeliveryOrders_customer_id__id
            references CustomerLinkages
            on update restrict on delete restrict,
    vendor_id        INT            not null
        constraint fk_DeliveryOrders_vendor_id__id
            references VendorAccessTable
            on update restrict on delete restrict,
    zipcode          VARCHAR(20)    not null,
    delivery_address VARCHAR(1000)  not null,
    delivery_name    VARCHAR(1000)  not null,
    note_to_vendor   VARCHAR(10000) not null,
    confirmed_date   TEXT,
    declined_date    TEXT,
    note_to_customer VARCHAR(1000),
    tracking_number  VARCHAR(1000)
);

create unique index DeliveryOrders_order_code
    on DeliveryOrders (order_code);

