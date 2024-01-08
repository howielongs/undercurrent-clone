-- Define a new table with the same structure as the original table, explicitly specifying AUTOINCREMENT for the id column
CREATE TABLE msgs_in_new
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    created_date TEXT           NOT NULL,
    updated_date TEXT           NOT NULL,
    expiry_epoch BIGINT,
    body         VARCHAR(50000) NOT NULL,
    sender_sms   VARCHAR(10000) NOT NULL,
    receiver_sms VARCHAR(10000) NOT NULL,
    dbus_path    VARCHAR(10000) NOT NULL,
    timestamp    BIGINT         NOT NULL,
    uuid         VARCHAR(10000),
    read_at      TEXT
);

-- Copy data from the original table to the new table
INSERT INTO msgs_in_new
SELECT *
FROM msgs_in;

-- Drop the original table
DROP TABLE msgs_in;

-- Rename the new table to the original table's name
ALTER TABLE msgs_in_new
    RENAME TO msgs_in;

-- Drop the unique index
DROP INDEX IF EXISTS msgs_raw_timestamp;
