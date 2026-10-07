create table ledger_entries (
    id uuid primary key,
    account_id uuid not null references accounts (id),
    type varchar(20) not null,
    amount numeric(19, 2) not null,
    created_at timestamp with time zone not null
);

create index ledger_entries_account_id_created_at_idx on ledger_entries (account_id, created_at);
