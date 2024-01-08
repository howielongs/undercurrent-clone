create table AttachmentLinks
(
    id                       INTEGER
        primary key autoincrement,
    status                   VARCHAR(1000),
    created_date             TEXT                      not null,
    updated_date             TEXT                      not null,
    expiry_epoch             BIGINT,
    parent_attachment_id     INT                       not null
        constraint fk_AttachmentLinks_parent_attachment_id__id
            references Attachments
            on update restrict on delete restrict,
    attachment_type          VARCHAR(1000)             not null,
    parent_entity_id         INT,
    parent_entity_class_name VARCHAR(1000),
    caption                  VARCHAR(10000) default '' not null
);

