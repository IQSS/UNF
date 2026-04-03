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

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UnfDartfxTest {

    @Test
    void generateReport_dartfx101A_matchesExpectedFileUNF() throws Exception {
        Path csvFile = Path.of("src/test/resources/test/dartfx/101A.csv");
        UnfCli.CliOptions options = new UnfCli.CliOptions().withInput(csvFile.toString());
        String json = UnfCli.generateReport(csvFile, options);

        assertTrue(json.contains("\"type\":\"file\""));
        assertTrue(json.contains("\"label\":\"101A.csv\""));
        assertTrue(json.contains("\"unf\":\"UNF:6:kBkE4q7GbowX/3tKvDSEWg==\""));
    }

    @Test
    void generateReport_dartfx101B_matchesExpectedFileUNF() throws Exception {
        Path csvFile = Path.of("src/test/resources/test/dartfx/101B.csv");
        UnfCli.CliOptions options = new UnfCli.CliOptions().withInput(csvFile.toString());
        String json = UnfCli.generateReport(csvFile, options);

        assertTrue(json.contains("\"type\":\"file\""));
        assertTrue(json.contains("\"label\":\"101B.csv\""));
        assertTrue(json.contains("\"unf\":\"UNF:6:kBkE4q7GbowX/3tKvDSEWg==\""));
    }

    @Test
    void generateReport_dartfx101C_matchesExpectedFileUNF() throws Exception {
        Path csvFile = Path.of("src/test/resources/test/dartfx/101C.csv");
        UnfCli.CliOptions options = new UnfCli.CliOptions().withInput(csvFile.toString());
        String json = UnfCli.generateReport(csvFile, options);

        assertTrue(json.contains("\"type\":\"file\""));
        assertTrue(json.contains("\"label\":\"101C.csv\""));
        assertTrue(json.contains("\"unf\":\"UNF:6:grJwrCwbQzxeZQ5fNkeApw==\""));
    }

    @Test
    void generateReport_dartfx101D_matchesExpectedFileUNF() throws Exception {
        Path csvFile = Path.of("src/test/resources/test/dartfx/101D.csv");
        UnfCli.CliOptions options = new UnfCli.CliOptions().withInput(csvFile.toString());
        String json = UnfCli.generateReport(csvFile, options);

        assertTrue(json.contains("\"type\":\"file\""));
        assertTrue(json.contains("\"label\":\"101D.csv\""));
        assertTrue(json.contains("\"unf\":\"UNF:6:grJwrCwbQzxeZQ5fNkeApw==\""));
    }
}
