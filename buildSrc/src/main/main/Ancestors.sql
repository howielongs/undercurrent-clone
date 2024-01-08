create table Ancestors
(
    id            INTEGER
        primary key autoincrement,
    status        VARCHAR(1000),
    created_date  TEXT          not null,
    updated_date  TEXT          not null,
    expiry_epoch  BIGINT,
    owner_user_id INT           not null
        constraint fk_Ancestors_owner_user_id__id
            references Users
            on update restrict on delete restrict,
    owner_role    VARCHAR(1000),
    entity_type   VARCHAR(1000) not null,
    memo          VARCHAR(1000) not null,
    old_entity_id INT           not null,
    new_entity_id INT           not null
);

