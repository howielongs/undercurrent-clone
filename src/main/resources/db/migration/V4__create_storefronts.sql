CREATE TABLE IF NOT EXISTS Storefronts
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    status        VARCHAR(1000) DEFAULT ''                                                                             NOT NULL,
    created_date  TEXT                                                                                                 NOT NULL,
    updated_date  TEXT                                                                                                 NOT NULL,
    expiry_epoch  BIGINT NULL,
    vendor_id     INT                                                                                                  NOT NULL,
    display_name  VARCHAR(100)  DEFAULT ''                                                                             NOT NULL,
    logo_img_path VARCHAR(100)  DEFAULT ''                                                                             NOT NULL,
    welcome_msg   VARCHAR(2000) DEFAULT 'Welcome to my shop! Thanks for taking the time to stay private and secure!\n' NOT NULL,
    join_code     VARCHAR(1000)                                                                                        NOT NULL,
    CONSTRAINT fk_Storefronts_vendor_id__id FOREIGN KEY (vendor_id) REFERENCES Vendors (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
