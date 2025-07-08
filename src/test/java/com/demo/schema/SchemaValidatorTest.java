package com.demo.schema;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;

/**
 * Unit tests for SchemaValidator
 */
public class SchemaValidatorTest {
    
    private SchemaValidator validator;
    private static final String VALID_SCHEMA_PATH = "src/main/resources/schema/feeds_schema.json";
    private static final String FILE_WATCHER_YAML_PATH = "src/main/resources/schema/feed_file_1.yaml";
    private static final String KAFKA_YAML_PATH = "src/main/resources/schema/feed_kafka_1.yaml";
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Before
    public void setUp() {
        validator = new SchemaValidator();
    }
    
    @Test
    public void testLoadSchema_ValidSchema() throws IOException, ProcessingException {
        // When: Loading a valid schema
        JsonSchema schema = validator.loadSchema(VALID_SCHEMA_PATH);
        
        // Then: Schema should be loaded successfully
        assertNotNull("Schema should not be null", schema);
    }
    
    @Test(expected = IOException.class)
    public void testLoadSchema_NonExistentFile() throws IOException, ProcessingException {
        // When: Loading a non-existent schema file
        validator.loadSchema("non-existent-schema.json");
        // Then: Should throw IOException
    }
    
    @Test(expected = IOException.class)
    public void testLoadSchema_InvalidJson() throws IOException, ProcessingException {
        // Given: An invalid JSON file
        File tempFile = this.tempFolder.newFile("invalid.json");
        Files.write(tempFile.toPath(), "{ invalid json }".getBytes());
        
        // When: Loading invalid JSON as schema
        validator.loadSchema(tempFile.getAbsolutePath());
        // Then: Should throw IOException due to JSON parsing error
    }
    
    @Test
    public void testLoadYaml_FileWatcherYaml() throws IOException {
        // When: Loading the file watcher YAML file
        JsonNode data = validator.loadYaml(FILE_WATCHER_YAML_PATH);
        
        // Then: YAML should be loaded successfully
        assertNotNull("File watcher YAML data should not be null", data);
        assertTrue("File watcher YAML data should be an array", data.isArray());
        assertTrue("File watcher YAML data should have at least one element", data.size() > 0);
    }
    
    @Test
    public void testLoadYaml_KafkaYaml() throws IOException {
        // When: Loading the Kafka YAML file
        JsonNode data = validator.loadYaml(KAFKA_YAML_PATH);
        
        // Then: YAML should be loaded successfully
        assertNotNull("Kafka YAML data should not be null", data);
        assertTrue("Kafka YAML data should be an array", data.isArray());
        assertTrue("Kafka YAML data should have at least one element", data.size() > 0);
    }
    
    @Test(expected = IOException.class)
    public void testLoadYaml_NonExistentFile() throws IOException {
        // When: Loading a non-existent YAML file
        validator.loadYaml("non-existent.yaml");
        // Then: Should throw IOException
    }
    
    @Test(expected = IOException.class)
    public void testLoadYaml_InvalidYaml() throws IOException {
        // Given: An invalid YAML file
        File tempFile = this.tempFolder.newFile("invalid.yaml");
        Files.write(tempFile.toPath(), "invalid: yaml: content: [".getBytes());
        
        // When: Loading invalid YAML
        validator.loadYaml(tempFile.getAbsolutePath());
        // Then: Should throw IOException
    }
    
    @Test
    public void testValidate_FileWatcherData() throws IOException, ProcessingException {
        // Given: Valid schema and file watcher data
        JsonSchema schema = validator.loadSchema(VALID_SCHEMA_PATH);
        JsonNode data = validator.loadYaml(FILE_WATCHER_YAML_PATH);
        
        // When: Validating file watcher data
        ProcessingReport report = validator.validate(schema, data);
        
        // Then: Validation should be successful
        assertNotNull("Report should not be null", report);
        assertTrue("File watcher validation should be successful", report.isSuccess());
    }
    
    @Test
    public void testValidate_KafkaData() throws IOException, ProcessingException {
        // Given: Valid schema and Kafka data
        JsonSchema schema = validator.loadSchema(VALID_SCHEMA_PATH);
        JsonNode data = validator.loadYaml(KAFKA_YAML_PATH);
        
        // When: Validating Kafka data
        ProcessingReport report = validator.validate(schema, data);
        
        // Then: Validation should be successful
        assertNotNull("Report should not be null", report);
        assertTrue("Kafka validation should be successful", report.isSuccess());
    }
    
    @Test
    public void testValidate_InvalidData() throws IOException, ProcessingException {
        // Given: Valid schema but invalid data
        JsonSchema schema = validator.loadSchema(VALID_SCHEMA_PATH);
        
        // Create invalid data (missing required fields)
        String invalidYaml = "- name: \"test\"\n" +
                           "  # Missing required file-watcher and inbound-datasets";
        File tempFile = this.tempFolder.newFile("invalid_data.yaml");
        Files.write(tempFile.toPath(), invalidYaml.getBytes());
        JsonNode invalidData = validator.loadYaml(tempFile.getAbsolutePath());
        
        // When: Validating invalid data
        ProcessingReport report = validator.validate(schema, invalidData);
        
        // Then: Validation should fail
        assertNotNull("Report should not be null", report);
        assertFalse("Validation should fail", report.isSuccess());
    }
    
    @Test
    public void testValidateFile_FileWatcherFiles() throws IOException, ProcessingException {
        // When: Validating file watcher files
        ProcessingReport report = validator.validateFile(VALID_SCHEMA_PATH, FILE_WATCHER_YAML_PATH);
        
        // Then: Validation should be successful
        assertNotNull("Report should not be null", report);
        assertTrue("File watcher validation should be successful", report.isSuccess());
    }
    
    @Test
    public void testValidateFile_KafkaFiles() throws IOException, ProcessingException {
        // When: Validating Kafka files
        ProcessingReport report = validator.validateFile(VALID_SCHEMA_PATH, KAFKA_YAML_PATH);
        
        // Then: Validation should be successful
        assertNotNull("Report should not be null", report);
        assertTrue("Kafka validation should be successful", report.isSuccess());
    }
    
    @Test(expected = IOException.class)
    public void testValidateFile_NonExistentSchema() throws IOException, ProcessingException {
        // When: Validating with non-existent schema
        validator.validateFile("non-existent-schema.json", FILE_WATCHER_YAML_PATH);
        // Then: Should throw IOException
    }
    
    @Test(expected = IOException.class)
    public void testValidateFile_NonExistentYaml() throws IOException, ProcessingException {
        // When: Validating with non-existent YAML
        validator.validateFile(VALID_SCHEMA_PATH, "non-existent.yaml");
        // Then: Should throw IOException
    }
    
    @Test
    public void testIsValid_ValidFileWatcherReport() throws IOException, ProcessingException {
        // Given: A valid validation report for file watcher
        ProcessingReport report = validator.validateFile(VALID_SCHEMA_PATH, FILE_WATCHER_YAML_PATH);
        
        // When: Checking if valid
        boolean isValid = validator.isValid(report);
        
        // Then: Should return true
        assertTrue("File watcher should be valid", isValid);
    }
    
    @Test
    public void testIsValid_ValidKafkaReport() throws IOException, ProcessingException {
        // Given: A valid validation report for Kafka
        ProcessingReport report = validator.validateFile(VALID_SCHEMA_PATH, KAFKA_YAML_PATH);
        
        // When: Checking if valid
        boolean isValid = validator.isValid(report);
        
        // Then: Should return true
        assertTrue("Kafka should be valid", isValid);
    }
    
    @Test
    public void testIsValid_InvalidReport() throws IOException, ProcessingException {
        // Given: An invalid validation report
        JsonSchema schema = validator.loadSchema(VALID_SCHEMA_PATH);
        
        // Create invalid data
        String invalidYaml = "- name: \"test\"";
        File tempFile = this.tempFolder.newFile("invalid_test.yaml");
        Files.write(tempFile.toPath(), invalidYaml.getBytes());
        JsonNode invalidData = validator.loadYaml(tempFile.getAbsolutePath());
        ProcessingReport report = validator.validate(schema, invalidData);
        
        // When: Checking if valid
        boolean isValid = validator.isValid(report);
        
        // Then: Should return false
        assertFalse("Should be invalid", isValid);
    }
    
    @Test
    public void testPrintReport_ValidFileWatcherReport() throws IOException, ProcessingException {
        // Given: A valid validation report for file watcher
        ProcessingReport report = validator.validateFile(VALID_SCHEMA_PATH, FILE_WATCHER_YAML_PATH);
        
        // When: Printing report
        // Then: Should not throw any exception
        validator.printReport(report);
    }
    
    @Test
    public void testPrintReport_ValidKafkaReport() throws IOException, ProcessingException {
        // Given: A valid validation report for Kafka
        ProcessingReport report = validator.validateFile(VALID_SCHEMA_PATH, KAFKA_YAML_PATH);
        
        // When: Printing report
        // Then: Should not throw any exception
        validator.printReport(report);
    }
    
    @Test
    public void testPrintReport_InvalidReport() throws IOException, ProcessingException {
        // Given: An invalid validation report
        JsonSchema schema = validator.loadSchema(VALID_SCHEMA_PATH);
        
        // Create invalid data
        String invalidYaml = "- name: \"test\"";
        File tempFile = this.tempFolder.newFile("invalid_print.yaml");
        Files.write(tempFile.toPath(), invalidYaml.getBytes());
        JsonNode invalidData = validator.loadYaml(tempFile.getAbsolutePath());
        ProcessingReport report = validator.validate(schema, invalidData);
        
        // When: Printing report
        // Then: Should not throw any exception
        validator.printReport(report);
    }
    
    @Test
    public void testValidateFile_ComplexSchema() throws IOException, ProcessingException {
        // Given: A complex schema with nested objects and arrays
        String complexSchema = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"name\": {\"type\": \"string\"},\n" +
            "    \"config\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"properties\": {\n" +
            "        \"enabled\": {\"type\": \"boolean\"},\n" +
            "        \"items\": {\n" +
            "          \"type\": \"array\",\n" +
            "          \"items\": {\"type\": \"string\"}\n" +
            "        }\n" +
            "      },\n" +
            "      \"required\": [\"enabled\"]\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\"name\", \"config\"]\n" +
            "}";
        
        String validData = "name: \"test\"\n" +
            "config:\n" +
            "  enabled: true\n" +
            "  items:\n" +
            "    - \"item1\"\n" +
            "    - \"item2\"";
        
        File schemaFile = this.tempFolder.newFile("complex_schema.json");
        File dataFile = this.tempFolder.newFile("complex_data.yaml");
        Files.write(schemaFile.toPath(), complexSchema.getBytes());
        Files.write(dataFile.toPath(), validData.getBytes());
        
        // When: Validating complex schema
        ProcessingReport report = validator.validateFile(schemaFile.getAbsolutePath(), dataFile.getAbsolutePath());
        
        // Then: Validation should be successful
        assertTrue("Complex validation should be successful", report.isSuccess());
    }
    
    @Test
    public void testValidateFile_EnumValidation() throws IOException, ProcessingException {
        // Given: A schema with enum validation
        String enumSchema = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"status\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"enum\": [\"active\", \"inactive\", \"pending\"]\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\"status\"]\n" +
            "}";
        
        String validData = "status: \"active\"";
        
        String invalidData = "status: \"invalid_status\"";
        
        File schemaFile = this.tempFolder.newFile("enum_schema.json");
        File validFile = this.tempFolder.newFile("valid_enum.yaml");
        File invalidFile = this.tempFolder.newFile("invalid_enum.yaml");
        
        Files.write(schemaFile.toPath(), enumSchema.getBytes());
        Files.write(validFile.toPath(), validData.getBytes());
        Files.write(invalidFile.toPath(), invalidData.getBytes());
        
        // When: Validating with valid enum value
        ProcessingReport validReport = validator.validateFile(schemaFile.getAbsolutePath(), validFile.getAbsolutePath());
        
        // Then: Should be valid
        assertTrue("Valid enum should pass validation", validReport.isSuccess());
        
        // When: Validating with invalid enum value
        ProcessingReport invalidReport = validator.validateFile(schemaFile.getAbsolutePath(), invalidFile.getAbsolutePath());
        
        // Then: Should be invalid
        assertFalse("Invalid enum should fail validation", invalidReport.isSuccess());
    }
    
    @Test
    public void testValidateFile_CompressionEnum() throws IOException, ProcessingException {
        // Given: Valid schema and data with different compression types
        JsonSchema schema = validator.loadSchema(VALID_SCHEMA_PATH);
        
        // Test with gzip compression
        String gzipYaml = "- name: \"gzip_test\"\n" +
                         "  file-watcher:\n" +
                         "    name: \"test\"\n" +
                         "    active: true\n" +
                         "    interval: 20\n" +
                         "    inbound-uri: \"test\"\n" +
                         "    preprocess-uri: \"test\"\n" +
                         "    monitor-uri: \"test\"\n" +
                         "    logs-uri: \"test\"\n" +
                         "    transaction:\n" +
                         "      commit:\n" +
                         "        success-uri: \"test\"\n" +
                         "        done-file:\n" +
                         "          uri: \"test\"\n" +
                         "          type: \".done\"\n" +
                         "          template: \"test\"\n" +
                         "      rollback:\n" +
                         "        failure-uri: \"test\"\n" +
                         "  inbound-datasets:\n" +
                         "    - name: \"test\"\n" +
                         "      active: true\n" +
                         "      pattern: \"test\"\n" +
                         "      type: \"test\"\n" +
                         "      module: \"test\"\n" +
                         "      format: \"test\"\n" +
                         "      compression: \"gzip\"\n" +
                         "      output:\n" +
                         "        hive:\n" +
                         "          table-name: \"test\"\n" +
                         "        done-file-uri: \"test\"\n" +
                         "        done-file-type: \".done\"";
        
        File gzipFile = this.tempFolder.newFile("gzip_test.yaml");
        Files.write(gzipFile.toPath(), gzipYaml.getBytes());
        
        // When: Validating with gzip compression
        ProcessingReport gzipReport = validator.validate(schema, validator.loadYaml(gzipFile.getAbsolutePath()));
        
        // Then: Should be valid
        assertTrue("Gzip compression should be valid", gzipReport.isSuccess());
        
        // Test with zip compression
        String zipYaml = gzipYaml.replace("compression: \"gzip\"", "compression: \"zip\"");
        File zipFile = this.tempFolder.newFile("zip_test.yaml");
        Files.write(zipFile.toPath(), zipYaml.getBytes());
        
        ProcessingReport zipReport = validator.validate(schema, validator.loadYaml(zipFile.getAbsolutePath()));
        assertTrue("Zip compression should be valid", zipReport.isSuccess());
        
        // Test with none compression
        String noneYaml = gzipYaml.replace("compression: \"gzip\"", "compression: \"none\"");
        File noneFile = this.tempFolder.newFile("none_test.yaml");
        Files.write(noneFile.toPath(), noneYaml.getBytes());
        
        ProcessingReport noneReport = validator.validate(schema, validator.loadYaml(noneFile.getAbsolutePath()));
        assertTrue("None compression should be valid", noneReport.isSuccess());
        
        // Test with null compression (omit the field entirely)
        String nullYaml = gzipYaml.replace("      compression: \"gzip\"\n", "");
        File nullFile = this.tempFolder.newFile("null_test.yaml");
        Files.write(nullFile.toPath(), nullYaml.getBytes());
        
        ProcessingReport nullReport = validator.validate(schema, validator.loadYaml(nullFile.getAbsolutePath()));
        assertTrue("Missing compression should be valid", nullReport.isSuccess());
    }
    
    @Test
    public void testValidateFile_InvalidCompression() throws IOException, ProcessingException {
        // Given: Valid schema but invalid compression type
        JsonSchema schema = validator.loadSchema(VALID_SCHEMA_PATH);
        
        String invalidYaml = "- name: \"invalid_compression\"\n" +
                           "  file-watcher:\n" +
                           "    name: \"test\"\n" +
                           "    active: true\n" +
                           "    interval: 20\n" +
                           "    inbound-uri: \"test\"\n" +
                           "    preprocess-uri: \"test\"\n" +
                           "    monitor-uri: \"test\"\n" +
                           "    logs-uri: \"test\"\n" +
                           "    transaction:\n" +
                           "      commit:\n" +
                           "        success-uri: \"test\"\n" +
                           "        done-file:\n" +
                           "          uri: \"test\"\n" +
                           "          type: \".done\"\n" +
                           "          template: \"test\"\n" +
                           "      rollback:\n" +
                           "        failure-uri: \"test\"\n" +
                           "  inbound-datasets:\n" +
                           "    - name: \"test\"\n" +
                           "      active: true\n" +
                           "      pattern: \"test\"\n" +
                           "      type: \"test\"\n" +
                           "      module: \"test\"\n" +
                           "      format: \"test\"\n" +
                           "      compression: \"invalid_compression\"\n" +
                           "      output:\n" +
                           "        hive:\n" +
                           "          table-name: \"test\"\n" +
                           "        done-file-uri: \"test\"\n" +
                           "        done-file-type: \".done\"";
        
        File invalidFile = this.tempFolder.newFile("invalid_compression.yaml");
        Files.write(invalidFile.toPath(), invalidYaml.getBytes());
        
        // When: Validating with invalid compression
        ProcessingReport report = validator.validate(schema, validator.loadYaml(invalidFile.getAbsolutePath()));
        
        // Then: Should be invalid
        assertFalse("Invalid compression should fail validation", report.isSuccess());
    }
    
    @Test
    public void testValidateFile_MissingRequiredFields() throws IOException, ProcessingException {
        // Given: Valid schema but missing required fields
        JsonSchema schema = validator.loadSchema(VALID_SCHEMA_PATH);
        
        // Test missing file-watcher
        String missingFileWatcher = "- name: \"test\"\n" +
                                   "  inbound-datasets:\n" +
                                   "    - name: \"test\"\n" +
                                   "      active: true\n" +
                                   "      pattern: \"test\"\n" +
                                   "      type: \"test\"\n" +
                                   "      module: \"test\"\n" +
                                   "      format: \"test\"\n" +
                                   "      output:\n" +
                                   "        hive:\n" +
                                   "          table-name: \"test\"\n" +
                                   "        done-file-uri: \"test\"\n" +
                                   "        done-file-type: \".done\"";
        
        File missingFileWatcherFile = this.tempFolder.newFile("missing_file_watcher.yaml");
        Files.write(missingFileWatcherFile.toPath(), missingFileWatcher.getBytes());
        
        // When: Validating with missing file-watcher
        ProcessingReport report = validator.validate(schema, validator.loadYaml(missingFileWatcherFile.getAbsolutePath()));
        
        // Then: Should be invalid
        assertFalse("Missing file-watcher should fail validation", report.isSuccess());
    }
} 