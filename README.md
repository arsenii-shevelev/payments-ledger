# payments-ledger

![CI](https://github.com/arsenii-shevelev/payments-ledger/actions/workflows/ci.yml/badge.svg)

A small payments ledger with accounts, deposits and transfers between them. Every change of a balance is also written to a ledger, so the history is always there.
Java 21, Spring Boot 4, PostgreSQL.

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
| GET | `/accounts/{id}/statement?from=&to=` | account statement |

## Transfers

Both accounts get locked with `select ... for update`, always the one with the smaller id first. Otherwise two transfers going opposite ways could deadlock.

Transfers are double-entry. Each one adds two ledger entries, minus on the sender's side and plus on the receiver's, so together they come to zero.

If the currencies don't match or there isn't enough money, you get `422`.

### Retries

You can send an `Idempotency-Key` header with `POST /transfers`. If the same request comes in again with that key, say after a timeout, you get the first transfer back and nothing is moved twice.
Keys only have to be unique per sending account. Using the same key for a different transfer returns `422`.

## Statements

`GET /accounts/{id}/statement?from=2026-10-01&to=2026-10-31` gives the ledger entries for those days and the balance before and after them. Dates are in UTC and both days count.
Entries come in pages, use `page` and `size` (50 by default, 200 at most). The balances are always for the whole period, not just the page.

## Auth

No auth here. The idea is that a gateway in front of the service checks who's calling and which accounts they can touch.

## Status

Work in progress.

- [x] accounts
- [x] deposits
- [x] transfers between accounts
- [x] idempotency keys for transfers
- [x] account statements
- [ ] concurrency test for transfers
- [ ] Dockerfile
