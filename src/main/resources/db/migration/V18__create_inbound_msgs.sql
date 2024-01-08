CREATE TABLE IF NOT EXISTS InboundMessages
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(10000) DEFAULT '' NOT NULL,
    created_date TEXT                      NOT NULL,
    updated_date TEXT                      NOT NULL,
    expiry_epoch BIGINT NULL,
    channel_id   INT                       NOT NULL,
    body         VARCHAR(10000)            NOT NULL,
    "timestamp"  BIGINT                    NOT NULL,
    CONSTRAINT fk_InboundMessages_channel_id__id FOREIGN KEY (channel_id) REFERENCES Channels (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);