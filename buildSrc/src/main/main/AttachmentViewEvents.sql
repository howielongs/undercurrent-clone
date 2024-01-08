create table AttachmentViewEvents
(
    id             INTEGER
        primary key autoincrement,
    status         VARCHAR(1000),
    created_date   TEXT          not null,
    updated_date   TEXT          not null,
    expiry_epoch   BIGINT,
    attachment_id  INT           not null
        constraint fk_AttachmentViewEvents_attachment_id__id
            references Attachments
            on update restrict on delete restrict,
    viewer_user_id INT           not null
        constraint fk_AttachmentViewEvents_viewer_user_id__id
            references Users
            on update restrict on delete restrict,
    location_tag   VARCHAR(1000) not null,
    raw_context    VARCHAR(10000)
);

