"# file" 
"# schema" 

# Schema Validation and Code Generation

This project demonstrates JSON Schema validation for YAML feed configurations and automatic Java code generation from JSON schemas.

## Features

- ✅ **JSON Schema Validation**: Validate YAML files against JSON schemas
- ✅ **Multiple Feed Types**: Support for file-watcher, kafka-file-replay, and kafka-topic feeds
- ✅ **Flexible Configuration**: Support for various data types in Kafka configs
- ✅ **Code Generation**: Generate strongly-typed Java classes from JSON schemas
- ✅ **Type-Safe Parsing**: Parse YAML files into typed Java objects
- ✅ **Complex Validation**: Custom business rules using Spring Validation API (Java 8 compatible)

## Project Structure

```
src/
├── main/
│   ├── java/com/demo/schema/
│   │   ├── SchemaValidator.java          # Core validation logic
│   │   ├── SchemaValidationExample.java  # Schema validation example
│   │   ├── YamlParserExample.java        # YAML parsing with generated classes
│   │   └── GeneratedClassesExample.java  # Advanced usage examples
│   └── resources/schema/
│       ├── feeds_schema.json             # JSON schema definition
│       ├── feed_file_1.yaml              # File watcher example
│       └── feed_kafka_1.yaml             # Kafka feeds example
└── test/
    └── java/com/demo/schema/
        └── SchemaValidatorTest.java      # Unit tests
```

## Quick Start

### 1. Build the Project

```bash
# Using Maven
mvn clean compile

# Using Gradle
./gradlew build
```

### 2. Generate Java Classes from JSON Schema

```bash
# Using Maven
mvn jsonschema2pojo:generate

# Using Gradle
./gradlew jsonschema2pojo
```

This will generate Java classes in `target/generated-sources/jsonschema2pojo/com/demo/schema/generated/`

### 3. Run Examples

```bash
# Schema validation example
mvn exec:java -Dexec.mainClass="com.demo.schema.SchemaValidationExample"

# YAML parsing with generated classes
mvn exec:java -Dexec.mainClass="com.demo.schema.YamlParserExample"

# Advanced generated classes example
mvn exec:java -Dexec.mainClass="com.demo.schema.GeneratedClassesExample"
```

## Schema Features

### Supported Feed Types

1. **File Watcher Feeds**
   - File monitoring with polling intervals
   - Transaction handling with commit/rollback
   - Done file generation

2. **Kafka File Replay Feeds**
   - Kafka-based file replay processing
   - Flow class configuration
   - Transaction management

3. **Kafka Topic Feeds**
   - Direct Kafka topic consumption
   - Flexible configuration properties
   - Security and SSL support

### Flexible Configuration

The schema supports flexible key-value pairs in Kafka configurations:

```yaml
configs:
  "security.protocol": "SASL_SSL"                    # String
  "max.poll.records": 500                            # Number
  "enable.auto.commit": true                         # Boolean
  "some.optional.config": null                       # Null
```

## Code Generation Benefits

### Before (Generic Parsing)
```java
// Type-unsafe, requires casting
Map<String, Object> feed = yamlMapper.readValue(file, Map.class);
String name = (String) feed.get("name");
Map<String, Object> kafkaTopic = (Map<String, Object>) feed.get("kafka-topic");
String topics = (String) kafkaTopic.get("topics");
```

### After (Generated Classes)
```java
// Type-safe, compile-time checking
List<FeedConfiguration> feeds = yamlMapper.readValue(file, 
    new TypeReference<List<FeedConfiguration>>() {});
    
for (FeedConfiguration feed : feeds) {
    if (feed.getKafkaTopic() != null) {
        KafkaTopic topic = feed.getKafkaTopic();
        System.out.println(topic.getTopics());        // Type-safe access
        System.out.println(topic.getBootstrapServers());
    }
}
```

## Configuration

### Maven Configuration

The `pom.xml` includes the jsonschema2pojo plugin:

```xml
<plugin>
    <groupId>org.jsonschema2pojo</groupId>
    <artifactId>jsonschema2pojo-maven-plugin</artifactId>
    <version>1.2.1</version>
    <configuration>
        <sourceDirectory>${basedir}/src/main/resources/schema</sourceDirectory>
        <targetPackage>com.demo.schema.generated</targetPackage>
        <generateBuilders>true</generateBuilders>
        <annotationStyle>jackson2</annotationStyle>
        <!-- ... more options ... -->
    </configuration>
</plugin>
```

### Gradle Configuration

The `build.gradle` includes similar configuration:

```gradle
jsonSchema2Pojo {
    source = files("${projectDir}/src/main/resources/schema")
    targetPackage = 'com.demo.schema.generated'
    generateBuilders = true
    annotationStyle = 'jackson2'
    // ... more options ...
}
```

## Examples

### Schema Validation

```java
SchemaValidator validator = new SchemaValidator();
ProcessingReport report = validator.validateFile("schema.json", "feed.yaml");
if (validator.isValid(report)) {
    System.out.println("✅ Validation successful!");
} else {
    System.out.println("❌ Validation failed!");
    validator.printReport(report);
}
```

### YAML Parsing with Generated Classes

```java
// After generating classes
ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
List<FeedConfiguration> feeds = yamlMapper.readValue(
    new File("feed.yaml"), 
    new TypeReference<List<FeedConfiguration>>() {}
);

// Type-safe access
for (FeedConfiguration feed : feeds) {
    if (feed.getFileWatcher() != null) {
        FileWatcher watcher = feed.getFileWatcher();
        System.out.println("Watcher: " + watcher.getName());
        System.out.println("Interval: " + watcher.getInterval());
    }
}
```

## Complex Validation with Spring Validation API (Java 8 Compatible)

For complex business rules that cannot be expressed in JSON Schema (like cross-field validation), 
this project includes examples using Spring Validation API compatible with Java 8.

### Complex Validation Examples

1. **Basic Complex Validation** (`ComplexValidationExample.java`)
   - Demonstrates custom validation rules like: "if fieldA=10 and fieldB=200, then fieldC must be 300 or 400"
   - Shows how to create custom annotations and validators

2. **Schema + Custom Validation** (`SchemaWithCustomValidation.java`)
   - Combines JSON Schema validation with custom business rules
   - Validates YAML files against schema first, then applies custom rules

### Running Complex Validation Examples

```bash
# Basic complex validation example
mvn exec:java -Dexec.mainClass="com.demo.schema.validation.ComplexValidationExample"

# Schema + custom validation example
mvn exec:java -Dexec.mainClass="com.demo.schema.validation.SchemaWithCustomValidation"

# Run tests
mvn test -Dtest=ComplexValidationTest
```

### Custom Validation Rules Examples

```java
// Rule: If file-watcher interval < 30 seconds, then monitor-uri must be specified
if (config.getFileWatcher() != null && config.getFileWatcher().getInterval() < 30) {
    if (config.getFileWatcher().getMonitorUri() == null || 
        config.getFileWatcher().getMonitorUri().trim().isEmpty()) {
        addConstraintViolation(context, "If file-watcher interval < 30 seconds, monitor-uri must be specified");
        return false;
    }
}

// Rule: If kafka-topic has SSL security, then port must be 9093
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
```

### Creating Custom Validators

1. **Define Custom Annotation**:
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MyCustomValidator.class)
public @interface MyCustomValidation {
    String message() default "Custom validation failed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

2. **Implement Validator**:
```java
public class MyCustomValidator implements ConstraintValidator<MyCustomValidation, MyClass> {
    @Override
    public boolean isValid(MyClass obj, ConstraintValidatorContext context) {
        // Your complex validation logic here
        if (condition1 && condition2) {
            return expectedResult;
        }
        return true;
    }
}
```

3. **Apply to Class**:
```java
@MyCustomValidation
public class MyClass {
    // Your fields here
}
```

## Testing
Run the test suite:

```bash
# Maven
mvn test

# Gradle
./gradlew test
```

The tests validate:
- Schema validation functionality
- YAML parsing capabilities
- Generated class usage

## Dependencies

- **JSON Schema Validation**: `com.github.java-json-tools:json-schema-validator`
- **YAML Processing**: `org.yaml:snakeyaml`
- **JSON Processing**: `com.fasterxml.jackson.core:jackson-databind`
- **Code Generation**: `org.jsonschema2pojo:jsonschema2pojo-maven-plugin`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request


## complicated validation
Complex cross-field validation rules like the one you described cannot be implemented in JSON Schema alone. JSON Schema is limited to basic validation patterns and doesn't support complex business logic. For such scenarios, you'll need to implement custom validation in Java code.


##
For a pure Kafka streaming use case, Spring Cloud Stream would be ideal. For your complex data processing pipeline, you might want to:

## Use Spring Cloud Stream for Kafka consumption
## Implement custom processors for the data transformation logic
## Integrate with Spring Batch for file processing capabilities
## Use Spring Data for Hive/database operations

## License

# linux command

# java -Dlog4j.configurationFile=classpath:log4j2.xml 
 -DlogPath=$LOG_DIR -Dspring.profiles.active=toolkit -Dspring.configuration.location=${REF_TOKEN_PATH}/ -jar ${FL_JAR_PATH}/rdh_toolkit.jar --feeds file:{$REF_TOKEN_PATH}/{$PRODUCT}.yaml  1>&2 &PID=$!
This project is licensed under the MIT License. 