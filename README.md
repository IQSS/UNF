UNF
===

Universal Numerical Fingerprint


The Universal Numerical Fingerprint (UNF) is a cryptographic signature of the approximated semantic content of a digital object.
It is computed on the normalized (or canonicalized) forms of the data values, and is thus independent of the storage 
medium and format of the data. 

CLI Usage
---------

This package includes a CLI entrypoint that reads supported input files and emits JSON aligned with `doc/unf.schema.json`.

Build:

```bash
mvn -q -DskipUT=true package
```

Run from terminal (quick start):

```bash
cd ~/git-hub/UNF-dataverse
mvn -q -DskipUT=true package
java -cp target/unf-<version>-SNAPSHOT.jar org.dataverse.unf.UnfCli --help
java -cp target/unf-<version>-SNAPSHOT.jar org.dataverse.unf.UnfCli --input <path>
```

CLI Entry Point:

```bash
java -cp target/unf-6.0.2-SNAPSHOT.jar org.dataverse.unf.UnfCli --input <path> [options]
```

Options:

- `--input <path>`: required. File or directory to process.
- `--output <path>`: optional. Writes JSON report to file. If omitted, prints to stdout.
- `--type <name>`: optional for non-CSV/TSV files. Default is `string`.
- `--datetime-format <fmt>`: required when `datetime` type is used.
- `--delimiter <char>`: optional delimiter override for tabular files.
- `--has-header <true|false>`: optional for tabular files. Default is `true`.
- `--column-types <list>`: optional comma-separated type list for CSV/TSV columns.
- `--help`: prints usage.

Supported Types:

- `string`
- `double`
- `float`
- `short`
- `byte`
- `long`
- `int`
- `boolean`
- `bitstring`
- `datetime`

Note: In the generated JSON output, all numeric types (double, float, short, byte, long, int) are reported as `"numeric"` since they are treated identically in UNF calculations (internally converted to doubles).

Examples:

Single-column text file (one value per line):

```bash
java -cp target/unf-6.0.2-SNAPSHOT.jar org.dataverse.unf.UnfCli \
  --input /path/to/values.txt \
  --type string
```

Date/time text file:

```bash
java -cp target/unf-6.0.2-SNAPSHOT.jar org.dataverse.unf.UnfCli \
  --input /path/to/timestamps.txt \
  --type datetime \
  --datetime-format "yyyy-MM-dd'T'HH:mm:ss"
```

CSV with inferred column types:

```bash
java -cp target/unf-6.0.2-SNAPSHOT.jar org.dataverse.unf.UnfCli \
  --input /path/to/data.csv
```

CSV with explicit per-column types:

```bash
java -cp target/unf-6.0.2-SNAPSHOT.jar org.dataverse.unf.UnfCli \
  --input /path/to/data.csv \
  --column-types int,int,string,double
```

TSV without header row:

```bash
java -cp target/unf-6.0.2-SNAPSHOT.jar org.dataverse.unf.UnfCli \
  --input /path/to/data.tsv \
  --has-header false
```

Force custom delimiter:

```bash
java -cp target/unf-6.0.2-SNAPSHOT.jar org.dataverse.unf.UnfCli \
  --input /path/to/data.csv \
  --delimiter ';'
```

Write output JSON to a file:

```bash
java -cp target/unf-6.0.2-SNAPSHOT.jar org.dataverse.unf.UnfCli \
  --input /path/to/data.csv \
  --output /path/to/report.unf.json
```

Dataset-level report from directory (regular files only, sorted by filename):

```bash
java -cp target/unf-6.0.2-SNAPSHOT.jar org.dataverse.unf.UnfCli \
  --input /path/to/dataset-dir
```

Operational Notes:

- For CSV/TSV, if `--column-types` is omitted, type inference is strict: every value in a column must parse for that type.
- Empty cells are not auto-converted to typed missing values during CSV/TSV parsing. Columns with blanks may infer as `string`.
- If a numeric column contains blanks and you force a numeric type with `--column-types`, parsing will fail.
- `--column-types` must include exactly one type per column.
- CSV/TSV parsing is delimiter-split based and does not implement quoted-field CSV escaping semantics.

```bash
java -cp target/unf-6.0.2-SNAPSHOT.jar org.dataverse.unf.UnfCli --help
```

Programmatic Usage
------------------

The UNF reporting logic can be used as a library by other Java applications.

```java
import org.dataverse.unf.UnfCli;
import java.nio.file.Path;

// 1. Configure options programmatically
UnfCli.CliOptions options = new UnfCli.CliOptions()
    .withInput("data.csv")
    .withType("string");

// (Optional) Configure advanced settings
options.hasHeader = true;
options.columnTypes = "int,string,double";

// 2. Generate the JSON report directly
String json = UnfCli.generateReport(Path.of("data.csv"), options);

// 3. (Optional) Use the built-in pretty printer
String prettyJson = UnfCli.prettyJson(json);
System.out.println(prettyJson);
```

License
-------

See [LICENSE](LICENSE) 
