alter table msgs_out
    add send_after BIGINT;

alter table msgs_out
    add sent_at BIGINT;
