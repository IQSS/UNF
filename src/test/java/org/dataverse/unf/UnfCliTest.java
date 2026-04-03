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

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UnfCliTest {

    @Test
    void generateReport_lineBasedStringFile_matchesSchemaShapeAndKnownUNF() throws Exception {
        Path tempFile = Files.createTempFile("unf-cli-string", ".txt");
        Files.writeString(tempFile, "Hello World\nTesting 123\n", StandardCharsets.UTF_8);

        UnfCli.CliOptions options = new UnfCli.CliOptions().withInput(tempFile.toString()).withType("string");
        String json = UnfCli.generateReport(tempFile, options);

        assertTrue(json.contains("\"unf_version\":\"6\""));
        assertTrue(json.contains("\"type\":\"file\""));
        assertTrue(json.contains("\"columns\""));
        assertTrue(json.contains("\"unf\":\"UNF:6:r+FDbVC6fKdUjRS6ZIzP4w==\""));
    }

    @Test
    void generateReport_csvFile_withTwoNumericColumns_returnsFileAndColumnUNFs() throws Exception {
        Path tempFile = Files.createTempFile("unf-cli-table", ".csv");
        Files.writeString(tempFile, "a,b\n6.6666666666666667,32\n75.216,2024\n", StandardCharsets.UTF_8);

        UnfCli.CliOptions options = new UnfCli.CliOptions()
                .withInput(tempFile.toString())
                .withColumnTypes("double,int");
        String json = UnfCli.generateReport(tempFile, options);

        assertTrue(json.contains("\"type\":\"file\""));
        assertTrue(json.contains("\"name\":\"a\""));
        assertTrue(json.contains("\"name\":\"b\""));
        assertTrue(json.contains("UNF:6:+kc3wyGwZ6otDkZwpvswDw=="));
        assertTrue(json.contains("UNF:6:R2Xa8BqKPRgj5EpnYEZQyw=="));
    }

    @Test
    void generateReport_withMissingValues_infersNumericAndHandlesBlanks() throws Exception {
        Path tempFile = Files.createTempFile("missing-numeric", ".csv");
        // var1 is all numeric, var2 has a blank, var3 has a blank
        Files.writeString(tempFile, "var1,var2,var3\n1,1,true\n2,,false\n3,3,\n", StandardCharsets.UTF_8);

        UnfCli.CliOptions options = new UnfCli.CliOptions().withInput(tempFile.toString());
        String json = UnfCli.generateReport(tempFile, options);

        // Inference checks
        assertTrue(json.contains("\"name\":\"var1\",\"type\":\"numeric\""));
        assertTrue(json.contains("\"name\":\"var2\",\"type\":\"numeric\""));
        assertTrue(json.contains("\"name\":\"var3\",\"type\":\"boolean\""));

        // No errors during UNF calculation
        assertTrue(json.contains("\"unf\":\"UNF:6:"));
    }

    @Test
    void generateReport_explicitNumericWithBlanks_worksWithoutError() throws Exception {
        Path tempFile = Files.createTempFile("explicit-blanks", ".csv");
        Files.writeString(tempFile, "val\n1\n\n3\n", StandardCharsets.UTF_8);

        UnfCli.CliOptions options = new UnfCli.CliOptions()
                .withInput(tempFile.toString())
                .withColumnTypes("int");
        String json = UnfCli.generateReport(tempFile, options);

        assertTrue(json.contains("\"name\":\"val\",\"type\":\"numeric\""));
        assertTrue(json.contains("\"unf\":\"UNF:6:"));
    }
}
