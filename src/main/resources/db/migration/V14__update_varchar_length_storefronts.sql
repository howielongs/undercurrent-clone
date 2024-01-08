create table Storefronts_dg_tmp
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(10000) default ''                                                                             not null,
    created_date  TEXT                                                                                                  not null,
    updated_date  TEXT                                                                                                  not null,
    expiry_epoch  BIGINT,
    vendor_id     INT                                                                                                   not null
        constraint fk_Storefronts_vendor_id__id
            references Vendors
            on update restrict on delete restrict,
    display_name  VARCHAR(10000) default ''                                                                             not null,
    logo_img_path VARCHAR(10000) default ''                                                                             not null,
    welcome_msg   VARCHAR(10000) default 'Welcome to my shop! Thanks for taking the time to stay private and secure!\n' not null,
    join_code     VARCHAR(10000)                                                                                        not null
);

insert into Storefronts_dg_tmp(id, status, created_date, updated_date, expiry_epoch, vendor_id, display_name,
                               logo_img_path, welcome_msg, join_code)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       vendor_id,
       display_name,
       logo_img_path,
       welcome_msg,
       join_code
from Storefronts;

drop table Storefronts;

alter table Storefronts_dg_tmp
    rename to Storefronts;

