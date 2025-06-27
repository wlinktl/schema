package com.demo.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Schema validator for validating YAML files against JSON schemas
 */
public class SchemaValidator {
    
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;
    private final JsonSchemaFactory schemaFactory;
    
    public SchemaValidator() {
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.schemaFactory = JsonSchemaFactory.byDefault();
    }
    
    /**
     * Load JSON schema from file
     * @param schemaPath Path to the JSON schema file
     * @return JsonSchema object
     * @throws IOException If file cannot be read
     * @throws ProcessingException If schema is invalid
     */
    public JsonSchema loadSchema(String schemaPath) throws IOException, ProcessingException {
        String schemaContent = Files.readString(Paths.get(schemaPath));
        JsonNode schemaNode = jsonMapper.readTree(schemaContent);
        return schemaFactory.getJsonSchema(schemaNode);
    }
    
    /**
     * Load YAML data from file
     * @param yamlPath Path to the YAML file
     * @return JsonNode representing the YAML data
     * @throws IOException If file cannot be read
     */
    public JsonNode loadYaml(String yamlPath) throws IOException {
        return yamlMapper.readTree(new File(yamlPath));
    }
    
    /**
     * Validate YAML data against JSON schema
     * @param schema The JSON schema to validate against
     * @param data The YAML data to validate
     * @return ProcessingReport containing validation results
     * @throws ProcessingException If validation fails
     */
    public ProcessingReport validate(JsonSchema schema, JsonNode data) throws ProcessingException {
        return schema.validate(data);
    }
    
    /**
     * Convenience method to validate a YAML file against a JSON schema file
     * @param schemaPath Path to the JSON schema file
     * @param yamlPath Path to the YAML file to validate
     * @return ProcessingReport containing validation results
     * @throws IOException If files cannot be read
     * @throws ProcessingException If validation fails
     */
    public ProcessingReport validateFile(String schemaPath, String yamlPath) throws IOException, ProcessingException {
        JsonSchema schema = loadSchema(schemaPath);
        JsonNode data = loadYaml(yamlPath);
        return validate(schema, data);
    }
    
    /**
     * Check if validation was successful
     * @param report The processing report
     * @return true if validation passed, false otherwise
     */
    public boolean isValid(ProcessingReport report) {
        return report.isSuccess();
    }
    
    /**
     * Print validation report in a readable format
     * @param report The processing report
     */
    public void printReport(ProcessingReport report) {
        System.out.println("Validation Report:");
        System.out.println("==================");
        System.out.println("Valid: " + isValid(report));
        System.out.println();
        
        if (!isValid(report)) {
            System.out.println("Validation Errors:");
            System.out.println("==================");
            report.forEach(message -> {
                System.out.println("Level: " + message.getLogLevel());
                System.out.println("Message: " + message.getMessage());
                System.out.println("---");
            });
        }
    }
} 