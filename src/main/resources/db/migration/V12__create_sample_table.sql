CREATE TABLE IF NOT EXISTS sample_thingy
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    created_date TEXT NOT NULL,
    updated_date TEXT NOT NULL,
    expiry_epoch BIGINT NULL,
    age          VARCHAR(200)
);
