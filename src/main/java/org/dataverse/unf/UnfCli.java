// Copyright 2026 Dataverse Core Team <support@dataverse.org>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// SPDX-License-Identifier: Apache-2.0

package org.dataverse.unf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * CLI entrypoint for producing UNF reports aligned to doc/unf.schema.json.
 */
public final class UnfCli {

    private static final String SOFTWARE_NAME = "dataverse-unf-java";
    private static final String SOFTWARE_VERSION = "6.0.2-SNAPSHOT";

    private UnfCli() {
    }

    public static void main(String[] args) {
        try {
            CliOptions options = CliOptions.parse(args);
            if (options.help) {
                System.out.println(usage());
                return;
            }
            String json = prettyJson(generateReport(Path.of(options.input), options));
            if (options.output == null) {
                System.out.println(json);
            } else {
                Files.writeString(Path.of(options.output), json + System.lineSeparator(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            System.err.println(usage());
            System.exit(1);
        }
    }

    public static String generateReport(Path inputPath, CliOptions options)
            throws IOException, UnfException {
        if (!Files.exists(inputPath)) {
            throw new IllegalArgumentException("Input path does not exist: " + inputPath);
        }

        String metadataJson = metadataJson(options);
        String resultJson;

        if (Files.isDirectory(inputPath)) {
            List<FileResult> entries = new ArrayList<>();
            List<Path> files = Files.list(inputPath)
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .toList();

            if (files.isEmpty()) {
                throw new IllegalArgumentException("Input directory has no regular files: " + inputPath);
            }

            List<String> fileUnfs = new ArrayList<>();
            for (Path file : files) {
                FileResult entry = computeFileResult(file, options);
                entries.add(entry);
                fileUnfs.add(entry.unf);
            }
            String datasetUnf = UNFUtil.calculateUNF(fileUnfs.toArray(new String[0]));
            resultJson = datasetResultJson(inputPath.getFileName().toString(), datasetUnf, entries);
        } else {
            FileResult fileResult = computeFileResult(inputPath, options);
            resultJson = fileResult.toJson();
        }

        return "{"
                + "\"unf_version\":\"6\","
                + "\"metadata\":" + metadataJson + ","
                + "\"result\":" + resultJson
                + "}";
    }

    private static FileResult computeFileResult(Path filePath, CliOptions options)
            throws IOException, UnfException {
        String name = filePath.getFileName().toString();
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".csv") || lower.endsWith(".tsv")) {
            char delimiter = options.delimiter != null
                    ? options.delimiter.charAt(0)
                    : (lower.endsWith(".tsv") ? '\t' : ',');
            return computeTabularFileResult(filePath, delimiter, options);
        }
        return computeLineFileResult(filePath, options);
    }

    private static FileResult computeLineFileResult(Path filePath, CliOptions options)
            throws IOException, UnfException {
        List<String> values = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Input file has no rows: " + filePath);
        }

        ColumnType type = ColumnType.parse(options.type);
        String columnName = stripFileExtension(filePath.getFileName().toString());
        String columnUnf = calculateColumnUnf(values, type, options.datetimeFormat);

        ColumnResult column = new ColumnResult(columnName, type.schemaType(), columnUnf);
        return new FileResult(filePath.getFileName().toString(), columnUnf, List.of(column));
    }

    private static FileResult computeTabularFileResult(Path filePath, char delimiter, CliOptions options)
            throws IOException, UnfException {
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Input file has no rows: " + filePath);
        }

        List<String[]> rows = new ArrayList<>();
        for (String line : lines) {
            rows.add(splitRow(line, delimiter));
        }

        int startRow = 0;
        String[] headers;
        if (options.hasHeader) {
            headers = rows.get(0);
            startRow = 1;
        } else {
            headers = null;
        }

        if (rows.size() <= startRow) {
            throw new IllegalArgumentException("Input file has no data rows: " + filePath);
        }

        int colCount = rows.get(startRow).length;
        List<List<String>> columns = new ArrayList<>();
        for (int i = 0; i < colCount; i++) {
            columns.add(new ArrayList<>());
        }

        for (int r = startRow; r < rows.size(); r++) {
            String[] row = rows.get(r);
            if (row.length != colCount) {
                throw new IllegalArgumentException("Inconsistent column count at row " + (r + 1)
                        + " in file " + filePath);
            }
            for (int c = 0; c < colCount; c++) {
                columns.get(c).add(row[c]);
            }
        }

        ColumnType[] types = resolveColumnTypes(columns, options.columnTypes);
        List<ColumnResult> results = new ArrayList<>();
        List<String> columnUnfs = new ArrayList<>();

        for (int i = 0; i < colCount; i++) {
            String colName = headers != null && i < headers.length && !headers[i].isEmpty()
                    ? headers[i]
                    : "column_" + (i + 1);
            String colUnf = calculateColumnUnf(columns.get(i), types[i], options.datetimeFormat);
            results.add(new ColumnResult(colName, types[i].schemaType(), colUnf));
            columnUnfs.add(colUnf);
        }

        String fileUnf = UNFUtil.calculateUNF(columnUnfs.toArray(new String[0]));
        return new FileResult(filePath.getFileName().toString(), fileUnf, results);
    }

    private static ColumnType[] resolveColumnTypes(List<List<String>> columns, String columnTypesArg) {
        int count = columns.size();
        ColumnType[] resolved = new ColumnType[count];

        if (columnTypesArg != null && !columnTypesArg.isBlank()) {
            String[] raw = columnTypesArg.split(",");
            if (raw.length != count) {
                throw new IllegalArgumentException("--column-types count (" + raw.length
                        + ") does not match actual column count (" + count + ").");
            }
            for (int i = 0; i < count; i++) {
                resolved[i] = ColumnType.parse(raw[i].trim());
            }
            return resolved;
        }

        for (int i = 0; i < count; i++) {
            resolved[i] = ColumnType.infer(columns.get(i));
        }
        return resolved;
    }

    private static String calculateColumnUnf(List<String> values, ColumnType type, String datetimeFormat)
            throws UnfException, IOException {
        switch (type) {
            case STRING:
                return UNFUtil.calculateUNF(values.toArray(new String[0]));
            case DOUBLE:
                return UNFUtil.calculateUNF((Number[]) toDoubleArray(values));
            case FLOAT:
                return UNFUtil.calculateUNF((Number[]) toFloatArray(values));
            case SHORT:
                return UNFUtil.calculateUNF((Number[]) toShortArray(values));
            case BYTE:
                return UNFUtil.calculateUNF((Number[]) toByteArray(values));
            case LONG:
                return UNFUtil.calculateUNF((Number[]) toLongArray(values));
            case INT:
                return UNFUtil.calculateUNF((Number[]) toIntArray(values));
            case BOOLEAN:
                return UnfDigest.unf(toBooleanArray(values))[0];
            case BITSTRING:
                return UNFUtil.calculateUNF(toBitStringArray(values));
            case DATETIME:
                if (datetimeFormat == null || datetimeFormat.isBlank()) {
                    throw new IllegalArgumentException("--datetime-format is required for type datetime.");
                }
                String[] rows = values.toArray(new String[0]);
                String[] patterns = new String[rows.length];
                Arrays.fill(patterns, datetimeFormat);
                return UNFUtil.calculateUNF(rows, patterns);
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    private static String datasetResultJson(String label, String datasetUnf, List<FileResult> entries) {
        StringBuilder out = new StringBuilder();
        out.append("{");
        out.append("\"type\":\"dataset\",");
        out.append("\"label\":\"").append(escapeJson(label)).append("\",");
        out.append("\"unf\":\"").append(escapeJson(datasetUnf)).append("\",");
        out.append("\"entries\":[");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                out.append(',');
            }
            out.append(entries.get(i).toJson());
        }
        out.append("]}");
        return out.toString();
    }

    private static String metadataJson(CliOptions options) {
        StringBuilder optionsJson = new StringBuilder();
        optionsJson.append("{");
        if (options.output != null) {
            optionsJson.append("\"output\":\"").append(escapeJson(options.output)).append("\",");
        }
        optionsJson.append("\"type\":\"").append(escapeJson(options.type)).append("\",");
        if (options.datetimeFormat != null) {
            optionsJson.append("\"datetime_format\":\"").append(escapeJson(options.datetimeFormat)).append("\",");
        }
        if (options.delimiter != null) {
            optionsJson.append("\"delimiter\":\"").append(escapeJson(options.delimiter)).append("\",");
        }
        optionsJson.append("\"has_header\":").append(options.hasHeader).append(",");
        if (options.columnTypes != null) {
            optionsJson.append("\"column_types\":\"").append(escapeJson(options.columnTypes)).append("\"");
        } else {
            optionsJson.append("\"column_types\":null");
        }
        optionsJson.append("}");

        return "{"
                + "\"timestamp\":\"" + Instant.now() + "\","
                + "\"parameters\":{"
                + "\"N\":7,"
                + "\"X\":128,"
                + "\"H\":128,"
                + "\"rounding_mode\":\"IEEE_754_nearest_even\""
                + "},"
                + "\"software\":{"
                + "\"name\":\"" + SOFTWARE_NAME + "\","
                + "\"version\":\"" + SOFTWARE_VERSION + "\""
                + "},"
                + "\"options\":" + optionsJson.toString()
                + "}";
    }

    private static String[] splitRow(String line, char delimiter) {
        return line.split(Pattern.quote(String.valueOf(delimiter)), -1);
    }

    private static String stripFileExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx > 0 ? name.substring(0, idx) : name;
    }

    private static Double[] toDoubleArray(List<String> values) {
        Double[] out = new Double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i);
            if (v != null && !v.isBlank()) {
                out[i] = Double.parseDouble(v.trim());
            }
        }
        return out;
    }

    private static Float[] toFloatArray(List<String> values) {
        Float[] out = new Float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i);
            if (v != null && !v.isBlank()) {
                out[i] = Float.parseFloat(v.trim());
            }
        }
        return out;
    }

    private static Short[] toShortArray(List<String> values) {
        Short[] out = new Short[values.size()];
        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i);
            if (v != null && !v.isBlank()) {
                out[i] = Short.parseShort(v.trim());
            }
        }
        return out;
    }

    private static Byte[] toByteArray(List<String> values) {
        Byte[] out = new Byte[values.size()];
        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i);
            if (v != null && !v.isBlank()) {
                out[i] = Byte.parseByte(v.trim());
            }
        }
        return out;
    }

    private static Long[] toLongArray(List<String> values) {
        Long[] out = new Long[values.size()];
        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i);
            if (v != null && !v.isBlank()) {
                out[i] = Long.parseLong(v.trim());
            }
        }
        return out;
    }

    private static Integer[] toIntArray(List<String> values) {
        Integer[] out = new Integer[values.size()];
        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i);
            if (v != null && !v.isBlank()) {
                out[i] = Integer.parseInt(v.trim());
            }
        }
        return out;
    }

    private static Boolean[] toBooleanArray(List<String> values) {
        Boolean[] out = new Boolean[values.size()];
        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i);
            if (v != null && !v.isBlank()) {
                String value = v.trim().toLowerCase(Locale.ROOT);
                if (!"true".equals(value) && !"false".equals(value)) {
                    throw new IllegalArgumentException("Invalid boolean value: " + v);
                }
                out[i] = Boolean.parseBoolean(value);
            }
        }
        return out;
    }

    private static BitString[] toBitStringArray(List<String> values) {
        BitString[] out = new BitString[values.size()];
        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i);
            if (v != null && !v.isBlank()) {
                String trimmed = v.trim();
                if (!trimmed.matches("[01]+")) {
                    throw new IllegalArgumentException("Invalid bitstring value: " + v);
                }
                out[i] = new BitString(trimmed);
            }
        }
        return out;
    }

    private static String escapeJson(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\b':
                    out.append("\\b");
                    break;
                case '\f':
                    out.append("\\f");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }

    private static String usage() {
        return "Usage: java org.dataverse.unf.UnfCli --input <path> [options]\n"
                + "\n"
                + "Options:\n"
                + "  --input <path>            Required. File or directory input path.\n"
                + "  --output <path>           Optional. Write JSON report to file (stdout otherwise).\n"
                + "  --type <name>             Single-column file type: string|double|float|short|byte|long|int|boolean|bitstring|datetime\n"
                + "                           Default: string\n"
                + "  --datetime-format <fmt>   Required when --type datetime or datetime column types are used.\n"
                + "  --delimiter <char>        Delimiter for CSV/TSV parsing. Defaults: ',' for .csv and '\\t' for .tsv.\n"
                + "  --has-header <true|false> Whether first row in CSV/TSV is header. Default: true.\n"
                + "  --column-types <list>     Comma-separated types for CSV/TSV columns.\n"
                + "                           If omitted, types are inferred per column.\n"
                + "  --help                    Show this help text.\n";
    }

    public static String prettyJson(String minified) {
        StringBuilder out = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        boolean escaping = false;

        for (int i = 0; i < minified.length(); i++) {
            char c = minified.charAt(i);

            if (inString) {
                out.append(c);
                if (escaping) {
                    escaping = false;
                } else if (c == '\\') {
                    escaping = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            switch (c) {
                case '"':
                    inString = true;
                    out.append(c);
                    break;
                case '{':
                case '[':
                    out.append(c).append('\n');
                    indent++;
                    appendIndent(out, indent);
                    break;
                case '}':
                case ']':
                    out.append('\n');
                    indent--;
                    appendIndent(out, indent);
                    out.append(c);
                    break;
                case ',':
                    out.append(c).append('\n');
                    appendIndent(out, indent);
                    break;
                case ':':
                    out.append(": ");
                    break;
                default:
                    if (!Character.isWhitespace(c)) {
                        out.append(c);
                    }
            }
        }

        return out.toString();
    }

    private static void appendIndent(StringBuilder out, int indent) {
        for (int i = 0; i < indent; i++) {
            out.append("  ");
        }
    }

    public static final class CliOptions {
        public String input;
        public String output;
        public String type = "string";
        public String datetimeFormat;
        public String delimiter;
        public boolean hasHeader = true;
        public String columnTypes;
        public boolean help;

        public static CliOptions parse(String[] args) {
            CliOptions options = new CliOptions();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                switch (arg) {
                    case "--help":
                        options.help = true;
                        return options;
                    case "--input":
                        options.input = requireValue(args, ++i, arg);
                        break;
                    case "--output":
                        options.output = requireValue(args, ++i, arg);
                        break;
                    case "--type":
                        options.type = requireValue(args, ++i, arg);
                        break;
                    case "--datetime-format":
                        options.datetimeFormat = requireValue(args, ++i, arg);
                        break;
                    case "--delimiter":
                        options.delimiter = requireValue(args, ++i, arg);
                        if (options.delimiter.length() != 1) {
                            throw new IllegalArgumentException("--delimiter expects a single character.");
                        }
                        break;
                    case "--has-header":
                        options.hasHeader = Boolean.parseBoolean(requireValue(args, ++i, arg));
                        break;
                    case "--column-types":
                        options.columnTypes = requireValue(args, ++i, arg);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }
            if (options.input == null || options.input.isBlank()) {
                throw new IllegalArgumentException("--input is required.");
            }
            return options;
        }

        private static String requireValue(String[] args, int index, String flag) {
            if (index >= args.length) {
                throw new IllegalArgumentException("Missing value for " + flag);
            }
            return args[index];
        }

        public CliOptions withInput(String value) {
            this.input = value;
            return this;
        }

        public CliOptions withType(String value) {
            this.type = value;
            return this;
        }

        public CliOptions withColumnTypes(String value) {
            this.columnTypes = value;
            return this;
        }

        public CliOptions withDatetimeFormat(String value) {
            this.datetimeFormat = value;
            return this;
        }
    }

    private enum ColumnType {
        STRING,
        DOUBLE,
        FLOAT,
        SHORT,
        BYTE,
        LONG,
        INT,
        BOOLEAN,
        BITSTRING,
        DATETIME;

        static ColumnType parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return STRING;
            }
            return ColumnType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        }

        String schemaType() {
            switch (this) {
                case DOUBLE:
                case FLOAT:
                case SHORT:
                case BYTE:
                case LONG:
                case INT:
                    return "numeric";
                default:
                    return name().toLowerCase(Locale.ROOT);
            }
        }

        static ColumnType infer(List<String> values) {
            if (all(values, ColumnType::isBoolean)) {
                return BOOLEAN;
            }
            if (all(values, ColumnType::isBitString)) {
                return BITSTRING;
            }
            if (all(values, ColumnType::isInt)) {
                return INT;
            }
            if (all(values, ColumnType::isLong)) {
                return LONG;
            }
            if (all(values, ColumnType::isDouble)) {
                return DOUBLE;
            }
            return STRING;
        }

        private interface Checker {
            boolean test(String value);
        }

        private static boolean all(List<String> values, Checker checker) {
            boolean hasNonEmpty = false;
            for (String value : values) {
                if (value == null || value.isBlank()) {
                    continue;
                }
                hasNonEmpty = true;
                if (!checker.test(value.trim())) {
                    return false;
                }
            }
            return hasNonEmpty;
        }

        private static boolean isBoolean(String value) {
            String v = value.trim().toLowerCase(Locale.ROOT);
            return "true".equals(v) || "false".equals(v);
        }

        private static boolean isBitString(String value) {
            return value.matches("[01]+") && !value.isEmpty();
        }

        private static boolean isInt(String value) {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        private static boolean isLong(String value) {
            try {
                Long.parseLong(value);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        private static boolean isDouble(String value) {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
    }

    private static final class ColumnResult {
        private final String name;
        private final String type;
        private final String unf;

        private ColumnResult(String name, String type, String unf) {
            this.name = name;
            this.type = type;
            this.unf = unf;
        }

        private String toJson() {
            return "{"
                    + "\"name\":\"" + escapeJson(name) + "\","
                    + "\"type\":\"" + escapeJson(type) + "\","
                    + "\"unf\":\"" + escapeJson(unf) + "\""
                    + "}";
        }
    }

    private static final class FileResult {
        private final String label;
        private final String unf;
        private final List<ColumnResult> columns;

        private FileResult(String label, String unf, List<ColumnResult> columns) {
            this.label = label;
            this.unf = unf;
            this.columns = columns;
        }

        private String toJson() {
            StringBuilder out = new StringBuilder();
            out.append('{');
            out.append("\"type\":\"file\",");
            out.append("\"label\":\"").append(escapeJson(label)).append("\",");
            out.append("\"unf\":\"").append(escapeJson(unf)).append("\",");
            out.append("\"columns\":[");
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    out.append(',');
                }
                out.append(columns.get(i).toJson());
            }
            out.append("]}");
            return out.toString();
        }
    }
}
