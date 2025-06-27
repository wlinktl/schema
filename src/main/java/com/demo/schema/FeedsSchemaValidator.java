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
import java.util.ArrayList;
import java.util.List;

/**
 * Specialized validator for feeds configuration schema
 */
public class FeedsSchemaValidator {
    
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;
    private final JsonSchemaFactory schemaFactory;
    
    public FeedsSchemaValidator() {
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.schemaFactory = JsonSchemaFactory.byDefault();
    }
    
    /**
     * Validation result containing detailed information
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        private final ProcessingReport report;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings, ProcessingReport report) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
            this.report = report;
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public ProcessingReport getReport() { return report; }
    }
    
    /**
     * Validate feeds configuration with detailed reporting
     */
    public ValidationResult validateFeedsConfiguration(String schemaPath, String yamlPath) 
            throws IOException, ProcessingException {
        
        JsonSchema schema = loadSchema(schemaPath);
        JsonNode data = loadYaml(yamlPath);
        ProcessingReport report = schema.validate(data);
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Analyze the report
        report.forEach(message -> {
            String errorMsg = String.format("[%s] %s", 
                message.getLogLevel(), 
                message.getMessage());
            
            if (message.getLogLevel().name().equals("ERROR")) {
                errors.add(errorMsg);
            } else {
                warnings.add(errorMsg);
            }
        });
        
        return new ValidationResult(report.isSuccess(), errors, warnings, report);
    }
    
    /**
     * Load JSON schema from file
     */
    private JsonSchema loadSchema(String schemaPath) throws IOException, ProcessingException {
        String schemaContent = Files.readString(Paths.get(schemaPath));
        JsonNode schemaNode = jsonMapper.readTree(schemaContent);
        return schemaFactory.getJsonSchema(schemaNode);
    }
    
    /**
     * Load YAML data from file
     */
    private JsonNode loadYaml(String yamlPath) throws IOException {
        return yamlMapper.readTree(new File(yamlPath));
    }
    
    /**
     * Print detailed validation report
     */
    public void printDetailedReport(ValidationResult result) {
        System.out.println("=== Feeds Configuration Validation Report ===");
        System.out.println("Overall Status: " + (result.isValid() ? "✅ VALID" : "❌ INVALID"));
        System.out.println();
        
        if (!result.getErrors().isEmpty()) {
            System.out.println("❌ ERRORS (" + result.getErrors().size() + "):");
            System.out.println("================");
            for (int i = 0; i < result.getErrors().size(); i++) {
                System.out.println((i + 1) + ". " + result.getErrors().get(i));
            }
            System.out.println();
        }
        
        if (!result.getWarnings().isEmpty()) {
            System.out.println("⚠️  WARNINGS (" + result.getWarnings().size() + "):");
            System.out.println("=================");
            for (int i = 0; i < result.getWarnings().size(); i++) {
                System.out.println((i + 1) + ". " + result.getWarnings().get(i));
            }
            System.out.println();
        }
        
        if (result.isValid()) {
            System.out.println("✅ Configuration is valid and ready for use!");
        } else {
            System.out.println("❌ Configuration has validation errors that need to be fixed.");
        }
    }
    
    /**
     * Get configuration summary
     */
    public void printConfigurationSummary(String yamlPath) throws IOException {
        JsonNode data = loadYaml(yamlPath);
        
        System.out.println("=== Configuration Summary ===");
        if (data.isArray()) {
            System.out.println("Number of configurations: " + data.size());
            
            for (int i = 0; i < data.size(); i++) {
                JsonNode config = data.get(i);
                System.out.println("\nConfiguration " + (i + 1) + ":");
                System.out.println("  Name: " + config.path("name").asText("N/A"));
                System.out.println("  Active: " + config.path("active").asBoolean(true));
                System.out.println("  Layer: " + config.path("layer").asText("N/A"));
                
                JsonNode fileWatcher = config.path("file-watcher");
                if (!fileWatcher.isMissingNode()) {
                    System.out.println("  File Watcher: " + fileWatcher.path("name").asText("N/A"));
                    System.out.println("  Interval: " + fileWatcher.path("interval").asInt(0) + " seconds");
                }
                
                JsonNode datasets = config.path("inbound-datasets");
                if (datasets.isArray()) {
                    System.out.println("  Datasets: " + datasets.size());
                }
            }
        }
        System.out.println();
    }
} 