create table Attachments
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(1000),
    created_date  TEXT                             not null,
    updated_date  TEXT                             not null,
    expiry_epoch  BIGINT,
    owner_user_id INT                              not null
        constraint fk_Attachments_owner_user_id__id
            references Users
            on update restrict on delete restrict,
    owner_role    VARCHAR(1000) default 'CUSTOMER' not null,
    path          VARCHAR(1000)                    not null,
    caption       VARCHAR(10000)                   not null
);

