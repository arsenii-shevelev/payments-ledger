# payments-ledger

![CI](https://github.com/arsenii-shevelev/payments-ledger/actions/workflows/ci.yml/badge.svg)

A small payments ledger service: accounts, deposits and transfers with double-entry bookkeeping.
Built with Java 21, Spring Boot 4 and PostgreSQL.

## Running it

Need Java 21 and Docker.

```
./mvnw spring-boot:run
```

Spring Boot starts Postgres from `compose.yaml` on its own.

```
./mvnw test      # unit tests
./mvnw verify    # unit + integration tests (Testcontainers)
```

## API

| Method | Path | |
|---|---|---|
| POST | `/accounts` | open an account |
| GET | `/accounts/{id}` | get an account |
| POST | `/accounts/{id}/deposits` | deposit money |
| POST | `/transfers` | transfer money between accounts |
| GET | `/transfers/{id}` | get a transfer |

## Transfers

A transfer locks both accounts (`select ... for update`) in the order of their ids, so two transfers going in opposite directions can't deadlock.
It writes two ledger entries: a negative one for the sender and a positive one for the receiver, so every transfer adds up to zero.

Transfers between different currencies or without enough money on the account are rejected with `422`.

## Status

Work in progress.

- [x] accounts
- [x] deposits
- [x] transfers between accounts
- [ ] idempotency keys
- [ ] account statements
