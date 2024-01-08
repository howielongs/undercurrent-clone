CREATE TABLE IF NOT EXISTS Storefront
(
    vendorId    INTEGER,
    displayName TEXT,
    logoImgPath TEXT,
    welcomeMsg  TEXT,
    joinCode    TEXT UNIQUE,
    status      TEXT,
    uid         INTEGER PRIMARY KEY AUTOINCREMENT,
    createdDate TEXT,
    updatedDate TEXT,
    expiryEpoch TEXT
);