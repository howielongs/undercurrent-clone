alter table Channels
    rename to msgs_channels;

alter table InboundMessages
    rename to msgs_inbound;

alter table AdminAccessTable
    rename to system_admins;

alter table CryptoReceiveEvent
    rename to DEFUNCT_crypto_receive_events;


alter table CryptoWalletEvent
    rename to DEFUNCT_crypto_wallet_events;

alter table Ancestors
    rename to data_ancestors;


alter table OldMessages
    rename to msgs_old;

alter table TestInputs
    rename to msgs_input_test;