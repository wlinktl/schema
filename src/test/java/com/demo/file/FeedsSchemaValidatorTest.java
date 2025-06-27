package com.demo.file;

import com.demo.schema.FeedsSchemaValidator;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests for FeedsSchemaValidator
 */
public class FeedsSchemaValidatorTest {
    
    private FeedsSchemaValidator validator;
    private static final String SCHEMA_PATH = "src/main/resources/schema/feeds_schema.json";
    private static final String TEST_YAML_PATH = "src/test/resources/feed_1.yaml";
    
    @Before
    public void setUp() {
        validator = new FeedsSchemaValidator();
    }
    
    @Test
    public void testValidateFeedsConfiguration_ValidYaml() throws IOException, ProcessingException {
        // Given: A valid YAML file and JSON schema
        
        // When: Validating the configuration
        FeedsSchemaValidator.ValidationResult result = validator.validateFeedsConfiguration(SCHEMA_PATH, TEST_YAML_PATH);
        
        // Then: Validation should be successful
        assertTrue("Configuration should be valid", result.isValid());
        assertTrue("Should have no errors", result.getErrors().isEmpty());
        assertNotNull("Report should not be null", result.getReport());
    }
    
    @Test
    public void testValidateFeedsConfiguration_ErrorHandling() throws IOException, ProcessingException {
        // Given: A non-existent YAML file
        
        // When: Validating with non-existent file
        try {
            validator.validateFeedsConfiguration(SCHEMA_PATH, "non-existent.yaml");
            fail("Should throw IOException for non-existent file");
        } catch (IOException e) {
            // Expected exception
            assertNotNull("Exception message should not be null", e.getMessage());
        }
    }
    
    @Test
    public void testValidateFeedsConfiguration_InvalidSchema() throws IOException, ProcessingException {
        // Given: A non-existent schema file
        
        // When: Validating with non-existent schema
        try {
            validator.validateFeedsConfiguration("non-existent-schema.json", TEST_YAML_PATH);
            fail("Should throw IOException for non-existent schema");
        } catch (IOException e) {
            // Expected exception
            assertNotNull("Exception message should not be null", e.getMessage());
        }
    }
    
    @Test
    public void testPrintDetailedReport() throws IOException, ProcessingException {
        // Given: A validation result
        FeedsSchemaValidator.ValidationResult result = validator.validateFeedsConfiguration(SCHEMA_PATH, TEST_YAML_PATH);
        
        // When: Printing detailed report
        // Then: Should not throw any exception
        validator.printDetailedReport(result);
    }
    
    @Test
    public void testPrintConfigurationSummary() throws IOException {
        // Given: A YAML file
        
        // When: Printing configuration summary
        // Then: Should not throw any exception
        validator.printConfigurationSummary(TEST_YAML_PATH);
    }
    
    @Test
    public void testValidationResult_Properties() throws IOException, ProcessingException {
        // Given: A validation result
        FeedsSchemaValidator.ValidationResult result = validator.validateFeedsConfiguration(SCHEMA_PATH, TEST_YAML_PATH);
        
        // Then: All properties should be accessible
        assertNotNull("Errors list should not be null", result.getErrors());
        assertNotNull("Warnings list should not be null", result.getWarnings());
        assertNotNull("Report should not be null", result.getReport());
        
        // Test boolean property
        boolean isValid = result.isValid();
        assertTrue("Configuration should be valid", isValid);
    }
    
    @Test
    public void testValidateFeedsConfiguration_WithInvalidYaml() throws IOException, ProcessingException {
        // Given: An invalid YAML file (missing required fields)
        String invalidYamlContent = """
            - name: "test"
              # Missing required file-watcher and inbound-datasets
            """;
        
        // Create a temporary invalid YAML file for testing
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("invalid", ".yaml");
        java.nio.file.Files.write(tempFile, invalidYamlContent.getBytes());
        
        try {
            // When: Validating the invalid configuration
            FeedsSchemaValidator.ValidationResult result = validator.validateFeedsConfiguration(
                SCHEMA_PATH, tempFile.toString());
            
            // Then: Validation should fail
            assertFalse("Configuration should be invalid", result.isValid());
            assertFalse("Should have errors", result.getErrors().isEmpty());
            
        } finally {
            // Clean up
            java.nio.file.Files.deleteIfExists(tempFile);
        }
    }
} 