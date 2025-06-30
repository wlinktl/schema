package com.demo.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration class that maps to the YAML configuration structure
 */
@Component
@ConfigurationProperties(prefix = "file-processing")
public class FileProcessingConfig {
    
    private List<Configuration> configurations;
    
    // Getters and setters
    public List<Configuration> getConfigurations() {
        return configurations;
    }
    
    public void setConfigurations(List<Configuration> configurations) {
        this.configurations = configurations;
    }
    
    /**
     * Individual configuration item
     */
    public static class Configuration {
        private String name;
        private boolean active = true; // default value
        private String layer;
        private String logsUri;
        private FileWatcher fileWatcher;
        private List<InboundDataset> inboundDatasets;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public String getLayer() { return layer; }
        public void setLayer(String layer) { this.layer = layer; }
        
        public String getLogsUri() { return logsUri; }
        public void setLogsUri(String logsUri) { this.logsUri = logsUri; }
        
        public FileWatcher getFileWatcher() { return fileWatcher; }
        public void setFileWatcher(FileWatcher fileWatcher) { this.fileWatcher = fileWatcher; }
        
        public List<InboundDataset> getInboundDatasets() { return inboundDatasets; }
        public void setInboundDatasets(List<InboundDataset> inboundDatasets) { this.inboundDatasets = inboundDatasets; }
    }
    
    /**
     * File watcher configuration
     */
    public static class FileWatcher {
        private String name;
        private boolean active;
        private int interval;
        private String inboundUri;
        private String preprocessUri;
        private String monitorUri;
        private Transaction transaction;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public int getInterval() { return interval; }
        public void setInterval(int interval) { this.interval = interval; }
        
        public String getInboundUri() { return inboundUri; }
        public void setInboundUri(String inboundUri) { this.inboundUri = inboundUri; }
        
        public String getPreprocessUri() { return preprocessUri; }
        public void setPreprocessUri(String preprocessUri) { this.preprocessUri = preprocessUri; }
        
        public String getMonitorUri() { return monitorUri; }
        public void setMonitorUri(String monitorUri) { this.monitorUri = monitorUri; }
        
        public Transaction getTransaction() { return transaction; }
        public void setTransaction(Transaction transaction) { this.transaction = transaction; }
    }
    
    /**
     * Transaction configuration
     */
    public static class Transaction {
        private Commit commit;
        private Rollback rollback;
        
        // Getters and setters
        public Commit getCommit() { return commit; }
        public void setCommit(Commit commit) { this.commit = commit; }
        
        public Rollback getRollback() { return rollback; }
        public void setRollback(Rollback rollback) { this.rollback = rollback; }
    }
    
    public static class Commit {
        private String successUri;
        
        public String getSuccessUri() { return successUri; }
        public void setSuccessUri(String successUri) { this.successUri = successUri; }
    }
    
    public static class Rollback {
        private String failureUri;
        
        public String getFailureUri() { return failureUri; }
        public void setFailureUri(String failureUri) { this.failureUri = failureUri; }
    }
    
    /**
     * Inbound dataset configuration
     */
    public static class InboundDataset {
        private String name;
        private boolean active;
        private String pattern;
        private String type;
        private String module;
        private String format;
        private String compression;
        private String delimiter;
        private boolean trailingDelimeter;
        private String addColumnsCsv;
        private String cobScript;
        private TextHeader textHeader;
        private TextTrailer textTrailer;
        private Output output;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
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
        
        public boolean isTrailingDelimeter() { return trailingDelimeter; }
        public void setTrailingDelimeter(boolean trailingDelimeter) { this.trailingDelimeter = trailingDelimeter; }
        
        public String getAddColumnsCsv() { return addColumnsCsv; }
        public void setAddColumnsCsv(String addColumnsCsv) { this.addColumnsCsv = addColumnsCsv; }
        
        public String getCobScript() { return cobScript; }
        public void setCobScript(String cobScript) { this.cobScript = cobScript; }
        
        public TextHeader getTextHeader() { return textHeader; }
        public void setTextHeader(TextHeader textHeader) { this.textHeader = textHeader; }
        
        public TextTrailer getTextTrailer() { return textTrailer; }
        public void setTextTrailer(TextTrailer textTrailer) { this.textTrailer = textTrailer; }
        
        public Output getOutput() { return output; }
        public void setOutput(Output output) { this.output = output; }
    }
    
    public static class TextHeader {
        private String type;
        private String token;
        private int sourceNamePosition;
        private int cobPosition;
        private int snapshotPosition;
        private String snapshotFormat;
        private int recordCountPosition;
        private int columnCountPosition;
        private boolean columnNamesPresent;
        private int frequencyPosition;
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        
        public int getSourceNamePosition() { return sourceNamePosition; }
        public void setSourceNamePosition(int sourceNamePosition) { this.sourceNamePosition = sourceNamePosition; }
        
        public int getCobPosition() { return cobPosition; }
        public void setCobPosition(int cobPosition) { this.cobPosition = cobPosition; }
        
        public int getSnapshotPosition() { return snapshotPosition; }
        public void setSnapshotPosition(int snapshotPosition) { this.snapshotPosition = snapshotPosition; }
        
        public String getSnapshotFormat() { return snapshotFormat; }
        public void setSnapshotFormat(String snapshotFormat) { this.snapshotFormat = snapshotFormat; }
        
        public int getRecordCountPosition() { return recordCountPosition; }
        public void setRecordCountPosition(int recordCountPosition) { this.recordCountPosition = recordCountPosition; }
        
        public int getColumnCountPosition() { return columnCountPosition; }
        public void setColumnCountPosition(int columnCountPosition) { this.columnCountPosition = columnCountPosition; }
        
        public boolean isColumnNamesPresent() { return columnNamesPresent; }
        public void setColumnNamesPresent(boolean columnNamesPresent) { this.columnNamesPresent = columnNamesPresent; }
        
        public int getFrequencyPosition() { return frequencyPosition; }
        public void setFrequencyPosition(int frequencyPosition) { this.frequencyPosition = frequencyPosition; }
    }
    
    public static class TextTrailer {
        private String token;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
    
    public static class Output {
        private Hive hive;
        private String doneFileUri;
        private String doneFileType;
        
        // Getters and setters
        public Hive getHive() { return hive; }
        public void setHive(Hive hive) { this.hive = hive; }
        
        public String getDoneFileUri() { return doneFileUri; }
        public void setDoneFileUri(String doneFileUri) { this.doneFileUri = doneFileUri; }
        
        public String getDoneFileType() { return doneFileType; }
        public void setDoneFileType(String doneFileType) { this.doneFileType = doneFileType; }
    }
    
    public static class Hive {
        private String tableName;
        private java.util.Map<String, String> partitions;
        
        // Getters and setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        
        public java.util.Map<String, String> getPartitions() { return partitions; }
        public void setPartitions(java.util.Map<String, String> partitions) { this.partitions = partitions; }
    }
} 