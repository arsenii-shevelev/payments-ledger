create table accounts (
    id uuid primary key,
    owner_name varchar(100) not null,
    currency varchar(3) not null,
    balance numeric(19, 2) not null default 0,
    created_at timestamp with time zone not null,
    constraint accounts_balance_not_negative check (balance >= 0)
);
