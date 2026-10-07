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

## Status

Work in progress.

- [x] accounts
- [x] deposits
- [ ] transfers between accounts
- [ ] idempotency keys
- [ ] account statements
