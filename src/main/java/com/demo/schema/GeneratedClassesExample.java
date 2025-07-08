package com.demo.schema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Example demonstrating how to use the generated Java classes from JSON schema
 * 
 * This example shows the ideal way to work with YAML data once the classes are generated.
 * Run 'mvn jsonschema2pojo:generate' first to generate the classes.
 */
public class GeneratedClassesExample {
    
    private final ObjectMapper yamlMapper;
    
    public GeneratedClassesExample() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }
    
    public static void main(String[] args) {
        GeneratedClassesExample example = new GeneratedClassesExample();
        
        System.out.println("🚀 Generated Classes Example");
        System.out.println("=============================");
        System.out.println("This example demonstrates type-safe YAML parsing");
        System.out.println("using generated Java classes from JSON schema.");
        System.out.println();
        
        // Note: This example assumes the classes have been generated
        // Run 'mvn jsonschema2pojo:generate' to generate them first
        
        String[] yamlFiles = {
            "src/main/resources/schema/feed_file_1.yaml",
            "src/main/resources/schema/feed_kafka_1.yaml"
        };
        
        for (String yamlFile : yamlFiles) {
            System.out.println("📄 Processing: " + yamlFile);
            System.out.println("----------------------------------------");
            
            try {
                // Parse YAML using generated classes (commented out until classes are generated)
                // List<FeedConfiguration> feeds = example.parseWithGeneratedClasses(yamlFile);
                
                // For now, use generic parsing to demonstrate the concept
                List<Map<String, Object>> feeds = example.parseGeneric(yamlFile);
                example.processFeeds(feeds);
                
            } catch (IOException e) {
                System.err.println("❌ Error processing file: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println();
        }
        
        // Show how to use generated classes
        example.showGeneratedClassUsage();
    }
    
    /**
     * Parse YAML using generated classes (ideal approach)
     * Uncomment this method once classes are generated
     */
    /*
    public List<FeedConfiguration> parseWithGeneratedClasses(String yamlPath) throws IOException {
        return yamlMapper.readValue(new File(yamlPath), new TypeReference<List<FeedConfiguration>>() {});
    }
    */
    
    /**
     * Parse YAML using generic Map (fallback approach)
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> parseGeneric(String yamlPath) throws IOException {
        return yamlMapper.readValue(new File(yamlPath), List.class);
    }
    
    /**
     * Process feeds and demonstrate type-safe access
     */
    public void processFeeds(List<Map<String, Object>> feeds) {
        for (int i = 0; i < feeds.size(); i++) {
            Map<String, Object> feed = feeds.get(i);
            System.out.println("Feed " + (i + 1) + ":");
            
            // Access feed properties
            String name = (String) feed.get("name");
            Boolean active = (Boolean) feed.get("active");
            String layer = (String) feed.get("layer");
            
            System.out.println("  Name: " + name);
            System.out.println("  Active: " + active);
            System.out.println("  Layer: " + layer);
            
            // Check feed type and process accordingly
            if (feed.containsKey("file-watcher")) {
                processFileWatcherFeed(feed);
            } else if (feed.containsKey("kafka-file-replay")) {
                processKafkaFileReplayFeed(feed);
            } else if (feed.containsKey("kafka-topic")) {
                processKafkaTopicFeed(feed);
            }
            
            // Process inbound datasets
            processInboundDatasets(feed);
            
            System.out.println();
        }
    }
    
    /**
     * Process file watcher feed configuration
     */
    @SuppressWarnings("unchecked")
    private void processFileWatcherFeed(Map<String, Object> feed) {
        Map<String, Object> fileWatcher = (Map<String, Object>) feed.get("file-watcher");
        if (fileWatcher != null) {
            System.out.println("  Feed Type: File Watcher");
            System.out.println("    Watcher Name: " + fileWatcher.get("name"));
            System.out.println("    Active: " + fileWatcher.get("active"));
            System.out.println("    Interval: " + fileWatcher.get("interval") + " seconds");
            System.out.println("    Inbound URI: " + fileWatcher.get("inbound-uri"));
            
            // Process transaction configuration
            processTransaction(fileWatcher);
        }
    }
    
    /**
     * Process kafka file replay feed configuration
     */
    @SuppressWarnings("unchecked")
    private void processKafkaFileReplayFeed(Map<String, Object> feed) {
        Map<String, Object> kafkaFileReplay = (Map<String, Object>) feed.get("kafka-file-replay");
        if (kafkaFileReplay != null) {
            System.out.println("  Feed Type: Kafka File Replay");
            System.out.println("    Name: " + kafkaFileReplay.get("name"));
            System.out.println("    Flow Class: " + kafkaFileReplay.get("flow-class"));
            System.out.println("    Active: " + kafkaFileReplay.get("active"));
            System.out.println("    Interval: " + kafkaFileReplay.get("interval") + " seconds");
            
            // Process transaction configuration
            processTransaction(kafkaFileReplay);
        }
    }
    
    /**
     * Process kafka topic feed configuration
     */
    @SuppressWarnings("unchecked")
    private void processKafkaTopicFeed(Map<String, Object> feed) {
        Map<String, Object> kafkaTopic = (Map<String, Object>) feed.get("kafka-topic");
        if (kafkaTopic != null) {
            System.out.println("  Feed Type: Kafka Topic");
            System.out.println("    Name: " + kafkaTopic.get("name"));
            System.out.println("    Flow Class: " + kafkaTopic.get("flow-class"));
            System.out.println("    Bootstrap Servers: " + kafkaTopic.get("bootstrap-servers"));
            System.out.println("    Topics: " + kafkaTopic.get("topics"));
            System.out.println("    Consumer Group ID: " + kafkaTopic.get("consumer-group-id"));
            System.out.println("    Consumer ID: " + kafkaTopic.get("consumer-id"));
            System.out.println("    Starting Offsets: " + kafkaTopic.get("starting-offsets"));
            
            // Process configs
            Map<String, Object> configs = (Map<String, Object>) kafkaTopic.get("configs");
            if (configs != null) {
                System.out.println("    Configs:");
                configs.forEach((key, value) -> 
                    System.out.println("      " + key + ": " + value));
            }
            
            // Process transaction configuration
            processTransaction(kafkaTopic);
        }
    }
    
    /**
     * Process transaction configuration
     */
    @SuppressWarnings("unchecked")
    private void processTransaction(Map<String, Object> component) {
        Map<String, Object> transaction = (Map<String, Object>) component.get("transaction");
        if (transaction != null) {
            System.out.println("    Transaction:");
            
            // Process commit
            Map<String, Object> commit = (Map<String, Object>) transaction.get("commit");
            if (commit != null) {
                System.out.println("      Commit:");
                System.out.println("        Success URI: " + commit.get("success-uri"));
                
                Map<String, Object> doneFile = (Map<String, Object>) commit.get("done-file");
                if (doneFile != null) {
                    System.out.println("        Done File:");
                    System.out.println("          URI: " + doneFile.get("uri"));
                    System.out.println("          Type: " + doneFile.get("type"));
                    System.out.println("          Template: " + doneFile.get("template"));
                }
            }
            
            // Process rollback
            Map<String, Object> rollback = (Map<String, Object>) transaction.get("rollback");
            if (rollback != null) {
                System.out.println("      Rollback:");
                System.out.println("        Failure URI: " + rollback.get("failure-uri"));
            }
        }
    }
    
    /**
     * Process inbound datasets
     */
    @SuppressWarnings("unchecked")
    private void processInboundDatasets(Map<String, Object> feed) {
        List<Map<String, Object>> datasets = (List<Map<String, Object>>) feed.get("inbound-datasets");
        if (datasets != null) {
            System.out.println("  Inbound Datasets (" + datasets.size() + "):");
            for (int i = 0; i < datasets.size(); i++) {
                Map<String, Object> dataset = datasets.get(i);
                System.out.println("    Dataset " + (i + 1) + ":");
                System.out.println("      Name: " + dataset.get("name"));
                System.out.println("      Active: " + dataset.get("active"));
                System.out.println("      Pattern: " + dataset.get("pattern"));
                System.out.println("      Type: " + dataset.get("type"));
                System.out.println("      Module: " + dataset.get("module"));
                System.out.println("      Format: " + dataset.get("format"));
                System.out.println("      Compression: " + dataset.get("compression"));
                
                // Process output configuration
                Map<String, Object> output = (Map<String, Object>) dataset.get("output");
                if (output != null) {
                    Map<String, Object> hive = (Map<String, Object>) output.get("hive");
                    if (hive != null) {
                        System.out.println("      Hive Output:");
                        System.out.println("        Table: " + hive.get("table-name"));
                        System.out.println("        Partitions: " + hive.get("partitions"));
                    }
                }
            }
        }
    }
    
    /**
     * Show how to use generated classes once they're available
     */
    public void showGeneratedClassUsage() {
        System.out.println("🔧 Generated Classes Usage Guide");
        System.out.println("================================");
        System.out.println();
        
        System.out.println("1. Generate classes:");
        System.out.println("   mvn jsonschema2pojo:generate");
        System.out.println();
        
        System.out.println("2. Import generated classes:");
        System.out.println("   import com.demo.schema.generated.*;");
        System.out.println();
        
        System.out.println("3. Parse YAML with type safety:");
        System.out.println("   List<FeedConfiguration> feeds = yamlMapper.readValue(");
        System.out.println("       file, new TypeReference<List<FeedConfiguration>>() {});");
        System.out.println();
        
        System.out.println("4. Access properties safely:");
        System.out.println("   for (FeedConfiguration feed : feeds) {");
        System.out.println("       // Type-safe access");
        System.out.println("       if (feed.getFileWatcher() != null) {");
        System.out.println("           FileWatcher watcher = feed.getFileWatcher();");
        System.out.println("           System.out.println(watcher.getName());");
        System.out.println("           System.out.println(watcher.getInterval());");
        System.out.println("       }");
        System.out.println("       ");
        System.out.println("       if (feed.getKafkaTopic() != null) {");
        System.out.println("           KafkaTopic topic = feed.getKafkaTopic();");
        System.out.println("           System.out.println(topic.getTopics());");
        System.out.println("           System.out.println(topic.getBootstrapServers());");
        System.out.println("       }");
        System.out.println("   }");
        System.out.println();
        
        System.out.println("5. Use builder pattern:");
        System.out.println("   FeedConfiguration feed = FeedConfiguration.builder()");
        System.out.println("       .name(\"my-feed\")");
        System.out.println("       .active(true)");
        System.out.println("       .layer(\"production\")");
        System.out.println("       .build();");
        System.out.println();
        
        System.out.println("✅ Benefits:");
        System.out.println("   - Compile-time type checking");
        System.out.println("   - IDE autocomplete and refactoring");
        System.out.println("   - No runtime casting errors");
        System.out.println("   - Better code maintainability");
    }
} 