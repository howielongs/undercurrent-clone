CREATE TABLE IF NOT EXISTS Messages
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date TEXT                     NOT NULL,
    updated_date TEXT                     NOT NULL,
    expiry_epoch BIGINT                   NULL,
    user_id      INT                      NULL,
    body         VARCHAR(50000)           NOT NULL,
    sender       VARCHAR(1000)            NOT NULL,
    receiver     VARCHAR(1000)            NOT NULL,
    "timestamp"  BIGINT                   NULL,
    "role"       VARCHAR(1000)            NULL,
    CONSTRAINT fk_Messages_user_id__id FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS Admins
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    status       VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date TEXT                     NOT NULL,
    updated_date TEXT                     NOT NULL,
    expiry_epoch BIGINT                   NULL,
    user_id      INT                      NOT NULL,
    CONSTRAINT fk_Admins_user_id__id FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);


