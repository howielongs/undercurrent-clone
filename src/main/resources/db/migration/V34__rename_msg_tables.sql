alter table msgs_all
    rename to msgs_out;


alter table msgs_raw
    rename to msgs_in;


alter table msg_notifications
    rename to msgs_out_notifications;

