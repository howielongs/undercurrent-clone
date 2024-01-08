DELETE FROM msgs_raw
WHERE id NOT IN (
    SELECT MIN(id)
    FROM msgs_raw
    GROUP BY timestamp
);

CREATE UNIQUE INDEX IF NOT EXISTS msgs_raw_timestamp
    on msgs_raw (timestamp);
