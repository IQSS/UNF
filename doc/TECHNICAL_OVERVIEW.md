# UNF Dataverse Java Package: Technical Overview

## Purpose

This package computes **UNF (Universal Numerical Fingerprint)** values for tabular and vector-like data.
A UNF is a canonical, content-based signature designed to remain stable across storage formats.

At a high level, the package:
1. Canonicalizes each value (numbers, strings, booleans, bitfields, date/time text).
2. Appends value delimiters/sentinels in a deterministic way.
3. Hashes canonical bytes with `SHA-256`.
4. Truncates to the most significant 128 bits.
5. Encodes as Base64 and prefixes with `UNF:<version>[:extensions]:`.

Current version string is `6` (`UnfDigest.currentVersion`).

## Public API Surface

Primary entry point: `org.dataverse.unf.UNFUtil`

`UNFUtil` exposes overloaded `calculateUNF(...)` methods for:
- `double[]`, `float[]`, `short[]`, `byte[]`, `long[]`, `int[]`, `Number[]`
- `boolean[]`
- `String[]` (plain string or precomputed UNFs)
- `String[]` + format arrays for date/time canonicalization
- `double[][]`, `String[][]` (column-wise UNFs, then combine)
- `BitString[]`
- `List<T>` (numeric and string-like paths)

## Core Processing Pipeline

### 1. API Normalization (`UNFUtil`)

`UNFUtil` adapts type-specific inputs to internal forms expected by `UnfDigest`:
- Primitive numerics are widened to `double`/`Double`.
- `boolean[]` are boxed and mapped to `Boolean[]`.
- Date/time strings can be transformed through `UnfDateFormatter` into normalized representations.
- `String[]` beginning with `UNF:` can be combined directly via `UnfDigest.addUNFs(...)`.

### 2. Digest Orchestration (`UnfDigest`)

`UnfDigest` is the orchestrator and version/prefix authority.

Responsibilities:
- Dispatches by input family (Number, CharSequence, Boolean, BitString).
- Handles matrix transpose behavior (`setTrnps(...)`) for column-wise hashing.
- Builds output prefix `UNF:<version>[:extensions]:...`.
- Optionally records `UnfClass signature` metadata (fingerprints, hex, b64, extension tags).
- Combines multiple column UNFs through `addUNFs(...)` by sorting then re-hashing.

### 3. Type-Specific Canonicalization + Hash Feed

Type handlers:
- `UnfNumber`: numeric canonicalization and hashing.
- `UnfString`: string canonicalization and hashing.
- `UnfBoolean`: maps values to `"1"` / `"0"`, then hashes.
- `UnfBitfield`: bitstring-specific canonicalization and hashing.

Each handler:
- Updates a `MessageDigest` (`SHA-256` default).
- Feeds canonical bytes plus missing-value sentinel bytes where needed.
- Truncates digest to 128 bits.
- Base64-encodes truncated bytes.

### 4. Canonicalization Utilities

Important utility classes:
- `RoundRoutines`: canonical numeric formatting, including special values and exponent formatting.
- `RoundString`: truncates string values to configured character length and appends line terminator.
- `UnfDigestUtils`: transpose helpers, row counting, missing checks, zero-padding cleanup.
- `UtilsConverter`: byte conversion and hex utilities.
- `Base64Encoding`: deterministic Base64 encoding used by UNF output.

### 5. Date/Time Canonicalization

`UNFUtil.calculateUNF(String[] values, String[] sdfFormat)` and the interval overload:
- Parses each value with provided `SimpleDateFormat` pattern.
- Builds canonical UNF-compatible format with `UnfDateFormatter`.
- Applies UTC when timezone fields are present.
- Trims trailing fractional-second zeros.
- Hashes normalized strings through the standard CharSequence path.

## Key Components and Roles

- `UNFUtil`: convenience facade and overload-heavy API.
- `UnfDigest`: pipeline coordinator and UNF string constructor.
- `UnfClass`: in-memory metadata object for fingerprints/hex/base64 per processed vector.
- `UnfNumber`, `UnfString`, `UnfBoolean`, `UnfBitfield`: type-specific canonicalization and digest feed logic.
- `RoundRoutines`, `RoundString`: canonical text generation for stable hashing.
- `UnfDateFormatter`: maps arbitrary date/time patterns to canonical UNF-compatible patterns.
- `BitString`: validates/normalizes binary literals used by bitfield hashing.
- `UnfCons`: central constants for defaults and algorithm behavior.

## Data Model and Orientation

The library generally treats a 2D input as a matrix where UNFs are computed per column.
Transpose behavior is controlled by `UnfDigest.setTrnps(...)`.
`UNFUtil` typically sets this to `false` for 1D wrapper calls.

## Combination Strategy

When combining multiple UNFs:
1. Strip the prefix payload (`UNF:...:`) to extract base64 bodies.
2. Sort bodies lexicographically.
3. Re-hash sorted list using string path with default character precision.
4. Emit a single composite UNF string.

This makes dataset-level signatures deterministic regardless of original column order in some composition scenarios.

## Defaults and Configuration Points

Defined in `UnfCons` and constructors:
- Numeric significant digits (`DEF_NDGTS = 7`)
- Character cutoff (`DEF_CDGTS = 128`)
- Hash size (`DEF_HSZ = 128`)
- Null-byte and sentinel behavior for canonical stream composition
- UNF object/signature accumulation behavior

`UnfClass` extension tags (for non-default settings) are encoded as:
- `X<cdigits>` for character precision
- `N<ndigits>` for numeric precision
- `H<hsize>` for hash-size variation

## Verification and Test Coverage

Current automated tests are in `src/test/java/org/dataverse/unf/UNF6UtilTest.java`.
The test style is fixture-driven with expected UNFs in `src/test/resources/test/*`.
Covered input families:
- Double, Float, Short, Byte, Long, Int
- Boolean
- String
- DateTime string+format
- BitString

## Notes for Maintainers

- The implementation is stateful in places (`UnfDigest` static fields such as `trnps`, `signature`, `fingerprint`), so call ordering and shared use should be considered when embedding in concurrent contexts.
- Behavior is intentionally tied to canonical formatting rules, not to Java default string rendering.
- Backward compatibility requires preserving canonicalization semantics, not just method signatures.
