create table StripeApiKeys
(
    id                INTEGER
        primary key autoincrement,
    status            VARCHAR(1000),
    created_date      TEXT not null,
    updated_date      TEXT not null,
    expiry_epoch      BIGINT,
    tag               VARCHAR(10000),
    type              VARCHAR(10000),
    memo              VARCHAR(10000),
    raw               VARCHAR(10000),
    storefront_id     INT  not null
        constraint fk_StripeApiKeys_storefront_id__id
            references Storefronts
            on update restrict on delete restrict,
    stripe_secret_key VARCHAR(10000)
);

