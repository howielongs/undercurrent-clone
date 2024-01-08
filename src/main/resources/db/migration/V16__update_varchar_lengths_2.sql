create table StorefrontPrefs_dg_tmp
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(10000) default '' not null,
    created_date  TEXT                      not null,
    updated_date  TEXT                      not null,
    expiry_epoch  BIGINT,
    storefront_id INT                       not null
        constraint fk_StorefrontPrefs_storefront_id__id
            references Storefronts
            on update restrict on delete restrict,
    key           VARCHAR(10000)            not null,
    value         VARCHAR(10000)            not null,
    datatype      VARCHAR(10000)            not null
);

insert into StorefrontPrefs_dg_tmp(id, status, created_date, updated_date, expiry_epoch, storefront_id, key, value,
                                   datatype)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       storefront_id,
    key,
    value,
    datatype
from StorefrontPrefs;

drop table StorefrontPrefs;

alter table StorefrontPrefs_dg_tmp
    rename to StorefrontPrefs;




create table CryptoSendEvents_dg_tmp
(
    id               INTEGER
        primary key autoincrement,
    status           VARCHAR(10000) default '' not null,
    created_date     TEXT                      not null,
    updated_date     TEXT                      not null,
    expiry_epoch     BIGINT,
    invoice_id       INT                       not null
        constraint fk_CryptoSendEvents_invoice_id__id
            references Invoices
            on update restrict on delete restrict,
    dest_address_id  INT                       not null
        constraint fk_CryptoSendEvents_dest_address_id__id
            references CryptoAddresses
            on update restrict on delete restrict,
    amount_crypto_id INT                       not null
        constraint fk_CryptoSendEvents_amount_crypto_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    memo             VARCHAR(10000)            not null,
    raw              VARCHAR(10000)            not null,
    raw_amount       VARCHAR(10000)            not null,
    crypto_type      VARCHAR(10000)            not null
);

insert into CryptoSendEvents_dg_tmp(id, status, created_date, updated_date, expiry_epoch, invoice_id, dest_address_id,
                                    amount_crypto_id, memo, raw, raw_amount, crypto_type)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       invoice_id,
       dest_address_id,
       amount_crypto_id,
       memo,
       raw,
       raw_amount,
       crypto_type
from CryptoSendEvents;

drop table CryptoSendEvents;

alter table CryptoSendEvents_dg_tmp
    rename to CryptoSendEvents;


create table ZipCodeLookups_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    zipcode      VARCHAR(10000)            not null,
    city         VARCHAR(10000)            not null,
    state        VARCHAR(10000)            not null,
    state_abbr   VARCHAR(10000)            not null,
    timezone     VARCHAR(10000)            not null
);

insert into ZipCodeLookups_dg_tmp(id, status, created_date, updated_date, expiry_epoch, zipcode, city, state,
                                  state_abbr, timezone)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       zipcode,
       city,
       state,
       state_abbr,
       timezone
from ZipCodeLookups;

drop table ZipCodeLookups;

alter table ZipCodeLookups_dg_tmp
    rename to ZipCodeLookups;




create table BtcReceivedEvents_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    tag          VARCHAR(10000) default '' not null,
    type         VARCHAR(10000) default '' not null,
    memo         VARCHAR(10000) default '' not null,
    raw          VARCHAR(10000) default '' not null,
    json         VARCHAR(10000) default '' not null,
    amount       VARCHAR(10000)            not null,
    rec_addr_str VARCHAR(10000)
);

insert into BtcReceivedEvents_dg_tmp(id, status, created_date, updated_date, expiry_epoch, tag, type, memo, raw, json,
                                     amount, rec_addr_str)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       tag,
       type,
       memo,
       raw,
       json,
       amount,
       rec_addr_str
from BtcReceivedEvents;

drop table BtcReceivedEvents;

alter table BtcReceivedEvents_dg_tmp
    rename to BtcReceivedEvents;







create table MobReceivedEvents_dg_tmp
(
    id                    INTEGER
        primary key autoincrement,
    status                VARCHAR(1000)  default '' not null,
    created_date          TEXT                      not null,
    updated_date          TEXT                      not null,
    expiry_epoch          BIGINT,
    tag                   VARCHAR(10000) default '' not null,
    type                  VARCHAR(10000) default '' not null,
    memo                  VARCHAR(10000) default '' not null,
    raw                   VARCHAR(10000) default '' not null,
    json                  VARCHAR(10000) default '' not null,
    mob_amount            VARCHAR(10000)            not null,
    pmob_amount           BIGINT                    not null,
    sender_sms            VARCHAR(10000)            not null,
    recipient_sms         VARCHAR(10000),
    receipt_bytes         VARCHAR(10000),
    receipt_b64           VARCHAR(10000),
    receipt_note          VARCHAR(10000),
    receipt_json          VARCHAR(10000),
    receiver_request_json VARCHAR(10000)
);

insert into MobReceivedEvents_dg_tmp(id, status, created_date, updated_date, expiry_epoch, tag, type, memo, raw, json,
                                     mob_amount, pmob_amount, sender_sms, recipient_sms, receipt_bytes, receipt_b64,
                                     receipt_note, receipt_json, receiver_request_json)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       tag,
       type,
       memo,
       raw,
       json,
       mob_amount,
       pmob_amount,
       sender_sms,
       recipient_sms,
       receipt_bytes,
       receipt_b64,
       receipt_note,
       receipt_json,
       receiver_request_json
from MobReceivedEvents;

drop table MobReceivedEvents;

alter table MobReceivedEvents_dg_tmp
    rename to MobReceivedEvents;





create table StripeApiKeys_dg_tmp
(
    id                INTEGER
        primary key autoincrement,
    status            VARCHAR(10000) default '' not null,
    created_date      TEXT                      not null,
    updated_date      TEXT                      not null,
    expiry_epoch      BIGINT,
    tag               VARCHAR(10000) default '' not null,
    type              VARCHAR(10000) default '' not null,
    memo              VARCHAR(10000) default '' not null,
    raw               VARCHAR(10000) default '' not null,
    json              VARCHAR(10000) default '' not null,
    storefront_id     INT                       not null
        constraint fk_StripeApiKeys_storefront_id__id
            references Storefronts
            on update restrict on delete restrict,
    stripe_secret_key VARCHAR(10000)
);

insert into StripeApiKeys_dg_tmp(id, status, created_date, updated_date, expiry_epoch, tag, type, memo, raw, json,
                                 storefront_id, stripe_secret_key)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       tag,
       type,
       memo,
       raw,
       json,
       storefront_id,
       stripe_secret_key
from StripeApiKeys;

drop table StripeApiKeys;

alter table StripeApiKeys_dg_tmp
    rename to StripeApiKeys;





create table BtcReceivedEvents_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    tag          VARCHAR(10000) default '' not null,
    type         VARCHAR(10000) default '' not null,
    memo         VARCHAR(10000) default '' not null,
    raw          VARCHAR(10000) default '' not null,
    json         VARCHAR(10000) default '' not null,
    amount       VARCHAR(10000)            not null,
    rec_addr_str VARCHAR(10000)
);

insert into BtcReceivedEvents_dg_tmp(id, status, created_date, updated_date, expiry_epoch, tag, type, memo, raw, json,
                                     amount, rec_addr_str)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       tag,
       type,
       memo,
       raw,
       json,
       amount,
       rec_addr_str
from BtcReceivedEvents;

drop table BtcReceivedEvents;

alter table BtcReceivedEvents_dg_tmp
    rename to BtcReceivedEvents;





create table Ancestors_dg_tmp
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(10000) default ''         not null,
    created_date  TEXT                              not null,
    updated_date  TEXT                              not null,
    expiry_epoch  BIGINT,
    owner_user_id INT                               not null
        constraint fk_Ancestors_owner_user_id__id
            references Users
            on update restrict on delete restrict,
    owner_role    VARCHAR(10000) default '' not null,
    entity_type   VARCHAR(10000)                    not null,
    memo          VARCHAR(10000)                    not null,
    old_entity_id INT                               not null,
    new_entity_id INT                               not null
);

insert into Ancestors_dg_tmp(id, status, created_date, updated_date, expiry_epoch, owner_user_id, owner_role,
                             entity_type, memo, old_entity_id, new_entity_id)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       owner_user_id,
       owner_role,
       entity_type,
       memo,
       old_entity_id,
       new_entity_id
from Ancestors;

drop table Ancestors;

alter table Ancestors_dg_tmp
    rename to Ancestors;






create table CryptoSendEvents_dg_tmp
(
    id               INTEGER
        primary key autoincrement,
    status           VARCHAR(10000) default '' not null,
    created_date     TEXT                      not null,
    updated_date     TEXT                      not null,
    expiry_epoch     BIGINT,
    invoice_id       INT                       not null
        constraint fk_CryptoSendEvents_invoice_id__id
            references Invoices
            on update restrict on delete restrict,
    dest_address_id  INT                       not null
        constraint fk_CryptoSendEvents_dest_address_id__id
            references CryptoAddresses
            on update restrict on delete restrict,
    amount_crypto_id INT                       not null
        constraint fk_CryptoSendEvents_amount_crypto_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    memo             VARCHAR(10000)            not null,
    raw              VARCHAR(10000)            not null,
    raw_amount       VARCHAR(10000)            not null,
    crypto_type      VARCHAR(10000)            not null
);

insert into CryptoSendEvents_dg_tmp(id, status, created_date, updated_date, expiry_epoch, invoice_id, dest_address_id,
                                    amount_crypto_id, memo, raw, raw_amount, crypto_type)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       invoice_id,
       dest_address_id,
       amount_crypto_id,
       memo,
       raw,
       raw_amount,
       crypto_type
from CryptoSendEvents;

drop table CryptoSendEvents;

alter table CryptoSendEvents_dg_tmp
    rename to CryptoSendEvents;




create table DeliveryOrders_dg_tmp
(
    id               INTEGER
        primary key autoincrement,
    status           VARCHAR(10000) default '' not null,
    created_date     TEXT                      not null,
    updated_date     TEXT                      not null,
    expiry_epoch     BIGINT,
    order_code       VARCHAR(10000)            not null,
    invoice_id       INT                       not null
        constraint fk_DeliveryOrders_invoice_id__id
            references Invoices
            on update restrict on delete restrict,
    customer_id      INT                       not null
        constraint fk_DeliveryOrders_customer_id__id
            references Customers
            on update restrict on delete restrict,
    vendor_id        INT                       not null
        constraint fk_DeliveryOrders_vendor_id__id
            references Vendors
            on update restrict on delete restrict,
    zipcode          VARCHAR(10000)            not null,
    delivery_address VARCHAR(10000)            not null,
    delivery_name    VARCHAR(10000)            not null,
    note_to_vendor   VARCHAR(10000)            not null,
    confirmed_date   TEXT,
    declined_date    TEXT,
    note_to_customer VARCHAR(10000),
    tracking_number  VARCHAR(10000)
);

insert into DeliveryOrders_dg_tmp(id, status, created_date, updated_date, expiry_epoch, order_code, invoice_id,
                                  customer_id, vendor_id, zipcode, delivery_address, delivery_name, note_to_vendor,
                                  confirmed_date, declined_date, note_to_customer, tracking_number)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       order_code,
       invoice_id,
       customer_id,
       vendor_id,
       zipcode,
       delivery_address,
       delivery_name,
       note_to_vendor,
       confirmed_date,
       declined_date,
       note_to_customer,
       tracking_number
from DeliveryOrders;

drop table DeliveryOrders;

alter table DeliveryOrders_dg_tmp
    rename to DeliveryOrders;

create unique index DeliveryOrders_order_code
    on DeliveryOrders (order_code);




create table IntroEvents_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    user_id      INT                       not null
        constraint fk_IntroEvents_user_id__id
            references Users
            on update restrict on delete restrict,
    role         VARCHAR(10000)            not null,
    event_type   VARCHAR(10000)            not null,
    memo         VARCHAR(10000)            not null
);

insert into IntroEvents_dg_tmp(id, status, created_date, updated_date, expiry_epoch, user_id, role, event_type, memo)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       user_id,
       role,
       event_type,
       memo
from IntroEvents;

drop table IntroEvents;

alter table IntroEvents_dg_tmp
    rename to IntroEvents;




create table ScanEvents_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default ''  not null,
    created_date TEXT                       not null,
    updated_date TEXT                       not null,
    expiry_epoch BIGINT,
    event_tag    VARCHAR(10000)             not null,
    env          VARCHAR(10000)             not null,
    scan_group   VARCHAR(10000)             not null,
    period       INT            default 500 not null
);

insert into ScanEvents_dg_tmp(id, status, created_date, updated_date, expiry_epoch, event_tag, env, scan_group, period)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       event_tag,
       env,
       scan_group,
       period
from ScanEvents;

drop table ScanEvents;

alter table ScanEvents_dg_tmp
    rename to ScanEvents;

