create table JoinCodeUsages
(
    id           INTEGER
        primary key autoincrement,
    status       VARCHAR(1000),
    created_date TEXT not null,
    updated_date TEXT not null,
    expiry_epoch BIGINT,
    user_id      INT  not null
        constraint fk_JoinCodeUsages_user_id__id
            references Users
            on update restrict on delete restrict,
    join_code_id INT  not null
        constraint fk_JoinCodeUsages_join_code_id__id
            references ShopJoinCodes
            on update restrict on delete restrict
);

