package com.demo.schema.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;

/**
 * Example showing how to combine JSON Schema validation with custom Spring Validation
 * for complex business rules that cannot be expressed in JSON Schema
 */
public class SchemaWithCustomValidation {
    
    private final ObjectMapper yamlMapper;
    private final Validator validator;
    private final JsonSchemaFactory schemaFactory;
    
    public SchemaWithCustomValidation() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
        this.schemaFactory = JsonSchemaFactory.byDefault();
    }
    
    public static void main(String[] args) {
        SchemaWithCustomValidation example = new SchemaWithCustomValidation();
        
        System.out.println("🔍 Schema + Custom Validation Example");
        System.out.println("=====================================");
        
        // Test validation with real YAML files
        example.validateYamlFiles();
    }
    
    /**
     * Validate YAML files using both JSON Schema and custom validation
     */
    public void validateYamlFiles() {
        String[] yamlFiles = {
            "src/main/resources/schema/feed_file_1.yaml",
            "src/main/resources/schema/feed_kafka_1.yaml"
        };
        
        for (String yamlFile : yamlFiles) {
            System.out.println("\n📄 Validating: " + yamlFile);
            System.out.println("----------------------------------------");
            
            try {
                // Step 1: JSON Schema validation
                boolean schemaValid = validateWithJsonSchema(yamlFile);
                
                if (!schemaValid) {
                    System.out.println("❌ Failed JSON Schema validation - skipping custom validation");
                    continue;
                }
                
                System.out.println("✅ Passed JSON Schema validation");
                
                // Step 2: Custom business rule validation
                validateWithCustomRules(yamlFile);
                
            } catch (Exception e) {
                System.err.println("❌ Error validating " + yamlFile + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Validate YAML file against JSON Schema
     */
    private boolean validateWithJsonSchema(String yamlFile) throws IOException, ProcessingException {
        // Load schema
        JsonNode schemaNode = yamlMapper.readTree(new File("src/main/resources/schema/feeds_schema.json"));
        JsonSchema schema = schemaFactory.getJsonSchema(schemaNode);
        
        // Load YAML data
        JsonNode yamlData = yamlMapper.readTree(new File(yamlFile));
        
        // Validate
        ProcessingReport report = schema.validate(yamlData);
        
        if (!report.isSuccess()) {
            System.out.println("❌ JSON Schema validation failed:");
            report.forEach(processingMessage -> 
                System.out.println("  - " + processingMessage.getMessage()));
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate YAML file with custom business rules
     */
    private void validateWithCustomRules(String yamlFile) throws IOException {
        // Parse YAML into our custom classes
        List<FeedConfigWithCustomValidation> feeds = yamlMapper.readValue(
            new File(yamlFile), 
            yamlMapper.getTypeFactory().constructCollectionType(List.class, FeedConfigWithCustomValidation.class)
        );
        
        for (int i = 0; i < feeds.size(); i++) {
            FeedConfigWithCustomValidation feed = feeds.get(i);
            System.out.println("Feed " + (i + 1) + ": " + feed.getName());
            
            Set<javax.validation.ConstraintViolation<FeedConfigWithCustomValidation>> violations = 
                validator.validate(feed);
            
            if (violations.isEmpty()) {
                System.out.println("  ✅ Passed all custom validation rules");
            } else {
                System.out.println("  ❌ Failed custom validation:");
                violations.forEach(violation -> 
                    System.out.println("    - " + violation.getPropertyPath() + ": " + violation.getMessage()));
            }
        }
    }
    
    /**
     * Custom annotation for business rule validation
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = BusinessRuleValidator.class)
    public @interface BusinessRuleValidation {
        String message() default "Business rule validation failed";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
    
    /**
     * Custom validator for business rules that cannot be expressed in JSON Schema
     */
    public static class BusinessRuleValidator implements ConstraintValidator<BusinessRuleValidation, FeedConfigWithCustomValidation> {
        
        @Override
        public boolean isValid(FeedConfigWithCustomValidation config, ConstraintValidatorContext context) {
            if (config == null) {
                return true;
            }
            
            // Rule 1: If file-watcher interval is < 30 seconds, then monitor-uri must be specified
            if (config.getFileWatcher() != null && config.getFileWatcher().getInterval() < 30) {
                if (config.getFileWatcher().getMonitorUri() == null || config.getFileWatcher().getMonitorUri().trim().isEmpty()) {
                    addConstraintViolation(context, "If file-watcher interval < 30 seconds, monitor-uri must be specified");
                    return false;
                }
            }
            
            // Rule 2: If kafka-topic has SSL security, then port must be 9093
            if (config.getKafkaTopic() != null && config.getKafkaTopic().getConfigs() != null) {
                Object securityProtocol = config.getKafkaTopic().getConfigs().get("security.protocol");
                if ("SSL".equals(securityProtocol) || "SASL_SSL".equals(securityProtocol)) {
                    Object port = config.getKafkaTopic().getConfigs().get("port");
                    if (port != null && !"9093".equals(port.toString())) {
                        addConstraintViolation(context, "If SSL security is used, port must be 9093");
                        return false;
                    }
                }
            }
            
            // Rule 3: If compression is gzip, then max file size must be <= 100MB
            if (config.getInboundDatasets() != null) {
                for (InboundDataset dataset : config.getInboundDatasets()) {
                    if ("gzip".equals(dataset.getCompression())) {
                        // This would require additional metadata about file sizes
                        // For demonstration, we'll check if there's a size limit specified
                        if (dataset.getMaxFileSize() != null && dataset.getMaxFileSize() > 100 * 1024 * 1024) {
                            addConstraintViolation(context, "If compression is gzip, max file size must be <= 100MB");
                            return false;
                        }
                    }
                }
            }
            
            // Rule 4: If kafka-file-replay is active, then flow-class must be specified
            if (config.getKafkaFileReplay() != null && config.getKafkaFileReplay().getActive() != null && config.getKafkaFileReplay().getActive()) {
                if (config.getKafkaFileReplay().getFlowClass() == null || 
                    config.getKafkaFileReplay().getFlowClass().trim().isEmpty()) {
                    addConstraintViolation(context, "If kafka-file-replay is active, flow-class must be specified");
                    return false;
                }
            }
            
            return true;
        }
        
        private void addConstraintViolation(ConstraintValidatorContext context, String message) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                   .addConstraintViolation();
        }
    }
    
    /**
     * Feed configuration class with custom validation
     */
    @BusinessRuleValidation
    public static class FeedConfigWithCustomValidation {
        
        @NotNull(message = "Name is required")
        private String name;
        
        private Boolean active;
        private String layer;
        private FileWatcherConfig fileWatcher;
        private KafkaFileReplayConfig kafkaFileReplay;
        private KafkaTopicConfig kafkaTopic;
        private List<InboundDataset> inboundDatasets;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        
        public String getLayer() { return layer; }
        public void setLayer(String layer) { this.layer = layer; }
        
        public FileWatcherConfig getFileWatcher() { return fileWatcher; }
        public void setFileWatcher(FileWatcherConfig fileWatcher) { this.fileWatcher = fileWatcher; }
        
        public KafkaFileReplayConfig getKafkaFileReplay() { return kafkaFileReplay; }
        public void setKafkaFileReplay(KafkaFileReplayConfig kafkaFileReplay) { this.kafkaFileReplay = kafkaFileReplay; }
        
        public KafkaTopicConfig getKafkaTopic() { return kafkaTopic; }
        public void setKafkaTopic(KafkaTopicConfig kafkaTopic) { this.kafkaTopic = kafkaTopic; }
        
        public List<InboundDataset> getInboundDatasets() { return inboundDatasets; }
        public void setInboundDatasets(List<InboundDataset> inboundDatasets) { this.inboundDatasets = inboundDatasets; }
    }
    
    /**
     * File watcher configuration
     */
    public static class FileWatcherConfig {
        private String name;
        private Boolean active;
        private Integer interval;
        private String inboundUri;
        private String preprocessUri;
        private String monitorUri;
        private String logsUri;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        
        public Integer getInterval() { return interval; }
        public void setInterval(Integer interval) { this.interval = interval; }
        
        public String getInboundUri() { return inboundUri; }
        public void setInboundUri(String inboundUri) { this.inboundUri = inboundUri; }
        
        public String getPreprocessUri() { return preprocessUri; }
        public void setPreprocessUri(String preprocessUri) { this.preprocessUri = preprocessUri; }
        
        public String getMonitorUri() { return monitorUri; }
        public void setMonitorUri(String monitorUri) { this.monitorUri = monitorUri; }
        
        public String getLogsUri() { return logsUri; }
        public void setLogsUri(String logsUri) { this.logsUri = logsUri; }
    }
    
    /**
     * Kafka file replay configuration
     */
    public static class KafkaFileReplayConfig {
        private String name;
        private Boolean active;
        private String flowClass;
        private Integer interval;
        private String inboundUri;
        private String preprocessUri;
        private String monitorUri;
        private String logsUri;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        
        public String getFlowClass() { return flowClass; }
        public void setFlowClass(String flowClass) { this.flowClass = flowClass; }
        
        public Integer getInterval() { return interval; }
        public void setInterval(Integer interval) { this.interval = interval; }
        
        public String getInboundUri() { return inboundUri; }
        public void setInboundUri(String inboundUri) { this.inboundUri = inboundUri; }
        
        public String getPreprocessUri() { return preprocessUri; }
        public void setPreprocessUri(String preprocessUri) { this.preprocessUri = preprocessUri; }
        
        public String getMonitorUri() { return monitorUri; }
        public void setMonitorUri(String monitorUri) { this.monitorUri = monitorUri; }
        
        public String getLogsUri() { return logsUri; }
        public void setLogsUri(String logsUri) { this.logsUri = logsUri; }
    }
    
    /**
     * Kafka topic configuration
     */
    public static class KafkaTopicConfig {
        private String name;
        private Boolean active;
        private String flowClass;
        private String inboundUri;
        private String preprocessUri;
        private String monitorUri;
        private String logsUri;
        private String bootstrapServers;
        private String topics;
        private String consumerGroupId;
        private String consumerId;
        private String startingOffsets;
        private java.util.Map<String, Object> configs;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        
        public String getFlowClass() { return flowClass; }
        public void setFlowClass(String flowClass) { this.flowClass = flowClass; }
        
        public String getInboundUri() { return inboundUri; }
        public void setInboundUri(String inboundUri) { this.inboundUri = inboundUri; }
        
        public String getPreprocessUri() { return preprocessUri; }
        public void setPreprocessUri(String preprocessUri) { this.preprocessUri = preprocessUri; }
        
        public String getMonitorUri() { return monitorUri; }
        public void setMonitorUri(String monitorUri) { this.monitorUri = monitorUri; }
        
        public String getLogsUri() { return logsUri; }
        public void setLogsUri(String logsUri) { this.logsUri = logsUri; }
        
        public String getBootstrapServers() { return bootstrapServers; }
        public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }
        
        public String getTopics() { return topics; }
        public void setTopics(String topics) { this.topics = topics; }
        
        public String getConsumerGroupId() { return consumerGroupId; }
        public void setConsumerGroupId(String consumerGroupId) { this.consumerGroupId = consumerGroupId; }
        
        public String getConsumerId() { return consumerId; }
        public void setConsumerId(String consumerId) { this.consumerId = consumerId; }
        
        public String getStartingOffsets() { return startingOffsets; }
        public void setStartingOffsets(String startingOffsets) { this.startingOffsets = startingOffsets; }
        
        public java.util.Map<String, Object> getConfigs() { return configs; }
        public void setConfigs(java.util.Map<String, Object> configs) { this.configs = configs; }
    }
    
    /**
     * Inbound dataset configuration
     */
    public static class InboundDataset {
        private String name;
        private Boolean active;
        private String pattern;
        private String type;
        private String module;
        private String format;
        private String compression;
        private String delimiter;
        private Boolean trailingDelimeter;
        private String addColumns;
        private String dropColumns;
        private String cobScript;
        private Long maxFileSize;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        
        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getModule() { return module; }
        public void setModule(String module) { this.module = module; }
        
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        
        public String getCompression() { return compression; }
        public void setCompression(String compression) { this.compression = compression; }
        
        public String getDelimiter() { return delimiter; }
        public void setDelimiter(String delimiter) { this.delimiter = delimiter; }
        
        public Boolean getTrailingDelimeter() { return trailingDelimeter; }
        public void setTrailingDelimeter(Boolean trailingDelimeter) { this.trailingDelimeter = trailingDelimeter; }
        
        public String getAddColumns() { return addColumns; }
        public void setAddColumns(String addColumns) { this.addColumns = addColumns; }
        
        public String getDropColumns() { return dropColumns; }
        public void setDropColumns(String dropColumns) { this.dropColumns = dropColumns; }
        
        public String getCobScript() { return cobScript; }
        public void setCobScript(String cobScript) { this.cobScript = cobScript; }
        
        public Long getMaxFileSize() { return maxFileSize; }
        public void setMaxFileSize(Long maxFileSize) { this.maxFileSize = maxFileSize; }
    }
} 