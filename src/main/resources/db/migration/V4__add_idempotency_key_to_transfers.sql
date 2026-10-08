alter table transfers add column idempotency_key varchar(100);
alter table transfers add constraint transfers_idempotency_key_unique unique (idempotency_key);
