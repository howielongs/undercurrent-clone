create table CryptoAddresses_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default ''    not null,
    created_date TEXT                         not null,
    updated_date TEXT                         not null,
    expiry_epoch BIGINT,
    user_id      INT
        constraint fk_CryptoAddresses_user_id__id
            references Users
            on update restrict on delete restrict,
    address      VARCHAR(10000)               not null,
    crypto_type  VARCHAR(10000) default 'BTC' not null
);

insert into CryptoAddresses_dg_tmp(id, status, created_date, updated_date, expiry_epoch, user_id, address, crypto_type)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       user_id,
       address,
       crypto_type
from CryptoAddresses;

drop table CryptoAddresses;

alter table CryptoAddresses_dg_tmp
    rename to CryptoAddresses;






create table DepositCryptoAddresses_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default ''    not null,
    created_date TEXT                         not null,
    updated_date TEXT                         not null,
    expiry_epoch BIGINT,
    user_id      INT
        constraint fk_DepositCryptoAddresses_user_id__id
            references Users
            on update restrict on delete restrict,
    address      VARCHAR(10000)               not null,
    crypto_type  VARCHAR(10000) default 'BTC' not null
);

insert into DepositCryptoAddresses_dg_tmp(id, status, created_date, updated_date, expiry_epoch, user_id, address,
                                          crypto_type)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       user_id,
       address,
       crypto_type
from DepositCryptoAddresses;

drop table DepositCryptoAddresses;

alter table DepositCryptoAddresses_dg_tmp
    rename to DepositCryptoAddresses;






create table CartItems_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    order_id     INT            default NULL
        constraint fk_CartItems_order_id__id
            references DeliveryOrders
            on update restrict on delete restrict,
    sku_id       INT                       not null
        constraint fk_CartItems_sku_id__id
            references SaleItems
            on update restrict on delete restrict,
    customer_id  INT                       not null
        constraint fk_CartItems_customer_id__id
            references Customers (id)
            on update restrict on delete restrict,
    notes        VARCHAR(10000)            not null,
    quantity     INT                       not null
);

insert into CartItems_dg_tmp(id, status, created_date, updated_date, expiry_epoch, order_id, sku_id, customer_id, notes,
                             quantity)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       order_id,
       sku_id,
       customer_id,
       notes,
       quantity
from CartItems;

drop table CartItems;

alter table CartItems_dg_tmp
    rename to CartItems;





create table ExchangeRates_dg_tmp
(
    id                                  INTEGER
        primary key autoincrement,
    status                              VARCHAR(10000) default '' not null,
    created_date                        TEXT                      not null,
    updated_date                        TEXT                      not null,
    expiry_epoch                        BIGINT,
    crypto_type                         VARCHAR(10000)            not null,
    fiat_type                           VARCHAR(10000)            not null,
    fiat_to_crypto_atomic_exchange_rate VARCHAR(10000)            not null
);

insert into ExchangeRates_dg_tmp(id, status, created_date, updated_date, expiry_epoch, crypto_type, fiat_type,
                                 fiat_to_crypto_atomic_exchange_rate)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       crypto_type,
       fiat_type,
       fiat_to_crypto_atomic_exchange_rate
from ExchangeRates;

drop table ExchangeRates;

alter table ExchangeRates_dg_tmp
    rename to ExchangeRates;








create table Invoices_dg_tmp
(
    id                    INTEGER
        primary key autoincrement,
    status                VARCHAR(10000) default '' not null,
    created_date          TEXT                      not null,
    updated_date          TEXT                      not null,
    expiry_epoch          BIGINT,
    order_id              INT
        constraint fk_Invoices_order_id__id
            references DeliveryOrders
            on update restrict on delete restrict,
    exchange_rate_id      INT                       not null
        constraint fk_Invoices_exchange_rate_id__id
            references ExchangeRates
            on update restrict on delete restrict,
    subtotal_amount_id    INT                       not null
        constraint fk_Invoices_subtotal_amount_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    fees_amount_id        INT                       not null
        constraint fk_Invoices_fees_amount_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    total_amount_id       INT                       not null
        constraint fk_Invoices_total_amount_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    split_fees_amount_id  INT                       not null
        constraint fk_Invoices_split_fees_amount_id__id
            references CryptoAmounts
            on update restrict on delete restrict,
    fee_pct               VARCHAR(10000)            not null,
    raw                   VARCHAR(10000)            not null,
    receipt               VARCHAR(10000)            not null,
    last_nudged_timestamp BIGINT
);

insert into Invoices_dg_tmp(id, status, created_date, updated_date, expiry_epoch, order_id, exchange_rate_id,
                            subtotal_amount_id, fees_amount_id, total_amount_id, split_fees_amount_id, fee_pct, raw,
                            receipt, last_nudged_timestamp)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       order_id,
       exchange_rate_id,
       subtotal_amount_id,
       fees_amount_id,
       total_amount_id,
       split_fees_amount_id,
       fee_pct,
       raw,
       receipt,
       last_nudged_timestamp
from Invoices;

drop table Invoices;

alter table Invoices_dg_tmp
    rename to Invoices;







create table SaleItems_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default '' not null,
    created_date TEXT                      not null,
    updated_date TEXT                      not null,
    expiry_epoch BIGINT,
    product_id   INT                       not null
        constraint fk_SaleItems_product_id__id
            references Products
            on update restrict on delete restrict,
    price        VARCHAR(10000)            not null,
    unit_size    VARCHAR(10000)            not null
);

insert into SaleItems_dg_tmp(id, status, created_date, updated_date, expiry_epoch, product_id, price, unit_size)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       product_id,
       price,
       unit_size
from SaleItems;

drop table SaleItems;

alter table SaleItems_dg_tmp
    rename to SaleItems;











create table TestInputs_dg_tmp
(
    id                INTEGER
        primary key autoincrement,
    status            VARCHAR(10000) default '' not null,
    created_date      TEXT                      not null,
    updated_date      TEXT                      not null,
    expiry_epoch      BIGINT,
    message_id        INT
        constraint fk_TestInputs_message_id__id
            references Messages (id)
            on update restrict on delete restrict,
    body              VARCHAR(10000) default '' not null,
    num_msgs_expected INT,
    good_substrings   VARCHAR(10000) default '' not null,
    bad_substrings    VARCHAR(10000) default '' not null,
    case_sensitive    BOOLEAN        default 1  not null,
    sender            VARCHAR(10000) default '' not null,
    receiver          VARCHAR(10000) default '' not null,
    role              VARCHAR(10000),
    timestamp         BIGINT                    not null
);

insert into TestInputs_dg_tmp(id, status, created_date, updated_date, expiry_epoch, message_id, body, num_msgs_expected,
                              good_substrings, bad_substrings, case_sensitive, sender, receiver, role, timestamp)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       message_id,
       body,
       num_msgs_expected,
       good_substrings,
       bad_substrings,
       case_sensitive,
       sender,
       receiver,
       role,
       timestamp
from TestInputs;

drop table TestInputs;

alter table TestInputs_dg_tmp
    rename to TestInputs;


