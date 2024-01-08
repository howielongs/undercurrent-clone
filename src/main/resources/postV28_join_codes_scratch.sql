CREATE TABLE shop_join_code_burst_event
(
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    created_date      TEXT                      NOT NULL,
    updated_date      TEXT                      NOT NULL,
    expiry_epoch      BIGINT                    NULL,
    status            VARCHAR(10000) DEFAULT '' NOT NULL,
    storefront_id     INT                       NOT NULL,
    initiator_user_id INT                       NOT NULL,
    qty               INT                       NOT NULL,
    CONSTRAINT fk_shop_join_code_burst_event_storefront_id__id FOREIGN KEY (storefront_id) REFERENCES shop_storefronts (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_shop_join_code_burst_event_initiator_user_id__id FOREIGN KEY (initiator_user_id) REFERENCES system_users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);


CREATE TABLE IF NOT EXISTS "shop_join_codes"
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(10000) default '' not null,
    created_date  TEXT                      not null,
    updated_date  TEXT                      not null,
    expiry_epoch  BIGINT,
    owner_user_id INT                       not null
        constraint fk_JoinCodes_owner_user_id__id
            references "system_users"
            on update restrict on delete restrict,
    parent_id     INT
        constraint fk_JoinCodes_parent_id__id
            references "shop_join_codes"
            on update restrict on delete restrict,
    entity_id     INT                       not null,
    tag           VARCHAR(10000),
    value         VARCHAR(10000)            not null,
    entity_type   VARCHAR(10000)            not null
);
CREATE UNIQUE INDEX JoinCodes_value
    on "shop_join_codes" (value);