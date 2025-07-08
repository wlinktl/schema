package com.demo.schema.validation;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Set;

/**
 * Test class for complex validation examples using JUnit 4
 */
public class ComplexValidationTest {

    private ComplexValidationExample complexExample;
    private SchemaWithCustomValidation schemaExample;

    @Before
    public void setUp() {
        complexExample = new ComplexValidationExample();
        schemaExample = new SchemaWithCustomValidation();
    }

    @Test
    public void testComplexValidationExample() {
        // This test demonstrates the basic complex validation
        assertNotNull(complexExample);
    }

    @Test
    public void testSchemaWithCustomValidation() {
        // This test demonstrates combining JSON Schema with custom validation
        assertNotNull(schemaExample);
    }

    @Test
    public void testCustomValidationRule() {
        // Test the custom validation rule: if fieldA=10 and fieldB=200, then fieldC must be 300 or 400

        // Valid case
        ComplexValidationExample.FeedConfiguration validConfig = new ComplexValidationExample.FeedConfiguration();
        validConfig.setName("test");
        validConfig.setFieldA(10);
        validConfig.setFieldB(200);
        validConfig.setFieldC(300);

        // This should pass validation
        assertTrue(isValidConfiguration(validConfig));

        // Invalid case
        ComplexValidationExample.FeedConfiguration invalidConfig = new ComplexValidationExample.FeedConfiguration();
        invalidConfig.setName("test");
        invalidConfig.setFieldA(10);
        invalidConfig.setFieldB(200);
        invalidConfig.setFieldC(500); // Should fail - must be 300 or 400

        // This should fail validation
        assertFalse(isValidConfiguration(invalidConfig));

        // Case where condition is not met (should pass)
        ComplexValidationExample.FeedConfiguration nonConditionalConfig = new ComplexValidationExample.FeedConfiguration();
        nonConditionalConfig.setName("test");
        nonConditionalConfig.setFieldA(5); // Not 10, so condition not met
        nonConditionalConfig.setFieldB(200);
        nonConditionalConfig.setFieldC(500); // Can be anything since condition not met

        // This should pass validation
        assertTrue(isValidConfiguration(nonConditionalConfig));
    }

    private boolean isValidConfiguration(ComplexValidationExample.FeedConfiguration config) {
        javax.validation.ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
        javax.validation.Validator validator = factory.getValidator();

        Set<javax.validation.ConstraintViolation<ComplexValidationExample.FeedConfiguration>> violations =
                validator.validate(config);

        return violations.isEmpty();
    }
} 