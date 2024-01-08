alter table msgs_raw
    add uuid VARCHAR(10000);

alter table msgs_all
    add uuid VARCHAR(10000);
