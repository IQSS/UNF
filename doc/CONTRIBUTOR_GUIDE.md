# UNF Dataverse Java Package: Contributor Guide

## Scope

This guide explains how to contribute safely to the UNF Java package while preserving output stability.

UNF behavior is sensitive: even small canonicalization changes can alter signatures and break compatibility.

## Project Layout

- `src/main/java/org/dataverse/unf/`: implementation
- `src/test/java/org/dataverse/unf/UNF6UtilTest.java`: fixture-driven unit tests
- `src/test/resources/test/`: expected-output fixtures by data type
- `doc/`: package documentation and examples

## Local Prerequisites

- JDK 17 (from `pom.xml`)
- Maven 3.x

Typical commands:
```bash
mvn clean test
mvn test
```

To run a single test class:
```bash
mvn -Dtest=UNF6UtilTest test
```

## Recommended Contribution Workflow

1. Pick a target area (API overload, canonicalization logic, utility behavior, or tests).
2. Read existing tests and corresponding fixture file(s) first.
3. Implement minimal change with explicit compatibility intent.
4. Run full tests (`mvn test`).
5. If behavior changes are intentional, update fixture expected values and document rationale in PR notes.

## How the Package Works (Contributor View)

### Public API and entry points

Most external callers use `UNFUtil.calculateUNF(...)` overloads.
`UNFUtil` performs input adaptation and delegates to `UnfDigest`.

### Digest orchestration

`UnfDigest`:
- routes data to type-specific handlers,
- controls matrix orientation (`trnps`),
- prefixes output with `UNF:<version>...`,
- combines multiple UNFs via `addUNFs(...)`.

### Canonicalization engines

- Numeric: `UnfNumber` + `RoundRoutines`
- String: `UnfString` + `RoundRoutines` / `RoundString`
- Boolean: `UnfBoolean`
- Bitfield: `UnfBitfield` + `BitString`
- Date/time: `UNFUtil` overloads + `UnfDateFormatter`

### Hash + encoding

All handlers eventually:
- feed canonical bytes into `SHA-256`,
- truncate to 128 bits,
- Base64-encode,
- return with UNF prefix.

## High-Risk Change Areas

Treat these as compatibility-sensitive:

- `RoundRoutines` and `RoundString` formatting logic
- missing/null sentinel handling (`UnfCons.missv`, null-byte behavior)
- date/time normalization and timezone treatment
- digest truncation size and Base64 conversion path
- sorting/combining logic in `UnfDigest.addUNFs(...)`

Any change in these areas can alter emitted UNFs for existing data.

## Testing Strategy

### Existing tests

`UNF6UtilTest` reads each fixture file where:
- first line = expected UNF
- remaining lines = values to hash

### When adding features

- Add or extend fixtures in `src/test/resources/test/`.
- Add clear unit coverage for new type branches or canonicalization cases.
- Include corner cases: null/missing, blanks, NaN/Infinity, timezone-bearing dates.

### Regression protection

If you intentionally change canonicalization:
- explain why prior output was incorrect or incomplete,
- update fixtures explicitly,
- include migration/backward-compatibility notes in PR description.

## Coding Conventions for This Package

- Preserve deterministic behavior.
- Prefer explicit conversions to avoid locale/platform drift.
- Keep algorithm constants centralized in `UnfCons`.
- Avoid introducing side effects in static state unless necessary.
- Keep public API overload behavior predictable and symmetric across types.

## Common Pitfalls

- Forgetting that `UnfDigest` uses static mutable state (`trnps`, `signature`, `fingerprint`).
- Changing default precision (`DEF_NDGTS`, `DEF_CDGTS`) without documenting compatibility impact.
- Updating parsing/format rules for dates without fixture updates.
- Treating formatting cleanups as cosmetic; they may be algorithmic.

## Suggested PR Checklist

- [ ] Tests pass locally (`mvn test`).
- [ ] New or changed behavior is covered by tests.
- [ ] Fixture updates are intentional and explained.
- [ ] Backward-compatibility impact is explicitly stated.
- [ ] Public API changes (if any) are documented.

## Useful Starting Points for New Contributors

- Read `UNFUtil` for API shape.
- Read `UnfDigest` for top-level flow and UNF composition.
- Read `UnfNumber` and `RoundRoutines` for numeric canonicalization details.
- Use `UNF6UtilTest` plus fixture files to understand expected outputs quickly.
