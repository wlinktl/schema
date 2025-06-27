package com.demo.schema;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;

import java.io.IOException;

/**
 * Example demonstrating how to validate YAML files against JSON schemas
 */
public class SchemaValidationExample {
    
    public static void main(String[] args) {
        SchemaValidator validator = new SchemaValidator();
        
        // Paths to the schema and YAML files
        String schemaPath = "src/main/resources/schema/feeds_schema.json";
        String yamlPath = "src/main/resources/schema/feed_1.yaml";
        
        try {
            System.out.println("Validating YAML file against JSON schema...");
            System.out.println("Schema: " + schemaPath);
            System.out.println("YAML: " + yamlPath);
            System.out.println();
            
            // Perform validation
            ProcessingReport report = validator.validateFile(schemaPath, yamlPath);
            
            // Print results
            validator.printReport(report);
            
            // Additional analysis
            if (validator.isValid(report)) {
                System.out.println("✅ Validation successful! The YAML file conforms to the JSON schema.");
            } else {
                System.out.println("❌ Validation failed! The YAML file does not conform to the JSON schema.");
                System.out.println("Please fix the errors listed above.");
            }
            
        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
            e.printStackTrace();
        } catch (ProcessingException e) {
            System.err.println("Error processing schema: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 