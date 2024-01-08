create table Storefronts
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT                                                                                               not null,
    updated_date TEXT                                                                                               not null,
    expiry_epoch BIGINT,
    vendor_id    INT                                                                                                not null
        constraint fk_Storefronts_vendor_id__id
            references VendorAccessTable
            on update restrict on delete restrict,
    display_name VARCHAR(10000),
    welcome_msg  VARCHAR(5000) default 'Welcome to my shop! Thanks for taking the time to stay private and secure!' not null
);

