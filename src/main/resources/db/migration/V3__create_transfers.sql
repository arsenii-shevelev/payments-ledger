create table transfers (
    id uuid primary key,
    from_account_id uuid not null references accounts (id),
    to_account_id uuid not null references accounts (id),
    amount numeric(19, 2) not null,
    created_at timestamp with time zone not null,
    constraint transfers_amount_positive check (amount > 0),
    constraint transfers_different_accounts check (from_account_id <> to_account_id)
);

alter table ledger_entries add column transfer_id uuid references transfers (id);
