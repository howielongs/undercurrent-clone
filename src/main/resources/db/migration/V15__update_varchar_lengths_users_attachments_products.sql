create table Users_dg_tmp
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(10000) default ''         not null,
    created_date TEXT                              not null,
    updated_date TEXT                              not null,
    expiry_epoch BIGINT,
    number       VARCHAR(10000)                    not null,
    role         VARCHAR(10000) default 'CUSTOMER' not null,
    uuid         VARCHAR(10000)
);

insert into Users_dg_tmp(id, status, created_date, updated_date, expiry_epoch, number, role, uuid)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       number,
       role,
       uuid
from Users;

drop table Users;

alter table Users_dg_tmp
    rename to Users;

create unique index Users_number
    on Users (number);





create table AttachmentLinks_dg_tmp
(
    id                       INTEGER
        primary key autoincrement,
    status                   VARCHAR(10000) default '' not null,
    created_date             TEXT                      not null,
    updated_date             TEXT                      not null,
    expiry_epoch             BIGINT,
    parent_attachment_id     INT                       not null
        constraint fk_AttachmentLinks_parent_attachment_id__id
            references Attachments
            on update restrict on delete restrict,
    attachment_type          VARCHAR(10000)            not null,
    parent_entity_id         INT,
    parent_entity_class_name VARCHAR(10000),
    caption                  VARCHAR(10000) default '' not null
);

insert into AttachmentLinks_dg_tmp(id, status, created_date, updated_date, expiry_epoch, parent_attachment_id,
                                   attachment_type, parent_entity_id, parent_entity_class_name, caption)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       parent_attachment_id,
       attachment_type,
       parent_entity_id,
       parent_entity_class_name,
       caption
from AttachmentLinks;

drop table AttachmentLinks;

alter table AttachmentLinks_dg_tmp
    rename to AttachmentLinks;





create table Attachments_dg_tmp
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(10000) default ''         not null,
    created_date  TEXT                              not null,
    updated_date  TEXT                              not null,
    expiry_epoch  BIGINT,
    owner_user_id INT                               not null
        constraint fk_Attachments_owner_user_id__id
            references Users
            on update restrict on delete restrict,
    owner_role    VARCHAR(10000) default 'CUSTOMER' not null,
    path          VARCHAR(10000)                    not null,
    caption       VARCHAR(10000)                    not null
);

insert into Attachments_dg_tmp(id, status, created_date, updated_date, expiry_epoch, owner_user_id, owner_role, path,
                               caption)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       owner_user_id,
       owner_role,
       path,
       caption
from Attachments;

drop table Attachments;

alter table Attachments_dg_tmp
    rename to Attachments;



create table AttachmentViewEvents_dg_tmp
(
    id             INTEGER
        primary key autoincrement,
    status         VARCHAR(10000) default '' not null,
    created_date   TEXT                      not null,
    updated_date   TEXT                      not null,
    expiry_epoch   BIGINT,
    attachment_id  INT                       not null
        constraint fk_AttachmentViewEvents_attachment_id__id
            references Attachments
            on update restrict on delete restrict,
    viewer_user_id INT                       not null
        constraint fk_AttachmentViewEvents_viewer_user_id__id
            references Users
            on update restrict on delete restrict,
    location_tag   VARCHAR(10000)            not null,
    raw_context    VARCHAR(10000)
);

insert into AttachmentViewEvents_dg_tmp(id, status, created_date, updated_date, expiry_epoch, attachment_id,
                                        viewer_user_id, location_tag, raw_context)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       attachment_id,
       viewer_user_id,
       location_tag,
       raw_context
from AttachmentViewEvents;

drop table AttachmentViewEvents;

alter table AttachmentViewEvents_dg_tmp
    rename to AttachmentViewEvents;




create table Products_dg_tmp
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(10000) default '' not null,
    created_date  TEXT                      not null,
    updated_date  TEXT                      not null,
    expiry_epoch  BIGINT,
    storefront_id INT                       not null
        constraint fk_Products_storefront_id__id
            references Storefronts
            on update restrict on delete restrict,
    name          VARCHAR(10000)            not null,
    details       VARCHAR(10000)            not null
);

insert into Products_dg_tmp(id, status, created_date, updated_date, expiry_epoch, storefront_id, name, details)
select id,
       status,
       created_date,
       updated_date,
       expiry_epoch,
       storefront_id,
       name,
       details
from Products;

drop table Products;

alter table Products_dg_tmp
    rename to Products;




