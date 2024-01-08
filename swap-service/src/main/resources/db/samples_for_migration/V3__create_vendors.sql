CREATE TABLE IF NOT EXISTS Vendors (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       status VARCHAR(1000) DEFAULT '' NOT NULL,
    created_date TEXT NOT NULL,
    updated_date TEXT NOT NULL,
    expiry_epoch BIGINT NULL,
    user_id INT NOT NULL,
    name_tag VARCHAR(200) NOT NULL,
    CONSTRAINT fk_Vendors_user_id__id FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT ON UPDATE RESTRICT
    )