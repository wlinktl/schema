package com.demo.schema.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Valid;
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
 * Example demonstrating complex cross-field validation using Spring Validation API
 * 
 * This example shows how to implement validation rules that cannot be expressed in JSON Schema,
 * such as: "if field A is 10 and field B is 200, then field C must be 300 or 400"
 */
public class ComplexValidationExample {
    
    private final ObjectMapper yamlMapper;
    private final Validator validator;
    
    public ComplexValidationExample() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }
    
    public static void main(String[] args) {
        ComplexValidationExample example = new ComplexValidationExample();
        
        System.out.println("🔍 Complex Validation Example");
        System.out.println("=============================");
        
        // Test with valid data
        example.testValidData();
        
        // Test with invalid data
        example.testInvalidData();
        
        // Test with real YAML files
        example.testYamlValidation();
    }
    
    /**
     * Test with valid data that passes all validation rules
     */
    public void testValidData() {
        System.out.println("\n📄 Testing Valid Data:");
        System.out.println("----------------------");
        
        // Valid case: A=10, B=200, C=300 (should pass)
        FeedConfiguration validConfig = new FeedConfiguration();
        validConfig.setName("valid-feed");
        validConfig.setFieldA(10);
        validConfig.setFieldB(200);
        validConfig.setFieldC(300);
        
        Set<javax.validation.ConstraintViolation<FeedConfiguration>> violations = validator.validate(validConfig);
        
        if (violations.isEmpty()) {
            System.out.println("✅ Valid configuration passed all validation rules");
        } else {
            System.out.println("❌ Valid configuration failed validation:");
            violations.forEach(violation -> 
                System.out.println("  - " + violation.getPropertyPath() + ": " + violation.getMessage()));
        }
    }
    
    /**
     * Test with invalid data that violates validation rules
     */
    public void testInvalidData() {
        System.out.println("\n📄 Testing Invalid Data:");
        System.out.println("------------------------");
        
        // Invalid case: A=10, B=200, C=500 (should fail - C must be 300 or 400)
        FeedConfiguration invalidConfig = new FeedConfiguration();
        invalidConfig.setName("invalid-feed");
        invalidConfig.setFieldA(10);
        invalidConfig.setFieldB(200);
        invalidConfig.setFieldC(500);
        
        Set<javax.validation.ConstraintViolation<FeedConfiguration>> violations = validator.validate(invalidConfig);
        
        if (violations.isEmpty()) {
            System.out.println("❌ Invalid configuration unexpectedly passed validation");
        } else {
            System.out.println("✅ Invalid configuration correctly failed validation:");
            violations.forEach(violation -> 
                System.out.println("  - " + violation.getPropertyPath() + ": " + violation.getMessage()));
        }
    }
    
    /**
     * Test validation with real YAML files
     */
    public void testYamlValidation() {
        System.out.println("\n📄 Testing YAML File Validation:");
        System.out.println("--------------------------------");
        
        try {
            // Parse YAML and validate
            List<FeedConfiguration> feeds = yamlMapper.readValue(
                new File("src/main/resources/schema/feed_file_1.yaml"), 
                yamlMapper.getTypeFactory().constructCollectionType(List.class, FeedConfiguration.class)
            );
            
            for (int i = 0; i < feeds.size(); i++) {
                FeedConfiguration feed = feeds.get(i);
                System.out.println("Feed " + (i + 1) + ": " + feed.getName());
                
                Set<javax.validation.ConstraintViolation<FeedConfiguration>> violations = validator.validate(feed);
                
                if (violations.isEmpty()) {
                    System.out.println("  ✅ Passed all validation rules");
                } else {
                    System.out.println("  ❌ Failed validation:");
                    violations.forEach(violation -> 
                        System.out.println("    - " + violation.getPropertyPath() + ": " + violation.getMessage()));
                }
            }
            
        } catch (IOException e) {
            System.err.println("❌ Error reading YAML file: " + e.getMessage());
        }
    }
    
    /**
     * Custom annotation for complex validation rule
     * Rule: If fieldA is 10 and fieldB is 200, then fieldC must be 300 or 400
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = ComplexRuleValidator.class)
    public @interface ComplexValidationRule {
        String message() default "Complex validation rule failed: if fieldA is 10 and fieldB is 200, then fieldC must be 300 or 400";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
    
    /**
     * Custom validator implementation for complex business rules
     */
    public static class ComplexRuleValidator implements ConstraintValidator<ComplexValidationRule, FeedConfiguration> {
        
        @Override
        public boolean isValid(FeedConfiguration config, ConstraintValidatorContext context) {
            if (config == null) {
                return true; // Let @NotNull handle null validation
            }
            
            // Rule: If fieldA is 10 and fieldB is 200, then fieldC must be 300 or 400
            if (config.getFieldA() == 10 && config.getFieldB() == 200) {
                return config.getFieldC() == 300 || config.getFieldC() == 400;
            }
            
            // If the condition is not met, validation passes
            return true;
        }
    }
    
    /**
     * Example feed configuration class with complex validation
     */
    @ComplexValidationRule
    public static class FeedConfiguration {
        
        @NotNull(message = "Name is required")
        private String name;
        
        private Integer fieldA;
        private Integer fieldB;
        private Integer fieldC;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Integer getFieldA() { return fieldA; }
        public void setFieldA(Integer fieldA) { this.fieldA = fieldA; }
        
        public Integer getFieldB() { return fieldB; }
        public void setFieldB(Integer fieldB) { this.fieldB = fieldB; }
        
        public Integer getFieldC() { return fieldC; }
        public void setFieldC(Integer fieldC) { this.fieldC = fieldC; }
        
        @Override
        public String toString() {
            return "FeedConfiguration{name='" + name + "', fieldA=" + fieldA + 
                   ", fieldB=" + fieldB + ", fieldC=" + fieldC + "}";
        }
    }
    
    /**
     * More complex example with multiple validation rules
     */
    public static class AdvancedValidationExample {
        
        /**
         * Custom annotation for advanced validation
         */
        @Target({ElementType.TYPE})
        @Retention(RetentionPolicy.RUNTIME)
        @Constraint(validatedBy = AdvancedRuleValidator.class)
        public @interface AdvancedValidationRule {
            String message() default "Advanced validation rule failed";
            Class<?>[] groups() default {};
            Class<? extends Payload>[] payload() default {};
        }
        
        /**
         * Advanced validator with multiple complex rules
         */
        public static class AdvancedRuleValidator implements ConstraintValidator<AdvancedValidationRule, AdvancedFeedConfig> {
            
            @Override
            public boolean isValid(AdvancedFeedConfig config, ConstraintValidatorContext context) {
                if (config == null) {
                    return true;
                }
                
                // Rule 1: If type is "kafka" and security is "ssl", then port must be 9093
                if ("kafka".equals(config.getType()) && "ssl".equals(config.getSecurity())) {
                    if (config.getPort() != 9093) {
                        addConstraintViolation(context, "If type is kafka and security is ssl, port must be 9093");
                        return false;
                    }
                }
                
                // Rule 2: If compression is "gzip", then maxFileSize must be <= 100MB
                if ("gzip".equals(config.getCompression())) {
                    if (config.getMaxFileSize() > 100 * 1024 * 1024) { // 100MB in bytes
                        addConstraintViolation(context, "If compression is gzip, maxFileSize must be <= 100MB");
                        return false;
                    }
                }
                
                // Rule 3: If retryCount > 3, then timeout must be >= 30 seconds
                if (config.getRetryCount() > 3) {
                    if (config.getTimeout() < 30) {
                        addConstraintViolation(context, "If retryCount > 3, timeout must be >= 30 seconds");
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
         * Advanced feed configuration with multiple validation rules
         */
        @AdvancedValidationRule
        public static class AdvancedFeedConfig {
            @NotNull
            private String name;
            private String type;
            private String security;
            private Integer port;
            private String compression;
            private Long maxFileSize;
            private Integer retryCount;
            private Integer timeout;
            
            // Getters and setters
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            
            public String getSecurity() { return security; }
            public void setSecurity(String security) { this.security = security; }
            
            public Integer getPort() { return port; }
            public void setPort(Integer port) { this.port = port; }
            
            public String getCompression() { return compression; }
            public void setCompression(String compression) { this.compression = compression; }
            
            public Long getMaxFileSize() { return maxFileSize; }
            public void setMaxFileSize(Long maxFileSize) { this.maxFileSize = maxFileSize; }
            
            public Integer getRetryCount() { return retryCount; }
            public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
            
            public Integer getTimeout() { return timeout; }
            public void setTimeout(Integer timeout) { this.timeout = timeout; }
        }
    }
} 