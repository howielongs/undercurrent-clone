SELECT
    o.id,
    o.status,
    o.created_date,
    o.updated_date,
    o.order_code,
    o.invoice_id,
    o.customer_id,
    o.zipcode,
    o.tracking_number,
    cda.address AS crypto_address,
    cda.crypto_type,
    ROUND(CAST(ca.crypto_atomic_amount AS REAL) / 100000000, 8) AS btc_amount
FROM shop_orders o
         JOIN shop_customers sc ON o.customer_id = sc.id
         JOIN crypto_deposit_addresses cda ON sc.user_id = cda.user_id
         JOIN shop_order_invoices soi ON o.id = soi.order_id
         JOIN crypto_amounts ca ON soi.total_amount_id = ca.id
         LEFT JOIN crypto_fiat_exchange_rates cfer ON ca.exchange_rate_id = cfer.id AND cfer.crypto_type = 'BTC'
WHERE o.vendor_id = 5
  AND o.status NOT IN ('CANCELED', 'DECLINED')
  AND cda.created_date = (
    SELECT MAX(created_date)
    FROM crypto_deposit_addresses
    WHERE user_id = sc.user_id
);


