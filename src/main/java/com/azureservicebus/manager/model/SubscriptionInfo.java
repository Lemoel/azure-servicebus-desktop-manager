package com.azureservicebus.manager.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Modelo para informações de uma subscription do Azure Service Bus
 */
public class SubscriptionInfo {
    
    private String name;
    private String topicName;
    private String status;
    private int maxDeliveryCount;
    private Duration lockDuration;
    private Duration defaultMessageTimeToLive;
    private Duration autoDeleteOnIdle;
    private boolean sessionRequired;
    private boolean deadLetteringOnMessageExpiration;
    private boolean deadLetteringOnFilterEvaluationException;
    private boolean batchedOperationsEnabled;
    
    // Runtime properties
    private long totalMessages;
    private long activeMessages;
    private long deadLetterMessages;
    private long scheduledMessages;
    private long transferMessageCount;
    private long transferDeadLetterMessageCount;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime accessedAt;
    
    public SubscriptionInfo(String topicName, String name) {
        this.topicName = topicName;
        this.name = name;
    }
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTopicName() {
        return topicName;
    }
    
    public void setTopicName(String topicName) {
        this.topicName = topicName;
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
    
    public Duration getLockDuration() {
        return lockDuration;
    }
    
    public void setLockDuration(Duration lockDuration) {
        this.lockDuration = lockDuration;
    }
    
    public Duration getDefaultMessageTimeToLive() {
        return defaultMessageTimeToLive;
    }
    
    public void setDefaultMessageTimeToLive(Duration defaultMessageTimeToLive) {
        this.defaultMessageTimeToLive = defaultMessageTimeToLive;
    }
    
    public Duration getAutoDeleteOnIdle() {
        return autoDeleteOnIdle;
    }
    
    public void setAutoDeleteOnIdle(Duration autoDeleteOnIdle) {
        this.autoDeleteOnIdle = autoDeleteOnIdle;
    }
    
    public boolean isSessionRequired() {
        return sessionRequired;
    }
    
    public void setSessionRequired(boolean sessionRequired) {
        this.sessionRequired = sessionRequired;
    }
    
    public boolean isDeadLetteringOnMessageExpiration() {
        return deadLetteringOnMessageExpiration;
    }
    
    public void setDeadLetteringOnMessageExpiration(boolean deadLetteringOnMessageExpiration) {
        this.deadLetteringOnMessageExpiration = deadLetteringOnMessageExpiration;
    }
    
    public boolean isDeadLetteringOnFilterEvaluationException() {
        return deadLetteringOnFilterEvaluationException;
    }
    
    public void setDeadLetteringOnFilterEvaluationException(boolean deadLetteringOnFilterEvaluationException) {
        this.deadLetteringOnFilterEvaluationException = deadLetteringOnFilterEvaluationException;
    }
    
    public boolean isBatchedOperationsEnabled() {
        return batchedOperationsEnabled;
    }
    
    public void setBatchedOperationsEnabled(boolean batchedOperationsEnabled) {
        this.batchedOperationsEnabled = batchedOperationsEnabled;
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
    
    public long getTransferMessageCount() {
        return transferMessageCount;
    }
    
    public void setTransferMessageCount(long transferMessageCount) {
        this.transferMessageCount = transferMessageCount;
    }
    
    public long getTransferDeadLetterMessageCount() {
        return transferDeadLetterMessageCount;
    }
    
    public void setTransferDeadLetterMessageCount(long transferDeadLetterMessageCount) {
        this.transferDeadLetterMessageCount = transferDeadLetterMessageCount;
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
        return String.format("SubscriptionInfo{topic='%s', name='%s', status='%s', totalMessages=%d}", 
            topicName, name, status, totalMessages);
    }
}
