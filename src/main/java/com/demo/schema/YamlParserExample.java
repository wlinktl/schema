package com.demo.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Example demonstrating how to use generated Java classes from JSON schema to parse YAML files
 * 
 * This example shows how to:
 * 1. Parse YAML files into strongly-typed Java objects
 * 2. Access properties using generated getters/setters
 * 3. Work with different feed types (file-watcher, kafka-file-replay, kafka-topic)
 * 4. Validate data structure at compile time
 */
public class YamlParserExample {
    
    private final ObjectMapper yamlMapper;
    
    public YamlParserExample() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
    }
    
    public static void main(String[] args) {
        YamlParserExample parser = new YamlParserExample();
        
        String[] yamlFiles = {
            "src/main/resources/schema/feed_file_1.yaml",
            "src/main/resources/schema/feed_kafka_1.yaml"
        };
        
        System.out.println("🔍 YAML Parser Example using Generated Classes");
        System.out.println("=============================================");
        
        for (String yamlFile : yamlFiles) {
            System.out.println("\n📄 Parsing: " + yamlFile);
            System.out.println("----------------------------------------");
            
            try {
                // Parse YAML into generated Java objects
                List<Object> feeds = parser.parseYamlFile(yamlFile);
                
                // Process each feed configuration
                for (int i = 0; i < feeds.size(); i++) {
                    Object feed = feeds.get(i);
                    System.out.println("Feed " + (i + 1) + ":");
                    parser.analyzeFeed(feed);
                    System.out.println();
                }
                
            } catch (IOException e) {
                System.err.println("❌ Error parsing YAML file: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("❌ Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Demonstrate specific features
        System.out.println("🔧 Demonstrating Generated Class Features");
        System.out.println("=========================================");
        parser.demonstrateFeatures();
    }
    
    /**
     * Parse YAML file into a list of feed configurations
     */
    @SuppressWarnings("unchecked")
    public List<Object> parseYamlFile(String yamlPath) throws IOException {
        // Note: We use Object here because the generated classes will be in a different package
        // In a real implementation, you would import the generated classes
        return yamlMapper.readValue(new File(yamlPath), List.class);
    }
    
    /**
     * Analyze a feed configuration and print its details
     */
    public void analyzeFeed(Object feed) {
        try {
            // Use reflection to access properties (in real implementation, use generated classes)
            System.out.println("  Type: " + feed.getClass().getSimpleName());
            
            // Check for different feed types
            if (hasProperty(feed, "file-watcher")) {
                System.out.println("  Feed Type: File Watcher");
                analyzeFileWatcher(feed);
            } else if (hasProperty(feed, "kafka-file-replay")) {
                System.out.println("  Feed Type: Kafka File Replay");
                analyzeKafkaFileReplay(feed);
            } else if (hasProperty(feed, "kafka-topic")) {
                System.out.println("  Feed Type: Kafka Topic");
                analyzeKafkaTopic(feed);
            }
            
            // Analyze inbound datasets
            if (hasProperty(feed, "inbound-datasets")) {
                System.out.println("  Inbound Datasets: " + getPropertyValue(feed, "inbound-datasets"));
            }
            
        } catch (Exception e) {
            System.err.println("  Error analyzing feed: " + e.getMessage());
        }
    }
    
    /**
     * Analyze file watcher configuration
     */
    private void analyzeFileWatcher(Object feed) {
        Object fileWatcher = getPropertyValue(feed, "file-watcher");
        if (fileWatcher != null) {
            System.out.println("    Name: " + getPropertyValue(fileWatcher, "name"));
            System.out.println("    Active: " + getPropertyValue(fileWatcher, "active"));
            System.out.println("    Interval: " + getPropertyValue(fileWatcher, "interval") + " seconds");
            System.out.println("    Inbound URI: " + getPropertyValue(fileWatcher, "inbound-uri"));
        }
    }
    
    /**
     * Analyze kafka file replay configuration
     */
    private void analyzeKafkaFileReplay(Object feed) {
        Object kafkaFileReplay = getPropertyValue(feed, "kafka-file-replay");
        if (kafkaFileReplay != null) {
            System.out.println("    Name: " + getPropertyValue(kafkaFileReplay, "name"));
            System.out.println("    Flow Class: " + getPropertyValue(kafkaFileReplay, "flow-class"));
            System.out.println("    Active: " + getPropertyValue(kafkaFileReplay, "active"));
            System.out.println("    Interval: " + getPropertyValue(kafkaFileReplay, "interval") + " seconds");
        }
    }
    
    /**
     * Analyze kafka topic configuration
     */
    private void analyzeKafkaTopic(Object feed) {
        Object kafkaTopic = getPropertyValue(feed, "kafka-topic");
        if (kafkaTopic != null) {
            System.out.println("    Name: " + getPropertyValue(kafkaTopic, "name"));
            System.out.println("    Flow Class: " + getPropertyValue(kafkaTopic, "flow-class"));
            System.out.println("    Bootstrap Servers: " + getPropertyValue(kafkaTopic, "bootstrap-servers"));
            System.out.println("    Topics: " + getPropertyValue(kafkaTopic, "topics"));
            System.out.println("    Consumer Group ID: " + getPropertyValue(kafkaTopic, "consumer-group-id"));
            
            // Analyze configs
            Object configs = getPropertyValue(kafkaTopic, "configs");
            if (configs != null) {
                System.out.println("    Configs: " + configs);
            }
        }
    }
    
    /**
     * Check if an object has a specific property
     */
    private boolean hasProperty(Object obj, String propertyName) {
        try {
            return obj.getClass().getMethod("get" + capitalize(propertyName.replace("-", ""))) != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
    
    /**
     * Get property value using reflection
     */
    private Object getPropertyValue(Object obj, String propertyName) {
        try {
            String methodName = "get" + capitalize(propertyName.replace("-", ""));
            return obj.getClass().getMethod(methodName).invoke(obj);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Capitalize first letter of a string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Demonstrate specific features of the generated classes
     */
    public void demonstrateFeatures() {
        System.out.println("✅ Benefits of using generated classes:");
        System.out.println("  - Type safety at compile time");
        System.out.println("  - IntelliSense/autocomplete support");
        System.out.println("  - Builder pattern support");
        System.out.println("  - Jackson annotations for serialization");
        System.out.println("  - Validation annotations");
        System.out.println("  - toString(), equals(), hashCode() methods");
        System.out.println();
        
        System.out.println("📝 Example usage with generated classes:");
        System.out.println("  // Parse YAML");
        System.out.println("  List<FeedConfiguration> feeds = yamlMapper.readValue(file, new TypeReference<List<FeedConfiguration>>() {});");
        System.out.println();
        System.out.println("  // Access properties with type safety");
        System.out.println("  for (FeedConfiguration feed : feeds) {");
        System.out.println("    if (feed.getFileWatcher() != null) {");
        System.out.println("      System.out.println(\"File watcher: \" + feed.getFileWatcher().getName());");
        System.out.println("    }");
        System.out.println("    if (feed.getKafkaTopic() != null) {");
        System.out.println("      System.out.println(\"Kafka topic: \" + feed.getKafkaTopic().getTopics());");
        System.out.println("    }");
        System.out.println("  }");
        System.out.println();
        
        System.out.println("🔧 To generate classes, run:");
        System.out.println("  mvn jsonschema2pojo:generate");
        System.out.println();
        System.out.println("📁 Generated classes will be in: com.demo.schema.generated");
    }
} 