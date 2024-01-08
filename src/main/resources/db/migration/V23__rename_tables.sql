alter table Users
    rename to system_users;

drop table if exists ReferralCodes;

alter table Invoices
    rename to shop_order_invoices;
alter table DeliveryOrders
    rename to shop_orders;

alter table MobAccounts
    rename to mobilecoin_accounts;

alter table MobReceivedEvents
    rename to mobilecoin_received_events;

alter table BtcReceivedEvents
    rename to bitcoin_receive_events;

alter table IntroEvents
    rename to system_intro_events;

alter table ScanEvents
    rename to system_scan_events;

alter table StripeApiKeys
    rename to stripe_api_keys;

alter table StripePaymentLinks
    rename to stripe_payment_links;

alter table StripePrices
    rename to stripe_prices;

alter table JoinCodeUsages
    rename to shop_join_code_usages;

alter table AttachmentLinks
    rename to attachment_file_links;

alter table AttachmentViewEvents
    rename to attachment_file_view_events;

alter table Attachments
    rename to attachment_file_paths;

alter table BtcWalletEvents
    rename to bitcoin_wallet_events;

alter table ZipCodeLookups
    rename to address_zipcodes;







