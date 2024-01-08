-- auto-generated definition
create table IF NOT EXISTS shop_join_code_burst_event
(
    id                INTEGER
        primary key autoincrement,
    created_date      TEXT                      not null,
    updated_date      TEXT                      not null,
    expiry_epoch      BIGINT,
    status            VARCHAR(10000) default '' not null,
    storefront_id     INT                       not null
        constraint fk_shop_join_code_burst_event_storefront_id__id
            references shop_storefronts
            on update restrict on delete restrict,
    initiator_user_id INT                       not null
        constraint fk_shop_join_code_burst_event_initiator_user_id__id
            references system_users
            on update restrict on delete restrict,
    qty               INT                       not null
);



CREATE TEMPORARY TABLE shop_join_codes_backup AS
SELECT *
FROM shop_join_codes;

DROP TABLE shop_join_codes;


CREATE TABLE shop_join_codes
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    status         VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date   TEXT                     NOT NULL,
    updated_date   TEXT                     NOT NULL,
    expiry_epoch   BIGINT                   NULL,
    owner_user_id  INT                      NOT NULL,
    parent_id      INT                      NULL,
    entity_id      INT                      NOT NULL,
    tag            VARCHAR(5000)            NULL,
    "value"        VARCHAR(5000)            NOT NULL,
    entity_type    VARCHAR(5000)            NOT NULL,
    burst_event_id INT                      NULL,
    CONSTRAINT fk_shop_join_codes_owner_user_id__id FOREIGN KEY (owner_user_id) REFERENCES system_users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_shop_join_codes_parent_id__id FOREIGN KEY (parent_id) REFERENCES shop_join_codes (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_shop_join_codes_burst_event_id__id FOREIGN KEY (burst_event_id) REFERENCES shop_join_code_burst_event (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);


INSERT INTO shop_join_codes (id, status, created_date, updated_date, expiry_epoch, owner_user_id, parent_id, entity_id,
                             tag, "value", entity_type)
SELECT id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       owner_user_id,
       parent_id,
       entity_id,
       tag,
       "value",
       entity_type
FROM shop_join_codes_backup;

drop table shop_join_codes_backup

