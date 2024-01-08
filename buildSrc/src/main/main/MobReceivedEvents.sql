create table MobReceivedEvents
(
    id                    INTEGER
        primary key autoincrement,
    status                VARCHAR(1000),
    created_date          TEXT           not null,
    updated_date          TEXT           not null,
    expiry_epoch          BIGINT,
    tag                   VARCHAR(10000),
    type                  VARCHAR(10000),
    memo                  VARCHAR(10000),
    raw                   VARCHAR(10000),
    mob_amount            VARCHAR(10000) not null,
    pmob_amount           BIGINT         not null,
    sender_sms            VARCHAR(1000)  not null,
    recipient_sms         VARCHAR(1000),
    receipt_bytes         VARCHAR(10000),
    receipt_b64           VARCHAR(10000),
    receipt_note          VARCHAR(10000),
    receipt_json          VARCHAR(10000),
    receiver_request_json VARCHAR(10000)
);

