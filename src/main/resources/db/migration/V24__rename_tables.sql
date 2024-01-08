alter table CryptoAmounts
    rename to crypto_amounts;

drop table if exists TextField;
-- drop table if exists CryptoReceiveEvent;
-- drop table if exists CryptoWalletEvent;
drop table if exists StripeMapping;
drop table if exists UserIntroEvent;
drop table if exists UserStats;

alter table ExchangeRates
    rename to crypto_fiat_exchange_rates;

alter table CryptoAddresses
    rename to crypto_addresses;

alter table CryptoSendEvents
    rename to crypto_send_events;

alter table DepositCryptoAddresses
    rename to crypto_deposit_addresses;

alter table StorefrontPrefs
    rename to shop_storefront_prefs;

alter table SwapOperations
    rename to swap_transactions;

alter table UserCreditLedgerTable
    rename to accounting_user_credit_ledger;









