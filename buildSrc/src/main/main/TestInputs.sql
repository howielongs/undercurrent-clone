create table TestInputs
(
    id                INTEGER
        primary key autoincrement,
    status            VARCHAR(1000),
    created_date      TEXT                      not null,
    updated_date      TEXT                      not null,
    expiry_epoch      BIGINT,
    message_id        INT
        constraint fk_TestInputs_message_id__id
            references OldMessages
            on update restrict on delete restrict,
    body              VARCHAR(10000) default '' not null,
    num_msgs_expected INT,
    good_substrings   VARCHAR(10000) default '' not null,
    bad_substrings    VARCHAR(10000) default '' not null,
    case_sensitive    BOOLEAN        default 1  not null,
    sender            VARCHAR(100)   default '' not null,
    receiver          VARCHAR(100)   default '' not null,
    role              VARCHAR(100),
    timestamp         BIGINT                    not null
);

