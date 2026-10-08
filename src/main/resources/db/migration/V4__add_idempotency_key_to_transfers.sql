alter table transfers add column idempotency_key varchar(100);
alter table transfers add constraint transfers_idempotency_key_unique unique (from_account_id, idempotency_key);
