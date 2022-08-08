package com.ptsecurity.appsec.ai.ee.test.scan.result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.*;
import com.ptsecurity.appsec.ai.ee.scan.reports.Reports;
import com.ptsecurity.appsec.ai.ee.scan.result.ScanResult;
import com.ptsecurity.appsec.ai.ee.scan.result.issue.types.BaseIssue;
import com.ptsecurity.appsec.ai.ee.scan.result.issue.types.VulnerabilityIssue;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.test.BaseTest;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

@DisplayName("Read and parse data from PT AI version-independent scan results JSON resource file")
public class ScanResultTest extends BaseTest {
    @SneakyThrows
    @Test
    @DisplayName("Read and parse data from PT AI version-independent OWASP Bricks scan results JSON resource file")
    public void parseBricksScanResults() {
        ObjectMapper mapper = createFaultTolerantObjectMapper();
        for (Connection.Version version : Connection.Version.values()) {
            String json = extractSevenZippedSingleStringFromResource("json/scan/result/" + version.name().toLowerCase() + "/" + PHP_OWASP_BRICKS_PROJECT_NAME + ".json.7z");
            Assertions.assertFalse(StringUtils.isEmpty(json));
            ScanResult scanResult = mapper.readValue(json, ScanResult.class);
            Assertions.assertNotNull(scanResult.getStatistics());
            Assertions.assertNotEquals(0, scanResult.getStatistics().getScannedFileCount());
            long sqliCount = scanResult.getIssues().stream()
                    .filter(baseIssue -> baseIssue instanceof VulnerabilityIssue)
                    .filter(baseIssue -> BaseIssue.Level.HIGH == baseIssue.getLevel())
                    .filter(baseIssue -> "SQL Injection".equalsIgnoreCase(scanResult.getI18n().get(baseIssue.getTypeId()).get(Reports.Locale.EN).getTitle()))
                    .count();
            Assertions.assertNotEquals(0, sqliCount);
        }
    }

    @SneakyThrows
    @Test
    @DisplayName("Read and parse data from PT AI version-independent PHP Smoke scan results JSON resource file")
    public void parsePhpSmokeScanResults() {
        ObjectMapper mapper = createFaultTolerantObjectMapper();
        for (Connection.Version version : Connection.Version.values()) {
            String json = extractSevenZippedSingleStringFromResource("json/scan/result/" + version.name().toLowerCase() + "/" + PHP_SMOKE_MEDIUM_PROJECT_NAME + ".json.7z");
            Assertions.assertFalse(StringUtils.isEmpty(json));
            ScanResult scanResult = mapper.readValue(json, ScanResult.class);
            Assertions.assertNotNull(scanResult.getStatistics());
            Assertions.assertNotEquals(0, scanResult.getStatistics().getScannedFileCount());
            long xssCount = scanResult.getIssues().stream()
                    .filter(baseIssue -> baseIssue instanceof VulnerabilityIssue)
                    .filter(baseIssue -> BaseIssue.Level.MEDIUM == baseIssue.getLevel())
                    .filter(baseIssue -> "Cross-Site Scripting".equalsIgnoreCase(scanResult.getI18n().get(baseIssue.getTypeId()).get(Reports.Locale.EN).getTitle()))
                    .count();
            Assertions.assertNotEquals(0, xssCount);
        }
    }

    @SneakyThrows
    @Test
    @DisplayName("Check scan results JSON serialization")
    public void serializeScanResults() {
        ObjectMapper mapper = createFaultTolerantObjectMapper();
        for (Connection.Version version : Connection.Version.values()) {
            String json = extractSevenZippedSingleStringFromResource("json/scan/result/" + version.name().toLowerCase() + "/" + PHP_SMOKE_MEDIUM_PROJECT_NAME + ".json.7z");
            Assertions.assertFalse(StringUtils.isEmpty(json));
            ScanResult scanResult = mapper.readValue(json, ScanResult.class);
            String jsonOut = mapper.writeValueAsString(scanResult);
            Assertions.assertFalse(jsonOut.contains("\"clazz\":"));
        }
    }

    @SneakyThrows
    @Test
    @DisplayName("Generate ScanResult JSON schema")
    public void generateSchema() {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON);
        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(ScanResult.class);
        String schema = jsonSchema.toPrettyString();
    }
}
