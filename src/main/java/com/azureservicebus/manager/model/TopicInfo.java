package com.azureservicebus.manager.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Modelo para informações de um tópico do Azure Service Bus
 */
public class TopicInfo {
    
    private String name;
    private String status;
    private int maxDeliveryCount;
    private Duration defaultMessageTimeToLive;
    private Duration duplicateDetectionHistoryTimeWindow;
    private long maxSizeInMB;
    private boolean partitioningEnabled;
    private boolean duplicateDetectionEnabled;
    private boolean batchedOperationsEnabled;
    private boolean orderingSupported;
    
    // Runtime properties
    private long totalMessages;
    private long activeMessages;
    private long deadLetterMessages;
    private long scheduledMessages;
    private double sizeInKB;
    private int subscriptionCount;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime accessedAt;
    
    public TopicInfo(String name) {
        this.name = name;
    }
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getMaxDeliveryCount() {
        return maxDeliveryCount;
    }
    
    public void setMaxDeliveryCount(int maxDeliveryCount) {
        this.maxDeliveryCount = maxDeliveryCount;
    }
    
    public Duration getDefaultMessageTimeToLive() {
        return defaultMessageTimeToLive;
    }
    
    public void setDefaultMessageTimeToLive(Duration defaultMessageTimeToLive) {
        this.defaultMessageTimeToLive = defaultMessageTimeToLive;
    }
    
    public Duration getDuplicateDetectionHistoryTimeWindow() {
        return duplicateDetectionHistoryTimeWindow;
    }
    
    public void setDuplicateDetectionHistoryTimeWindow(Duration duplicateDetectionHistoryTimeWindow) {
        this.duplicateDetectionHistoryTimeWindow = duplicateDetectionHistoryTimeWindow;
    }
    
    public long getMaxSizeInMB() {
        return maxSizeInMB;
    }
    
    public void setMaxSizeInMB(long maxSizeInMB) {
        this.maxSizeInMB = maxSizeInMB;
    }
    
    public boolean isPartitioningEnabled() {
        return partitioningEnabled;
    }
    
    public void setPartitioningEnabled(boolean partitioningEnabled) {
        this.partitioningEnabled = partitioningEnabled;
    }
    
    public boolean isDuplicateDetectionEnabled() {
        return duplicateDetectionEnabled;
    }
    
    public void setDuplicateDetectionEnabled(boolean duplicateDetectionEnabled) {
        this.duplicateDetectionEnabled = duplicateDetectionEnabled;
    }
    
    public boolean isBatchedOperationsEnabled() {
        return batchedOperationsEnabled;
    }
    
    public void setBatchedOperationsEnabled(boolean batchedOperationsEnabled) {
        this.batchedOperationsEnabled = batchedOperationsEnabled;
    }
    
    public boolean isOrderingSupported() {
        return orderingSupported;
    }
    
    public void setOrderingSupported(boolean orderingSupported) {
        this.orderingSupported = orderingSupported;
    }
    
    public long getTotalMessages() {
        return totalMessages;
    }
    
    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }
    
    public long getActiveMessages() {
        return activeMessages;
    }
    
    public void setActiveMessages(long activeMessages) {
        this.activeMessages = activeMessages;
    }
    
    public long getDeadLetterMessages() {
        return deadLetterMessages;
    }
    
    public void setDeadLetterMessages(long deadLetterMessages) {
        this.deadLetterMessages = deadLetterMessages;
    }
    
    public long getScheduledMessages() {
        return scheduledMessages;
    }
    
    public void setScheduledMessages(long scheduledMessages) {
        this.scheduledMessages = scheduledMessages;
    }
    
    public double getSizeInKB() {
        return sizeInKB;
    }
    
    public void setSizeInKB(double sizeInKB) {
        this.sizeInKB = sizeInKB;
    }
    
    public int getSubscriptionCount() {
        return subscriptionCount;
    }
    
    public void setSubscriptionCount(int subscriptionCount) {
        this.subscriptionCount = subscriptionCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }
    
    public void setAccessedAt(LocalDateTime accessedAt) {
        this.accessedAt = accessedAt;
    }
    
    @Override
    public String toString() {
        return String.format("TopicInfo{name='%s', status='%s', totalMessages=%d, subscriptions=%d}", 
            name, status, totalMessages, subscriptionCount);
    }
}
