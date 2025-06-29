package com.demo.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * Unit tests for SchemaValidator
 */
public class SchemaValidatorTest {
    
    private SchemaValidator validator;
    private static final String VALID_SCHEMA_PATH = "src/main/resources/schema/feeds_schema.json";
    private static final String VALID_YAML_PATH = "src/main/resources/schema/feed_1.yaml";
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Before
    public void setUp() {
        validator = new SchemaValidator();
    }
    
    @Test
    public void testLoadSchema_ValidSchema() throws IOException, ProcessingException {
        // When: Loading a valid schema
        var schema = validator.loadSchema(VALID_SCHEMA_PATH);
        
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
        File tempFile = tempFolder.newFile("invalid.json");
        Files.write(tempFile.toPath(), "{ invalid json }".getBytes());
        
        // When: Loading invalid JSON as schema
        validator.loadSchema(tempFile.getAbsolutePath());
        // Then: Should throw IOException due to JSON parsing error
    }
    
    @Test
    public void testLoadYaml_ValidYaml() throws IOException {
        // When: Loading a valid YAML file
        JsonNode data = validator.loadYaml(VALID_YAML_PATH);
        
        // Then: YAML should be loaded successfully
        assertNotNull("YAML data should not be null", data);
        assertTrue("YAML data should be an array", data.isArray());
        assertTrue("YAML data should have at least one element", data.size() > 0);
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
        File tempFile = tempFolder.newFile("invalid.yaml");
        Files.write(tempFile.toPath(), "invalid: yaml: content: [".getBytes());
        
        // When: Loading invalid YAML
        validator.loadYaml(tempFile.getAbsolutePath());
        // Then: Should throw IOException
    }
    
    @Test
    public void testValidate_ValidData() throws IOException, ProcessingException {
        // Given: Valid schema and data
        var schema = validator.loadSchema(VALID_SCHEMA_PATH);
        JsonNode data = validator.loadYaml(VALID_YAML_PATH);
        
        // When: Validating valid data
        ProcessingReport report = validator.validate(schema, data);
        
        // Then: Validation should be successful
        assertNotNull("Report should not be null", report);
        assertTrue("Validation should be successful", report.isSuccess());
    }
    
    @Test
    public void testValidate_InvalidData() throws IOException, ProcessingException {
        // Given: Valid schema but invalid data
        var schema = validator.loadSchema(VALID_SCHEMA_PATH);
        
        // Create invalid data (missing required fields)
        String invalidYaml = """
            - name: "test"
              # Missing required file-watcher and inbound-datasets
            """;
        File tempFile = tempFolder.newFile("invalid_data.yaml");
        Files.write(tempFile.toPath(), invalidYaml.getBytes());
        JsonNode invalidData = validator.loadYaml(tempFile.getAbsolutePath());
        
        // When: Validating invalid data
        ProcessingReport report = validator.validate(schema, invalidData);
        
        // Then: Validation should fail
        assertNotNull("Report should not be null", report);
        assertFalse("Validation should fail", report.isSuccess());
    }
    
    @Test
    public void testValidateFile_ValidFiles() throws IOException, ProcessingException {
        // When: Validating valid files
        ProcessingReport report = validator.validateFile(VALID_SCHEMA_PATH, VALID_YAML_PATH);
        
        // Then: Validation should be successful
        assertNotNull("Report should not be null", report);
        assertTrue("Validation should be successful", report.isSuccess());
    }
    
    @Test(expected = IOException.class)
    public void testValidateFile_NonExistentSchema() throws IOException, ProcessingException {
        // When: Validating with non-existent schema
        validator.validateFile("non-existent-schema.json", VALID_YAML_PATH);
        // Then: Should throw IOException
    }
    
    @Test(expected = IOException.class)
    public void testValidateFile_NonExistentYaml() throws IOException, ProcessingException {
        // When: Validating with non-existent YAML
        validator.validateFile(VALID_SCHEMA_PATH, "non-existent.yaml");
        // Then: Should throw IOException
    }
    
    @Test
    public void testIsValid_ValidReport() throws IOException, ProcessingException {
        // Given: A valid validation report
        ProcessingReport report = validator.validateFile(VALID_SCHEMA_PATH, VALID_YAML_PATH);
        
        // When: Checking if valid
        boolean isValid = validator.isValid(report);
        
        // Then: Should return true
        assertTrue("Should be valid", isValid);
    }
    
    @Test
    public void testIsValid_InvalidReport() throws IOException, ProcessingException {
        // Given: An invalid validation report
        var schema = validator.loadSchema(VALID_SCHEMA_PATH);
        
        // Create invalid data
        String invalidYaml = """
            - name: "test"
            """;
        File tempFile = tempFolder.newFile("invalid_test.yaml");
        Files.write(tempFile.toPath(), invalidYaml.getBytes());
        JsonNode invalidData = validator.loadYaml(tempFile.getAbsolutePath());
        ProcessingReport report = validator.validate(schema, invalidData);
        
        // When: Checking if valid
        boolean isValid = validator.isValid(report);
        
        // Then: Should return false
        assertFalse("Should be invalid", isValid);
    }
    
    @Test
    public void testPrintReport_ValidReport() throws IOException, ProcessingException {
        // Given: A valid validation report
        ProcessingReport report = validator.validateFile(VALID_SCHEMA_PATH, VALID_YAML_PATH);
        
        // When: Printing report
        // Then: Should not throw any exception
        validator.printReport(report);
    }
    
    @Test
    public void testPrintReport_InvalidReport() throws IOException, ProcessingException {
        // Given: An invalid validation report
        var schema = validator.loadSchema(VALID_SCHEMA_PATH);
        
        // Create invalid data
        String invalidYaml = """
            - name: "test"
            """;
        File tempFile = tempFolder.newFile("invalid_print.yaml");
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
        String complexSchema = """
            {
              "$schema": "http://json-schema.org/draft-07/schema#",
              "type": "object",
              "properties": {
                "name": {"type": "string"},
                "config": {
                  "type": "object",
                  "properties": {
                    "enabled": {"type": "boolean"},
                    "items": {
                      "type": "array",
                      "items": {"type": "string"}
                    }
                  },
                  "required": ["enabled"]
                }
              },
              "required": ["name", "config"]
            }
            """;
        
        String validData = """
            name: "test"
            config:
              enabled: true
              items:
                - "item1"
                - "item2"
            """;
        
        File schemaFile = tempFolder.newFile("complex_schema.json");
        File dataFile = tempFolder.newFile("complex_data.yaml");
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
        String enumSchema = """
            {
              "$schema": "http://json-schema.org/draft-07/schema#",
              "type": "object",
              "properties": {
                "status": {
                  "type": "string",
                  "enum": ["active", "inactive", "pending"]
                }
              },
              "required": ["status"]
            }
            """;
        
        String validData = """
            status: "active"
            """;
        
        String invalidData = """
            status: "invalid_status"
            """;
        
        File schemaFile = tempFolder.newFile("enum_schema.json");
        File validFile = tempFolder.newFile("valid_enum.yaml");
        File invalidFile = tempFolder.newFile("invalid_enum.yaml");
        
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
} 