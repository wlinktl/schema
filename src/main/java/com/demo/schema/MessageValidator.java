package com.demo.schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * Validates JSON messages against JSON schemas using Jackson and JSON Schema validation.
 * This class provides methods to parse and validate JSON files against their corresponding schemas.
 */
public class MessageValidator {
    
    private final ObjectMapper objectMapper;
    private final JsonSchemaFactory schemaFactory;
    
    public MessageValidator() {
        this.objectMapper = new ObjectMapper();
        this.schemaFactory = JsonSchemaFactory.byDefault();
    }
    
    /**
     * Validates a JSON message file against a JSON schema file.
     * 
     * @param messagePath Path to the JSON message file
     * @param schemaPath Path to the JSON schema file
     * @return ValidationResult containing validation status and any errors
     */
    public ValidationResult validateMessage(String messagePath, String schemaPath) {
        try {
            // Read and parse the schema
            JsonNode schemaNode = readJsonFile(schemaPath);
            JsonSchema schema = schemaFactory.getJsonSchema(schemaNode);
            
            // Read and parse the message
            JsonNode messageNode = readJsonFile(messagePath);
            
            // Validate the message against the schema
            ProcessingReport report = schema.validate(messageNode);
            
            return new ValidationResult(report.isSuccess(), report.toString());
            
        } catch (IOException e) {
            return new ValidationResult(false, "IO Error: " + e.getMessage());
        } catch (ProcessingException e) {
            return new ValidationResult(false, "Schema Processing Error: " + e.getMessage());
        } catch (Exception e) {
            return new ValidationResult(false, "Unexpected Error: " + e.getMessage());
        }
    }
    
    /**
     * Validates a JSON message string against a JSON schema string.
     * 
     * @param messageJson JSON message as string
     * @param schemaJson JSON schema as string
     * @return ValidationResult containing validation status and any errors
     */
    public ValidationResult validateMessageString(String messageJson, String schemaJson) {
        try {
            // Parse the schema
            JsonNode schemaNode = objectMapper.readTree(schemaJson);
            JsonSchema schema = schemaFactory.getJsonSchema(schemaNode);
            
            // Parse the message
            JsonNode messageNode = objectMapper.readTree(messageJson);
            
            // Validate the message against the schema
            ProcessingReport report = schema.validate(messageNode);
            
            return new ValidationResult(report.isSuccess(), report.toString());
            
        } catch (IOException e) {
            return new ValidationResult(false, "JSON Parsing Error: " + e.getMessage());
        } catch (ProcessingException e) {
            return new ValidationResult(false, "Schema Processing Error: " + e.getMessage());
        } catch (Exception e) {
            return new ValidationResult(false, "Unexpected Error: " + e.getMessage());
        }
    }
    
    /**
     * Reads and parses a JSON file from the classpath or file system.
     * 
     * @param filePath Path to the JSON file
     * @return Parsed JsonNode
     * @throws IOException if the file cannot be read or parsed
     */
    private JsonNode readJsonFile(String filePath) throws IOException {
        // First try to load from classpath
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream != null) {
                return objectMapper.readTree(inputStream);
            }
        }
        
        // If not found in classpath, try to read from file system
        if (Files.exists(Paths.get(filePath))) {
            return objectMapper.readTree(Paths.get(filePath).toFile());
        }
        
        throw new IOException("File not found: " + filePath);
    }
    
    /**
     * Result of JSON schema validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String details;
        
        public ValidationResult(boolean valid, String details) {
            this.valid = valid;
            this.details = details;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getDetails() {
            return details;
        }
        
        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + valid +
                    ", details='" + details + '\'' +
                    '}';
        }
    }
    
    /**
     * Main method for testing the validator with the provided message.json and schema.json files.
     */
    public static void main(String[] args) {
        MessageValidator validator = new MessageValidator();

        String messageJson = "src/main/resources/message.json";
        String schemaJson = "src/main/resources/schema.json";

        System.out.println("messageJson: " + messageJson);
        System.out.println("schemaJson: " + schemaJson);
        
        // Validate the message against the schema
        ValidationResult result = validator.validateMessage(
            messageJson, 
            schemaJson
        );
        
        System.out.println("Validation Result:");
        System.out.println("Valid: " + result.isValid());
        System.out.println("Details: " + result.getDetails());
        
        if (!result.isValid()) {
            System.out.println("\nValidation failed. Please check the schema requirements.");
        } else {
            System.out.println("\nValidation successful! The message conforms to the schema.");
        }
    }
} 