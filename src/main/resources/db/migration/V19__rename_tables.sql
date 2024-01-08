drop table if exists AdminAccessTable;
create table if not exists AdminAccessTable
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    user_id      INT                       not null
        constraint fk_Admins_user_id__id
            references Users
            on update restrict on delete restrict
);

insert into AdminAccessTable(id, status, created_date, updated_date, expiry_epoch, user_id)
select id, status, created_date, updated_date, expiry_epoch, user_id
from Admins;

drop table Admins;





drop table if exists CustomerLinkages;

create table if not exists CustomerLinkages
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(10000) default '' not null,
    created_date  TEXT                      not null,
    updated_date  TEXT                      not null,
    expiry_epoch  BIGINT,
    storefront_id INT                       not null
        constraint fk_Customers_storefront_id__id
            references Storefronts
            on update restrict on delete restrict,
    user_id       INT                       not null
        constraint fk_Customers_user_id__id
            references Users
            on update restrict on delete restrict
);

insert into CustomerLinkages(id, status, created_date, updated_date, expiry_epoch, storefront_id, user_id)
select id, status, created_date, updated_date, expiry_epoch, storefront_id, user_id
from Customers;

drop table Customers;



-- Is this a bad idea? Could this potentially trash data if migration run again?
drop table if exists ShopJoinCodes;
alter table  JoinCodes
    rename to ShopJoinCodes;



drop table if exists StripePrices;

create table if not exists StripePrices
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    sale_item_id INT                       not null
        constraint fk_StripePrice_sale_item_id__id
            references SaleItems
            on update restrict on delete restrict,
    price_sid    VARCHAR(10000)            not null
);

insert into StripePrices(id, status, created_date, updated_date, expiry_epoch, sale_item_id, price_sid)
select id, status, created_date, updated_date, expiry_epoch, sale_item_id, price_sid
from StripePrice;

drop table StripePrice;




drop table if exists SwapOperations;

create table if not exists SwapOperations
(
    id                    INTEGER
        primary key autoincrement,
    status                VARCHAR(10000) default '' not null,
    created_date          TEXT                      not null,
    updated_date          TEXT                      not null,
    expiry_epoch          BIGINT,
    user_id               INT                       not null
        constraint fk_SwapOperation_user_id__id
            references Users
            on update restrict on delete restrict,
    role                  VARCHAR(10000)            not null,
    from_exchange_rate_id INT                       not null
        constraint fk_SwapOperation_from_exchange_rate_id__id
            references ExchangeRates
            on update restrict on delete restrict,
    to_exchange_rate_id   INT                       not null
        constraint fk_SwapOperation_to_exchange_rate_id__id
            references ExchangeRates
            on update restrict on delete restrict,
    from_amount_id        INT                       not null
        constraint fk_SwapOperation_from_amount_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    to_amount_id          INT                       not null
        constraint fk_SwapOperation_to_amount_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    target_fiat_amount    VARCHAR(10000),
    fiat_type             VARCHAR(10000)            not null,
    from_address_id       INT                       not null
        constraint fk_SwapOperation_from_address_id__id
            references DepositCryptoAddresses
            on update restrict on delete restrict,
    to_address_id         INT                       not null
        constraint fk_SwapOperation_to_address_id__id
            references DepositCryptoAddresses
            on update restrict on delete restrict
);

insert into SwapOperations(id, status, created_date, updated_date, expiry_epoch, user_id, role, from_exchange_rate_id,
                           to_exchange_rate_id, from_amount_id, to_amount_id, target_fiat_amount, fiat_type,
                           from_address_id, to_address_id)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       user_id,
       role,
       from_exchange_rate_id,
       to_exchange_rate_id,
       from_amount_id,
       to_amount_id,
       target_fiat_amount,
       fiat_type,
       from_address_id,
       to_address_id
from SwapOperation;

drop table SwapOperation;






drop table if exists UserCreditLedgerTable;

create table if not exists UserCreditLedgerTable
(
    id               INTEGER
        primary key autoincrement,
    status           VARCHAR(10000) default '' not null,
    created_date     TEXT                      not null,
    updated_date     TEXT                      not null,
    expiry_epoch     BIGINT,
    tag              VARCHAR(10000) default '' not null,
    type             VARCHAR(10000) default '' not null,
    memo             VARCHAR(10000) default '' not null,
    raw              VARCHAR(10000) default '' not null,
    json             VARCHAR(10000) default '' not null,
    user_id          INT                       not null
        constraint fk_UserCreditLedger_user_id__id
            references Users
            on update restrict on delete restrict,
    exchange_rate_id INT
        constraint fk_UserCreditLedger_exchange_rate_id__id
            references ExchangeRates
            on update restrict on delete restrict,
    invoice_id       INT
        constraint fk_UserCreditLedger_invoice_id__id
            references Invoices
            on update restrict on delete restrict,
    role             VARCHAR(10000),
    amount           VARCHAR(10000)            not null,
    currency_type    VARCHAR(10000)            not null,
    verified_date    TEXT
);

insert into UserCreditLedgerTable(id, status, created_date, updated_date, expiry_epoch, tag, type, memo, raw, json,
                                  user_id, exchange_rate_id, invoice_id, role, amount, currency_type, verified_date)
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
       user_id,
       exchange_rate_id,
       invoice_id,
       role,
       amount,
       currency_type,
       verified_date
from UserCreditLedger;

drop table UserCreditLedger;







drop table if exists VendorAccessTable;

create table if not exists VendorAccessTable
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    user_id      INT                       not null
        constraint fk_Vendors_user_id__id
            references Users
            on update restrict on delete restrict,
    name_tag     VARCHAR(10000)            not null
);

insert into VendorAccessTable(id, status, created_date, updated_date, expiry_epoch, user_id, name_tag)
select id, status, created_date, updated_date, expiry_epoch, user_id, name_tag
from Vendors;

drop table Vendors;








drop table if exists OldMessages;

create table if not exists OldMessages
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    user_id      INT
        constraint fk_Messages_user_id__id
            references Users
            on update restrict on delete restrict,
    body         VARCHAR(50000)            not null,
    sender       VARCHAR(10000)            not null,
    receiver     VARCHAR(10000)            not null,
    timestamp    BIGINT,
    role         VARCHAR(10000)
);

insert into OldMessages(id, status, created_date, updated_date, expiry_epoch, user_id, body, sender, receiver,
                        timestamp, role)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       user_id,
       body,
       sender,
       receiver,
    timestamp,
    role
from Messages;

drop table Messages;




