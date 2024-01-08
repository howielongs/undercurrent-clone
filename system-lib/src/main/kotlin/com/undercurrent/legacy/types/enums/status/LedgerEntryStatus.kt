package com.undercurrent.legacy.types.enums.status

/**
 * Canceling status pairs:
 *   CUSTOMER
 * 1. AWAITING
 * 2. RECEIVED
 *
 * Admin and Vendor statuses:
 * 1. PENDING -> AVAILABLE
 * 2. SENT
 */
enum class LedgerEntryStatus {

    //todo figure out how to handle cancellation of order -> don't want to have lingering debit for expired order
    AWAITING, //keep this for original entries
    RECEIVED,  // use this when amount received, but not yet verified

    PENDING,
    AVAILABLE, // update admin and vendor AWAITING status to this once 'received' (may want logic to wait for 'verified' as well)

    OUTBOX,
    SENT, // create new entries to cancel out AVAILABLE entries

    /**
     * this is when BTC wallet hasn't yet updated its available balance
     *
     * for something like "FROM_CUSTOMER", waiting incoming payment event
     */


    //incoming amount will get a second entry, which should ideally cancel out the DEBIT at the start of the order

}